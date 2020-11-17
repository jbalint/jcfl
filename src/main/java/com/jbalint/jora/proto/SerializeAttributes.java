package com.jbalint.jora.proto;

import java.util.Set;

import com.stardog.stark.BNode;
import com.stardog.stark.IRI;
import com.stardog.stark.Resource;
import com.stardog.stark.Statement;
import com.stardog.stark.Values;
import com.stardog.stark.vocabs.RDF;

import com.jbalint.jcfl.AttributeVisitor;
import com.jbalint.jcfl.ClassInfo;
import com.jbalint.jcfl.Code;
import com.jbalint.jcfl.ConstantValue;
import com.jbalint.jcfl.Deprecated;
import com.jbalint.jcfl.Exceptions;
import com.jbalint.jcfl.InnerClasses;
import com.jbalint.jcfl.JcflVocab;
import com.jbalint.jcfl.LineNumberTable;
import com.jbalint.jcfl.LocalVariableTable;
import com.jbalint.jcfl.LocalVariableTypeTable;
import com.jbalint.jcfl.RuntimeAnnotation;
import com.jbalint.jcfl.RuntimeAnnotations;
import com.jbalint.jcfl.SourceFile;

/**
 * Serialize attributes to {@link #mModel}
 */
class SerializeAttributes implements AttributeVisitor {

	private final Set<Statement> mModel;

	private final IRI mOwner;

	public SerializeAttributes(final Set<Statement> model, final IRI theMethod) {
		mModel = model;
		mOwner = theMethod;
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
		mModel.add(Values.statement(mOwner, RDF.TYPE, JcflVocab.JAVAP("Deprecated")));
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
		mModel.add(Values.statement(mOwner, JcflVocab.JAVAP("sourceFile"), Values.literal(attribute.sourceFile)));
		return null;
	}

	@Override
	public Object visit(RuntimeAnnotations attribute) {
		return null;
	}

	/**
	 * Add element value to the model. Recursive to support arrays of values
	 */
	private void addElementValue(Resource subject, RuntimeAnnotation.ElementValue elementValue) {
		switch (elementValue.valueType) {
			case ConstantValue:
				// TODO :
				System.err.println("Ignoring " + subject + " " + elementValue);
//				throw new UnsupportedOperationException();
				break;
			case ClassName:
				mModel.add(Values.statement(subject, JcflVocab.JAVAP("hasClass"), JcflVocab.classIri(elementValue.className)));
				break;
			case EnumConstant:
				// TODO :
				System.err.println("Ignoring " + subject + " " + elementValue);
//				throw new UnsupportedOperationException();
				break;
			case Annotation:
				// TODO :
				System.err.println("Ignoring " + subject + " " + elementValue);
//				throw new UnsupportedOperationException();
				break;
			case Array:
				BNode array = Values.bnode();
				mModel.add(Values.statement(subject, JcflVocab.JAVAP("hasArrayValue"), array));
				// TODO : sequence order is discarded
				elementValue.arrayValues.forEach(v -> addElementValue(array, v));
				break;
		}
	}

	@Override
	public Object visit(RuntimeAnnotation attribute) {
		BNode annotation = Values.bnode();
		mModel.add(Values.statement(mOwner, JcflVocab.JAVAP("hasAnnotation"), annotation));
		mModel.add(Values.statement(annotation, JcflVocab.JAVAP("annotationType"), JcflVocab.classIri(attribute.annotationClassName)));
//			mModel.add(Values.statement(annotation, JAVAP("visible"), Values.literal(TODO)));
		attribute.elementValuePairs.forEach((elementName, elementValue) -> {
			BNode node = Values.bnode();
			mModel.add(Values.statement(annotation, JcflVocab.JAVAP("hasElementValue"), node));
			mModel.add(Values.statement(node, JcflVocab.JAVAP("name"), Values.literal(elementName)));
			addElementValue(node, elementValue);
		});
		return null;
	}

	@Override
	public Object visit(final Exceptions attribute) {
		for (ClassInfo thrownClass : attribute.exceptions) {
			mModel.add(Values.statement(mOwner, JcflVocab.JAVAP("throws"), JcflVocab.classIri(thrownClass.asString())));
		}
		return null;
	}
}
