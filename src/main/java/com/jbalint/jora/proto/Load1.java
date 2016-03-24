package com.jbalint.jora.proto;

import java.util.HashMap;

import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.SelectQuery;
import com.complexible.common.protocols.server.ServerBuilder;

import com.complexible.common.openrdf.model.Models2;
import com.complexible.common.rdf.model.Values;

// http://rdf4j.org/sesame/2.7/apidocs/org/openrdf/model/package-summary.html
import org.openrdf.model.BNode;
import org.openrdf.model.IRI;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;

import com.jbalint.jcfl.*;

// http://docs.stardog.com/java/snarl/index.html?com/complexible/common/protocols/server/ServerBuilder.html

public class Load1 {
	static IRI JAVAP(String name) {
		return Values.iri("http://jbalint/javap#" + name);
	}

	private Connection conn;

	private HashMap<String, Resource> classNameToResource = new HashMap<>();
	private HashMap<String, Resource> methodToResource = new HashMap<>();

	public Load1() {
		conn = ConnectionConfiguration
			.to("jora1")
			.server("snarl://tagore:5820")
			.credentials("admin", "admin")
			.connect();

		initClassMap();
	}

	private void initClassMap() {
		SelectQuery stmt = conn.select("select ?class ?className where { ?class a javap:Class . ?class javap:name ?className }");
		// http://rdf4j.org/sesame/2.7/docs/articles/repository-api/querying.docbook?view
		// http://rdf4j.org/doc/4/apidocs/index.html?info/aduna/iteration/CloseableIteration.html
		TupleQueryResult res = stmt.execute();
		// TODO: use QueryResults.stream() and Collectors.toMap()
		while (res.hasNext()) {
			BindingSet s = res.next();
			classNameToResource.put(s.getValue("className").stringValue(), (Resource) s.getValue("class"));
		}
		res.close();
		System.err.println("Class map:\n" + classNameToResource.toString().replaceAll(",", ",\n"));
	}

	private Resource getOrCreateClass(String className) {
		Resource clazz = classNameToResource.get(className);
		if (clazz == null) {
			clazz = Values.bnode();
			classNameToResource.put(className, clazz);
			System.err.println("Creating class " + className);
			Model aGraph = Models2.newModel(Values.statement(clazz, RDF.TYPE, JAVAP("Class")),
											Values.statement(clazz, RDFS.LABEL, Values.literal(className)),
											Values.statement(clazz, JAVAP("name"), Values.literal(className)));
			conn.add().graph(aGraph);
		}
		return clazz;
	}

	private Resource getOrCreateMethod(String className, String methodName, String descriptor) {
		String cacheString = className + "." + methodName + descriptor;
		Resource fm = methodToResource.get(cacheString);
		if (fm != null) {
			return fm;
		}
		// query to see if method exists
		Resource clazz = getOrCreateClass(className);
		String query = "select ?m where { ?c javap:member ?m. ?m javap:name ?name. ?m javap:descriptor ?descriptor. }";
		SelectQuery stmt = conn.select(query);
		stmt.parameter("name", methodName);
		stmt.parameter("descriptor", descriptor);
		TupleQueryResult res = stmt.execute();
		if (res.hasNext()) {
			fm = (Resource) res.next().getValue("m");
			res.close();
			methodToResource.put(cacheString, fm);
			return fm;
		}

		// otherwise, create it anew
		fm = Values.bnode();
		String label = className + "." + methodName + descriptor;
		Model methodGraph = Models2.newModel(Values.statement(clazz, JAVAP("hasMember"), fm),
											 Values.statement(fm, RDF.TYPE, JAVAP("Method")),
											 Values.statement(fm, JAVAP("name"), Values.literal(methodName)),
											 Values.statement(fm, RDFS.LABEL, Values.literal(label)),
											 Values.statement(fm, JAVAP("descriptor"), Values.literal(descriptor)));
		conn.add().graph(methodGraph);
		methodToResource.put(cacheString, fm);
		return fm;
	}

	private Model constructMethod(String className, Resource clazz, FieldOrMethodInfo fmInfo) {
		try {
			fmInfo.analyzeCode();
		} catch (Exception ex) {
			System.err.println("Failed to analyze: " + fmInfo);
			ex.printStackTrace();
		}
		Resource fm = getOrCreateMethod(className, fmInfo.getName(), fmInfo.getDescriptor());
		Model aGraph = Models2.newModel();
		for (Methodref mr : fmInfo.calledMethods) {
			Resource calledMethod = getOrCreateMethod(mr.getClassName(), mr.getNameAndType().getName(), mr.getNameAndType().getDescriptor());
			aGraph.add(fm, JAVAP("calls"), calledMethod);
		}
		for (AttributeInfo a : fmInfo.attributes) {
			if (a.type.equals("Exceptions")) {
				// XXX: TODO: XXX
			} else if (a.type.equals("Signature")) {
				// XXX: TODO: XXX
			} else {
				System.err.println("(ignored attribute type=" + a.type + ")");
			}
		}
		return aGraph;
	}

	private Model constructField(String className, Resource clazz, FieldOrMethodInfo fmInfo) {
		Value type = JAVAP("Variable");
		String label = className + "." + fmInfo.getName() + ":" + fmInfo.getDescriptor();
		Resource fm = Values.bnode();
		Model aGraph = Models2.newModel();
		for (AttributeInfo a : fmInfo.attributes) {
			if (a.type.equals("ConstantValue")) {
				ConstantPoolInfo cv = ((ConstantValue) a).constantValue;
				if (cv.type == ConstantPoolInfo.InfoType.STRING) {
					aGraph.add(fm, JAVAP("initialValue"), Values.literal(cv.asString()));
				} else if (cv.type == ConstantPoolInfo.InfoType.INTEGER) {
					aGraph.add(fm, JAVAP("initialValue"), Values.literal(((IntegerInfo) cv).value));
				} else if (cv.type == ConstantPoolInfo.InfoType.FLOAT) {
					aGraph.add(fm, JAVAP("initialValue"), Values.literal(((FloatInfo) cv).value));
				} else if (cv.type == ConstantPoolInfo.InfoType.LONG) {
					aGraph.add(fm, JAVAP("initialValue"), Values.literal(((LongInfo) cv).value));
				} else if (cv.type == ConstantPoolInfo.InfoType.DOUBLE) {
					aGraph.add(fm, JAVAP("initialValue"), Values.literal(((DoubleInfo) cv).value));
				} else {
					throw new IllegalArgumentException("Unhandled constant type: " + cv.type + " (" + cv + ")");
				}
			} else {
				System.err.println("(ignored attribute type=" + a.type + ")");
			}
		}
		return aGraph;
	}

	public void load(ClassFile cf) throws Exception {
		try {
			conn.begin();

			Resource clazz = getOrCreateClass(cf.getClassName());

			for (FieldOrMethodInfo fm : cf.fieldsAndMethods) {
				if (fm.type == 'M') {
					conn.add().graph(constructMethod(cf.getClassName(), clazz, fm));
				} else if (fm.type == 'F') {
					conn.add().graph(constructField(cf.getClassName(), clazz, fm));
				}
			}
			conn.commit();
		} catch (Exception ex) {
			conn.rollback();
			throw ex;
		}
	}
}
