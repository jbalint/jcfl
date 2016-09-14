package com.jbalint.jora.proto;

import com.complexible.common.openrdf.model.Models2;
import com.complexible.common.rdf.model.Namespaces;
import com.complexible.common.rdf.model.StardogValueFactory;
import com.complexible.common.rdf.model.Values;
import com.complexible.common.rdf.rio.RDFWriters;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.jbalint.jcfl.*;
import org.openrdf.model.*;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.turtle.TurtleWriterFactory;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.*;

// http://rdf4j.org/sesame/2.7/apidocs/org/openrdf/model/package-summary.html

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

	static Map<String, IRI> constantTypes = ImmutableMap.<String, IRI>builder()
            .put("I", JAVAP("int"))
            .put("J", JAVAP("long"))
            .put("B", JAVAP("byte"))
            .put("Z", JAVAP("boolean"))
            .put("D", JAVAP("double"))
            .put("F", JAVAP("float"))
            .put("S", JAVAP("short"))
            .put("C", JAVAP("char"))
            .put("V", JAVAP("void"))
            .build();

    static Map<String, IRI> arrayTypeCache = new HashMap<>();

    private static IRI getTypeIndividual(Model model, String typeName) {
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
            IRI typeIri = classIri(elementClassName + new String(new char[dims]).replace("\0", "[]"));
            model.add(typeIri, JAVAP("dimensions"), Values.literal(dims));
            model.add(typeIri, JAVAP("arrayOf"), classIri(elementClassName));
            return typeIri;
        } else {
            // extract `name' from `Lname;'
            return classIri(typeName.substring(1, typeName.length() - 1));
        }
    }

	private static void constructMethod(Model model, String className, FieldOrMethodInfo fmInfo) {
		IRI method = methodIri(className, fmInfo.getName(), fmInfo.getDescriptor());
		model.add(classIri(className), JAVAP("hasMember"), method);
		model.add(method, RDF.TYPE, JAVAP("Method"));
		model.add(method, JAVAP("name"), Values.literal(fmInfo.getName()));
		model.add(method, RDFS.LABEL, Values.literal(className + "." + fmInfo.getName() + fmInfo.getDescriptor()));
		model.add(method, JAVAP("descriptor"), Values.literal(fmInfo.getDescriptor()));
        fmInfo.calledMethods.stream()
                .map(mr -> methodIri(mr.getClassName(), mr.getNameAndType().getName(), mr.getNameAndType().getDescriptor()))
                .forEach(iri -> model.add(method, JAVAP("calls"), iri));
        model.add(method, JAVAP("returns"), getTypeIndividual(model, fmInfo.getReturnTypeName()));
		model.add(method, JAVAP("arity"), Values.literal(fmInfo.getArgumentTypeNames().size()));
        for (int i = 0; i < fmInfo.getArgumentTypeNames().size(); ++i) {
            BNode arg = Values.bnode();
            model.add(method, JAVAP("argument"), arg);
            model.add(arg, RDF.TYPE, JAVAP("Argument"));
            model.add(arg, JAVAP("position"), Values.literal(i));
            model.add(arg, JAVAP("hasType"), getTypeIndividual(model, fmInfo.getArgumentTypeNames().get(i)));
		}
		for (AttributeInfo a : fmInfo.attributes) {
			if (!ignoredTypes.contains(a.type)) {
				System.out.println("(ignored method attribute type=" + a.type + ")");
				ignoredTypes.add(a.type);
			}
		}
	}

	private static void constructField(Model model, String className, FieldOrMethodInfo fmInfo) {
		IRI field = fieldIri(className, fmInfo.getName());
		model.add(field, RDF.TYPE, JAVAP("Variable"));
		model.add(field, RDFS.LABEL, Values.literal(className + "." + fmInfo.getName() + ":" + fmInfo.getDescriptor()));
		model.add(field, JAVAP("name"), Values.literal(fmInfo.getName()));
        model.add(field, JAVAP("hasType"), getTypeIndividual(model, fmInfo.getDescriptor()));
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

		model.add(classIri, JAVAP("subClassOf"), classIri(cf.getSuperclassName()));
		cf.getInterfaces().stream()
			.map(ClassToRdf::classIri)
			.forEach(theInterface -> model.add(classIri, JAVAP("implements"), theInterface));

        // TODO add interface and other modifiers, visibility, local variables, throw declarations
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

		RDFWriters.write(m, RDFFormat.TURTLE, namespaces, output);
	}
}
