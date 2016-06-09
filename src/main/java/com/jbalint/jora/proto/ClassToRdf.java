package com.jbalint.jora.proto;

import java.io.*;
import java.util.*;

import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.SelectQuery;
import com.complexible.common.protocols.server.ServerBuilder;

import com.complexible.common.openrdf.model.Models2;
import com.complexible.common.rdf.model.Values;

import com.complexible.common.rdf.rio.RDFWriters;

import com.complexible.common.rdf.model.StardogValueFactory;

import com.complexible.common.rdf.model.Namespaces;
// http://rdf4j.org/sesame/2.7/apidocs/org/openrdf/model/package-summary.html
import org.openrdf.model.BNode;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Namespace;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFFormat;

import com.jbalint.jcfl.*;

public class ClassToRdf {
	static IRI JAVAP(String name) {
		return Values.iri("http://jbalint/javap#" + java.net.URLEncoder.encode(name));
	}

	static IRI classIri(String className) {
		return JAVAP(className.replaceAll("/", "."));
	}

	static IRI methodIri(String className, String methodName, String methodSignature) {
		return JAVAP(className.replaceAll("/", ".") + "." + methodName + methodSignature.replaceAll("/", "."));
	}

	static IRI fieldIri(String className, String fieldName) {
		return JAVAP(className.replaceAll("/", ".") + "." + fieldName);
	}

	private static void constructMethod(Model model, String className, FieldOrMethodInfo fmInfo) {
		IRI method = methodIri(className, fmInfo.getName(), fmInfo.getDescriptor());
		model.add(classIri(className), JAVAP("hasMember"), method);
		model.add(method, RDF.TYPE, JAVAP("Method"));
		model.add(method, JAVAP("name"), Values.literal(fmInfo.getName()));
		model.add(method, RDFS.LABEL, Values.literal(className + "." + fmInfo.getName() + fmInfo.getDescriptor()));
		model.add(method, JAVAP("descriptor"), Values.literal(fmInfo.getDescriptor()));
		for (Methodref mr : fmInfo.calledMethods) {
			IRI calledMethod = methodIri(mr.getClassName(), mr.getNameAndType().getName(), mr.getNameAndType().getDescriptor());
			model.add(method, JAVAP("calls"), calledMethod);
		}
		for (AttributeInfo a : fmInfo.attributes) {
			if (a.type.equals("Exceptions")) {
				// XXX: TODO: XXX
			} else if (a.type.equals("Signature")) {
				// XXX: TODO: XXX
			} else {
				System.out.println("(ignored attribute type=" + a.type + ")");
			}
		}
	}

	private static void constructField(Model model, String className, FieldOrMethodInfo fmInfo) {
		IRI field = fieldIri(className, fmInfo.getName());
		model.add(field, RDF.TYPE, JAVAP("Variable"));
		model.add(field, RDFS.LABEL, Values.literal(className + "." + fmInfo.getName() + ":" + fmInfo.getDescriptor()));
		model.add(field, JAVAP("name"), Values.literal(fmInfo.getName()));
		switch (fmInfo.getDescriptor()) {
		case "I":
			model.add(field, JAVAP("type"), JAVAP("Int"));
		break;
		case "J":
			model.add(field, JAVAP("type"), JAVAP("Long"));
		break;
		case "B":
			model.add(field, JAVAP("type"), JAVAP("Byte"));
		break;
		case "Z":
			model.add(field, JAVAP("type"), JAVAP("Boolean"));
		break;
		case "D":
			model.add(field, JAVAP("type"), JAVAP("Double"));
		break;
		case "F":
			model.add(field, JAVAP("type"), JAVAP("Float"));
		break;
		case "S":
			model.add(field, JAVAP("type"), JAVAP("Short"));
		break;
		case "C":
			model.add(field, JAVAP("type"), JAVAP("Char"));
		break;
		default:
			if (fmInfo.getDescriptor().startsWith("[")) {
				System.out.println("Ignored type " + fmInfo.getDescriptor() + " for field " + className + "." + fmInfo.getName());
			} else if (fmInfo.getDescriptor().startsWith("L") && fmInfo.getDescriptor().endsWith(";")) {
				String c = fmInfo.getDescriptor();
				model.add(field, JAVAP("type"), classIri(c.substring(1, c.length() - 1)));
			} else {
				throw new IllegalArgumentException("Unknown descriptor " + fmInfo.getDescriptor() + " for field " + className + "." + fmInfo.getName());
			}
		}
		// TODO type
		for (AttributeInfo a : fmInfo.attributes) {
			if (a.type.equals("ConstantValue")) {
				ConstantPoolInfo cv = ((ConstantValue) a).constantValue;
				if (cv.type == ConstantPoolInfo.InfoType.STRING) {
					model.add(field, JAVAP("initialValue"), Values.literal(cv.asString()));
				} else if (cv.type == ConstantPoolInfo.InfoType.INTEGER) {
					model.add(field, JAVAP("initialValue"), Values.literal(((IntegerInfo) cv).value));
				} else if (cv.type == ConstantPoolInfo.InfoType.FLOAT) {
					model.add(field, JAVAP("initialValue"), Values.literal(((FloatInfo) cv).value));
				} else if (cv.type == ConstantPoolInfo.InfoType.LONG) {
					model.add(field, JAVAP("initialValue"), Values.literal(((LongInfo) cv).value));
				} else if (cv.type == ConstantPoolInfo.InfoType.DOUBLE) {
                    // Stardog's `RDFWriters' serializes these incorrectly causing parse errors when loading
                    // TODO they shouldn't be strings, but it's OK for the moment
                    double v = ((DoubleInfo) cv).value;
                    Literal l = Values.literal(v);
                    if (Double.isNaN(v)) {
                        l = Values.literal("NaN");//, StardogValueFactory.XSD.DOUBLE);
                    } else if (v == Double.NEGATIVE_INFINITY) {
                        l = Values.literal("-INF");//, StardogValueFactory.XSD.DOUBLE);
                    } else if (v == Double.POSITIVE_INFINITY) {
                        l = Values.literal("INF");//, StardogValueFactory.XSD.DOUBLE);
                    } else if (v < 0 && v > -1) {
                        // this one is odd, I get something like ..25e-1 for "-0.25"
                        l = Values.literal("" + v);
                    }
					model.add(field, JAVAP("initialValue"), l);
				} else {
					throw new IllegalArgumentException("Unhandled constant type: " + cv.type + " (" + cv + ")");
				}
			} else {
				System.out.println("(ignored attribute type=" + a.type + ")");
			}
		}
	}

	public static Model toRdf(ClassFile cf) {
		Model model = Models2.newModel();
		toRdf(model, cf);
		return model;
	}

	public static void toRdf(Model model, ClassFile cf) {
		IRI classIri = classIri(cf.getClassName());
		model.add(classIri, RDF.TYPE, JAVAP("Class"));
		model.add(classIri, RDFS.LABEL, Values.literal(cf.getClassName()));
		model.add(classIri, JAVAP("name"), Values.literal(cf.getClassName()));

		model.add(classIri, JAVAP("superclass"), classIri(cf.getSuperclassName()));
		cf.getInterfaces().stream()
			.map(ClassToRdf::classIri)
			.forEach(theInterface -> model.add(classIri, JAVAP("implements"), theInterface));

		for (FieldOrMethodInfo fm : cf.fieldsAndMethods) {
			if (fm.type == 'M') {
				try {
					fm.analyzeCode();
				} catch (Exception ex) {
					System.err.println("Failed to analyze: " + fm);
					ex.printStackTrace();
				}
				
				constructMethod(model, cf.getClassName(), fm);
			} else if (fm.type == 'F') {
				constructField(model, cf.getClassName(), fm);
			}
		}
	}

	public static String toN3String(Model m) {

		StardogValueFactory vf = StardogValueFactory.instance();

		List<Namespace> namespaces = new ArrayList<>();
		Namespaces.EXTENDED.forEach(namespaces::add);
		namespaces.add(vf.createNamespace("javap", "http://jbalint/javap#"));

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		RDFWriters.write(m, RDFFormat.N3, namespaces, output);
		return new String(output.toByteArray());
	}

	public static void writeN3String(Model m, OutputStream output) {

		StardogValueFactory vf = StardogValueFactory.instance();

		List<Namespace> namespaces = new ArrayList<>();
		Namespaces.EXTENDED.forEach(namespaces::add);
		namespaces.add(vf.createNamespace("javap", "http://jbalint/javap#"));

		RDFWriters.write(m, RDFFormat.N3, namespaces, output);
	}
}
