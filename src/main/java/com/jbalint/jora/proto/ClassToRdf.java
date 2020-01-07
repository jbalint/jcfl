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
import com.stardog.stark.io.RDFFormat;
import com.stardog.stark.io.RDFFormats;
import com.stardog.stark.io.RDFWriters;
import com.stardog.stark.vocabs.RDF;
import com.stardog.stark.vocabs.RDFS;

import com.google.common.collect.ImmutableMap;
import com.jbalint.jcfl.AttributeInfo;
import com.jbalint.jcfl.AttributeVisitor;
import com.jbalint.jcfl.ClassFile;
import com.jbalint.jcfl.ClassInfo;
import com.jbalint.jcfl.Code;
import com.jbalint.jcfl.ConstantPoolInfo;
import com.jbalint.jcfl.ConstantValue;
import com.jbalint.jcfl.Deprecated;
import com.jbalint.jcfl.DoubleInfo;
import com.jbalint.jcfl.Exceptions;
import com.jbalint.jcfl.FieldOrMethodInfo;
import com.jbalint.jcfl.FloatInfo;
import com.jbalint.jcfl.InnerClasses;
import com.jbalint.jcfl.IntegerInfo;
import com.jbalint.jcfl.LineNumberTable;
import com.jbalint.jcfl.LocalVariableTable;
import com.jbalint.jcfl.LocalVariableTypeTable;
import com.jbalint.jcfl.LongInfo;
import com.jbalint.jcfl.SourceFile;

/**
 * Serialize a (parsed) Class to RDF
 */
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
            IRI typeIri = classIri(elementClassName + new String(new char[dims]).replace("\0", "[]"));
            model.add(Values.statement(typeIri, JAVAP("dimensions"), Values.literal(dims)));
            model.add(Values.statement(typeIri, JAVAP("arrayOf"), classIri(elementClassName)));
            return typeIri;
        } else {
            // extract `name' from `Lname;'
            return classIri(typeName.substring(1, typeName.length() - 1));
        }
    }

	private static void constructMethod(Set<Statement> model, String className, FieldOrMethodInfo fmInfo) {
		IRI method = methodIri(className, fmInfo.getName(), fmInfo.getDescriptor());
		model.add(Values.statement(classIri(className), JAVAP("hasMember"), method));
		model.add(Values.statement(method, RDF.TYPE, JAVAP("Method")));
		model.add(Values.statement(method, JAVAP("name"), Values.literal(fmInfo.getName())));
		model.add(Values.statement(method, RDFS.LABEL, Values.literal(className + "." + fmInfo.getName() + fmInfo.getDescriptor())));
		model.add(Values.statement(method, JAVAP("descriptor"), Values.literal(fmInfo.getDescriptor())));
        fmInfo.calledMethods.stream()
                .map(mr -> methodIri(mr.getClassName(), mr.getNameAndType().getName(), mr.getNameAndType().getDescriptor()))
                .forEach(iri -> model.add(Values.statement(method, JAVAP("calls"), iri)));
        model.add(Values.statement(method, JAVAP("returns"), getTypeIndividual(model, fmInfo.getReturnTypeName())));
		model.add(Values.statement(method, JAVAP("arity"), Values.literal(fmInfo.getArgumentTypeNames().size())));
        for (int i = 0; i < fmInfo.getArgumentTypeNames().size(); ++i) {
            BNode arg = Values.bnode();
            model.add(Values.statement(method, JAVAP("argument"), arg));
            model.add(Values.statement(arg, RDF.TYPE, JAVAP("Argument")));
            model.add(Values.statement(arg, JAVAP("position"), Values.literal(i)));
            model.add(Values.statement(arg, JAVAP("hasType"), getTypeIndividual(model, fmInfo.getArgumentTypeNames().get(i))));
		}
		for (AttributeInfo a : fmInfo.attributes) {
            a.accept(new AttributeConstructor(model, method));
		}
	}

	private static void constructField(Set<Statement> model, String className, FieldOrMethodInfo fmInfo) {
		IRI field = fieldIri(className, fmInfo.getName());
		model.add(Values.statement(classIri(className), JAVAP("hasMember"), field));
		model.add(Values.statement(field, RDF.TYPE, JAVAP("Variable")));
		model.add(Values.statement(field, RDFS.LABEL, Values.literal(className + "." + fmInfo.getName() + ":" + fmInfo.getDescriptor())));
		model.add(Values.statement(field, JAVAP("name"), Values.literal(fmInfo.getName())));
        model.add(Values.statement(field, JAVAP("hasType"), getTypeIndividual(model, fmInfo.getDescriptor())));
        // TODO type
		for (AttributeInfo a : fmInfo.attributes) {
			if (a.type.equals("ConstantValue")) {
				ConstantPoolInfo cv = ((ConstantValue) a).constantValue;
				if (cv.type == ConstantPoolInfo.InfoType.STRING) {
					model.add(Values.statement(field, JAVAP("initialValue"), Values.literal(cv.asString())));
				} else if (cv.type == ConstantPoolInfo.InfoType.INTEGER) {
					model.add(Values.statement(field, JAVAP("initialValue"), Values.literal(((IntegerInfo) cv).value)));
				} else if (cv.type == ConstantPoolInfo.InfoType.FLOAT) {
					model.add(Values.statement(field, JAVAP("initialValue"), Values.literal(((FloatInfo) cv).value)));
				} else if (cv.type == ConstantPoolInfo.InfoType.LONG) {
					model.add(Values.statement(field, JAVAP("initialValue"), Values.literal(((LongInfo) cv).value)));
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
					model.add(Values.statement(field, JAVAP("initialValue"), l));

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
		IRI classIri = classIri(cf.getClassName());
		model.add(Values.statement(classIri, RDF.TYPE, JAVAP("Class")));
		model.add(Values.statement(classIri, RDFS.LABEL, Values.literal(cf.getClassName())));
		model.add(Values.statement(classIri, JAVAP("name"), Values.literal(cf.getClassName())));

		model.add(Values.statement(classIri, JAVAP("subClassOf"), classIri(cf.getSuperclassName())));
		cf.getInterfaces().stream()
			.map(ClassToRdf::classIri)
			.forEach(theInterface -> model.add(Values.statement(classIri, JAVAP("implements"), theInterface)));

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

	public static String toN3String(Set<Statement> m) {

		List<Namespace> namespaces = new ArrayList<>();
		Namespaces.EXTENDED.forEach(namespaces::add);
		namespaces.add(Values.namespace("javap", "http://jbalint/javap#"));

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		RDFWriters.write(output, RDFFormats.PRETTY_TURTLE, m, namespaces);
		return new String(output.toByteArray());
	}

	public static void writeN3String(Set<Statement> m, OutputStream output) {

		List<Namespace> namespaces = new ArrayList<>();
		Namespaces.EXTENDED.forEach(namespaces::add);
		namespaces.add(Values.namespace("javap", "http://jbalint/javap#"));

		RDFWriters.write(output, RDFFormats.PRETTY_TURTLE, m, namespaces);
	}

	private static class AttributeConstructor implements AttributeVisitor {

		private final Set<Statement> mModel;

		private final IRI mMethod;

		public AttributeConstructor(final Set<Statement> model, final IRI theMethod) {
			mModel = model;
			mMethod = theMethod;
		}

		@Override
		public Object visit(final ConstantValue attribute) {
			return null;
		}

		@Override
		public Object visit(final Code attribute) {
			return null;
		}

		@Override
		public Object visit(final Deprecated attribute) {
			mModel.add(Values.statement(mMethod, RDF.TYPE, JAVAP("Deprecated")));
			return null;
		}

		@Override
		public Object visit(final InnerClasses attribute) {
			return null;
		}

		@Override
		public Object visit(final LineNumberTable attribute) {
			return null;
		}

		@Override
		public Object visit(final LocalVariableTable attribute) {
			return null;
		}

		@Override
		public Object visit(final LocalVariableTypeTable attribute) {
			return null;
		}

		@Override
		public Object visit(final SourceFile attribute) {
			mModel.add(Values.statement(mMethod, JAVAP("sourceFile"), Values.literal(attribute.sourceFile)));
			return null;
		}

		@Override
		public Object visit(final Exceptions attribute) {
			for (ClassInfo thrownClass : attribute.exceptions) {
				mModel.add(Values.statement(mMethod, JAVAP("throws"), classIri(thrownClass.asString())));
			}
			return null;
		}
	}
}
