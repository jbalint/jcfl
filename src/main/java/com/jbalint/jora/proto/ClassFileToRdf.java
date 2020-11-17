package com.jbalint.jora.proto;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.stardog.stark.BNode;
import com.stardog.stark.IRI;
import com.stardog.stark.Literal;
import com.stardog.stark.Namespace;
import com.stardog.stark.Namespaces;
import com.stardog.stark.Statement;
import com.stardog.stark.Values;
import com.stardog.stark.io.RDFFormats;
import com.stardog.stark.io.RDFWriters;
import com.stardog.stark.vocabs.RDF;
import com.stardog.stark.vocabs.RDFS;

import com.google.common.collect.ImmutableMap;
import com.jbalint.jcfl.AttributeInfo;
import com.jbalint.jcfl.ClassFile;
import com.jbalint.jcfl.ConstantPoolInfo;
import com.jbalint.jcfl.ConstantValue;
import com.jbalint.jcfl.DoubleInfo;
import com.jbalint.jcfl.FieldOrMethodInfo;
import com.jbalint.jcfl.FloatInfo;
import com.jbalint.jcfl.IntegerInfo;
import com.jbalint.jcfl.JcflVocab;
import com.jbalint.jcfl.LongInfo;

import static com.jbalint.jcfl.JcflVocab.NS;

/**
 * Serialize a (parsed) Class to RDF
 */
public class ClassFileToRdf {

	static IRI methodIri(String className, String methodName, String methodSignature) {
		return JcflVocab.JAVAP(className + "." + methodName + methodSignature.replaceAll("/", "."));
	}

	static IRI fieldIri(String className, String fieldName) {
		return JcflVocab.JAVAP(className + "." + fieldName);
	}

	static Map<String, IRI> constantTypes = ImmutableMap.<String, IRI>builder()
            .put("I", JcflVocab.JAVAP("int"))
            .put("J", JcflVocab.JAVAP("long"))
            .put("B", JcflVocab.JAVAP("byte"))
            .put("Z", JcflVocab.JAVAP("boolean"))
            .put("D", JcflVocab.JAVAP("double"))
            .put("F", JcflVocab.JAVAP("float"))
            .put("S", JcflVocab.JAVAP("short"))
            .put("C", JcflVocab.JAVAP("char"))
            .put("V", JcflVocab.JAVAP("void"))
            .build();

    static Map<String, IRI> arrayTypeCache = new HashMap<>();

    private static IRI getTypeIndividual(Set<Statement> model, String typeName) {
        if (constantTypes.containsKey(typeName)) {
            return constantTypes.get(typeName);
        }

        if (typeName.startsWith("[")) {
            if (arrayTypeCache.containsKey(typeName)) {
                return arrayTypeCache.get(typeName);
            }
            // create a new array type
            int dims = 0;
            while (typeName.charAt(dims) == '[') {
                dims++;
            }
            String elementClassName;
            if (typeName.charAt(dims) == 'L') {
                elementClassName = typeName.substring(dims + 1, typeName.length() - 1);
            } else {
                elementClassName = typeName.substring(dims);
            }
            IRI typeIri = JcflVocab.classIri(elementClassName + new String(new char[dims]).replace("\0", "[]"));
            model.add(Values.statement(typeIri, JcflVocab.JAVAP("dimensions"), Values.literal(dims)));
            model.add(Values.statement(typeIri, JcflVocab.JAVAP("arrayOf"), JcflVocab.classIri(elementClassName)));
            return typeIri;
        } else {
            // extract `name' from `Lname;'
            return JcflVocab.classIri(typeName.substring(1, typeName.length() - 1));
        }
    }

	private static void constructMethod(Set<Statement> model, String className, FieldOrMethodInfo fmInfo) {
		IRI method = methodIri(className, fmInfo.getName(), fmInfo.getDescriptorString());
		model.add(Values.statement(JcflVocab.classIri(className), JcflVocab.JAVAP("hasMember"), method));
		model.add(Values.statement(method, RDF.TYPE, JcflVocab.JAVAP("Method")));
		model.add(Values.statement(method, JcflVocab.JAVAP("name"), Values.literal(fmInfo.getName())));
		model.add(Values.statement(method, RDFS.LABEL, Values.literal(className + "." + fmInfo.getName() + fmInfo.getDescriptorString())));
		model.add(Values.statement(method, JcflVocab.JAVAP("descriptor"), Values.literal(fmInfo.getDescriptorString())));
        fmInfo.calledMethods.stream()
                .map(mr -> methodIri(mr.getClassName(), mr.getNameAndType().getName(), mr.getNameAndType().getDescriptor()))
                .forEach(iri -> model.add(Values.statement(method, JcflVocab.JAVAP("calls"), iri)));
        model.add(Values.statement(method, JcflVocab.JAVAP("returns"), getTypeIndividual(model, fmInfo.getReturnTypeName())));
		model.add(Values.statement(method, JcflVocab.JAVAP("arity"), Values.literal(fmInfo.getArgumentTypeNames().size())));
        for (int i = 0; i < fmInfo.getArgumentTypeNames().size(); ++i) {
            BNode arg = Values.bnode();
            model.add(Values.statement(method, JcflVocab.JAVAP("argument"), arg));
            model.add(Values.statement(arg, RDF.TYPE, JcflVocab.JAVAP("Argument")));
            model.add(Values.statement(arg, JcflVocab.JAVAP("position"), Values.literal(i)));
            model.add(Values.statement(arg, JcflVocab.JAVAP("hasType"), getTypeIndividual(model, fmInfo.getArgumentTypeNames().get(i))));
		}
		SerializeAttributes serializeAttributes = new SerializeAttributes(model, method);
		fmInfo.attributes.forEach(attr -> attr.accept(serializeAttributes));
	}

	private static void constructField(Set<Statement> model, String className, FieldOrMethodInfo fmInfo) {
		IRI field = fieldIri(className, fmInfo.getName());
		model.add(Values.statement(JcflVocab.classIri(className), JcflVocab.JAVAP("hasMember"), field));
		model.add(Values.statement(field, RDF.TYPE, JcflVocab.JAVAP("Variable")));
		model.add(Values.statement(field, RDFS.LABEL, Values.literal(className + "." + fmInfo.getName() + ":" + fmInfo.getDescriptorString())));
		model.add(Values.statement(field, JcflVocab.JAVAP("name"), Values.literal(fmInfo.getName())));
        model.add(Values.statement(field, JcflVocab.JAVAP("hasType"), getTypeIndividual(model, fmInfo.getDescriptorString())));
        // TODO type
		for (AttributeInfo a : fmInfo.attributes) {
			if (a.type.equals("ConstantValue")) {
				ConstantPoolInfo cv = ((ConstantValue) a).constantValue;
				if (cv.type == ConstantPoolInfo.InfoType.STRING) {
					model.add(Values.statement(field, JcflVocab.JAVAP("initialValue"), Values.literal(cv.asString())));
				} else if (cv.type == ConstantPoolInfo.InfoType.INTEGER) {
					model.add(Values.statement(field, JcflVocab.JAVAP("initialValue"), Values.literal(((IntegerInfo) cv).value)));
				} else if (cv.type == ConstantPoolInfo.InfoType.FLOAT) {
					model.add(Values.statement(field, JcflVocab.JAVAP("initialValue"), Values.literal(((FloatInfo) cv).value)));
				} else if (cv.type == ConstantPoolInfo.InfoType.LONG) {
					model.add(Values.statement(field, JcflVocab.JAVAP("initialValue"), Values.literal(((LongInfo) cv).value)));
				} else if (cv.type == ConstantPoolInfo.InfoType.DOUBLE) {
					double v = ((DoubleInfo) cv).value;
					Literal l = Values.literal(v);
					model.add(Values.statement(field, JcflVocab.JAVAP("initialValue"), l));

					//model.add(field, JAVAP("initialValue"), Values.literal(((DoubleInfo) cv).value));
				} else {
					throw new IllegalArgumentException("Unhandled constant type: " + cv.type + " (" + cv + ")");
				}
			} else {
				if (!ignoredTypes.contains(a.type)) {
					System.out.println("(ignored field attribute type=" + a.type + ")");
					ignoredTypes.add(a.type);
				}
			}
		}
	}
	static HashSet<String> ignoredTypes = new HashSet<>();

	public static Set<Statement> toRdf(ClassFile cf) {
		Set<Statement> model = new HashSet<>();
		toRdf(model, cf);
		return model;
	}

	public static void toRdf(Set<Statement> model, ClassFile cf) {
		IRI classIri = JcflVocab.classIri(cf.className);
		model.add(Values.statement(classIri, RDF.TYPE, JcflVocab.JAVAP("Class")));
		model.add(Values.statement(classIri, RDFS.LABEL, Values.literal(cf.className)));
		model.add(Values.statement(classIri, JcflVocab.JAVAP("name"), Values.literal(cf.className)));

		model.add(Values.statement(classIri, JcflVocab.JAVAP("subClassOf"), JcflVocab.classIri(cf.superclassName)));
		cf.getInterfaces().stream()
			.map(JcflVocab::classIri)
			.forEach(theInterface -> model.add(Values.statement(classIri, JcflVocab.JAVAP("implements"), theInterface)));

		SerializeAttributes cfAttrSerializer = new SerializeAttributes(model, classIri);
		cf.attributes.forEach(attr -> attr.accept(cfAttrSerializer));

        // TODO add interface and other modifiers, visibility, local variables, throw declarations
		for (FieldOrMethodInfo fm : cf.fieldsAndMethods) {
			if (fm.type == 'M') {
				try {
					fm.analyzeCode();
				} catch (Exception ex) {
					System.err.println("Failed to analyze: " + fm);
					ex.printStackTrace();
				}

				constructMethod(model, cf.className, fm);
			} else if (fm.type == 'F') {
				constructField(model, cf.className, fm);
			}
		}
	}

	public static String toTurtleString(Set<Statement> m) {

		List<Namespace> namespaces = new ArrayList<>();
		Namespaces.EXTENDED.forEach(namespaces::add);
		namespaces.add(Values.namespace("javap", NS));

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		RDFWriters.write(output, RDFFormats.PRETTY_TURTLE, m, namespaces);
		return new String(output.toByteArray());
	}

	public static void writeTurtleString(Set<Statement> m, OutputStream output) {

		List<Namespace> namespaces = new ArrayList<>();
		Namespaces.EXTENDED.forEach(namespaces::add);
		namespaces.add(Values.namespace("javap", NS));

		RDFWriters.write(output, RDFFormats.PRETTY_TURTLE, m, namespaces);
	}

}
