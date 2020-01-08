package com.jbalint.jcfl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Annotation present in compiled code
 */
public class RuntimeAnnotation extends AttributeInfo {

	public String annotationClassName;
	public Map<String, ElementValue> elementValuePairs = new HashMap<>();

	@Override
	public <T> T accept(AttributeVisitor<T> visitor) {
		return visitor.visit(this);
	}

	public static class ElementValue {
		public ElementValueType valueType;

		/**
		 * Constant value if {@link #valueType} is {@link ElementValueType#ConstantValue}. The
		 * instance will be one of {@link DoubleInfo}, {@link StringInfo}, etc.
		 *
		 * Annotation constants cannot refer to arbitrary Java objects so it must be a primitive
		 * value or a string.
		 */
		public ConstantPoolInfo constantValue;

		public String className;

		public int enumTypeIndex;

		public int enumConstIndex;

		public RuntimeAnnotation annotation;

		public List<ElementValue> arrayValues;

		ElementValue(ElementValueType t) {
			valueType = t;
		}

		@Override
		public String toString() {
			return String.format("ElementValue(%s)", valueType.name());
		}
	}

	public enum ElementValueType {
		ConstantValue,
		ClassName,
		EnumConstant,
		Annotation,
		Array,
	}

	private static ElementValue readElementValue(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
		int elementValueTag = is.read();
		ElementValue v;
		switch (elementValueTag) {
			case 'B':
			case 'C':
			case 'D':
			case 'F':
			case 'I':
			case 'J':
			case 'S':
			case 'Z':
			case 's':
				v = new ElementValue(ElementValueType.ConstantValue);
				v.constantValue = constantPool[is.readUShort()];
				return v;
			case 'e':
				v = new ElementValue(ElementValueType.EnumConstant);
				v.enumTypeIndex = is.readUShort();
				v.enumConstIndex = is.readUShort();
				return v;
			case 'c':
				v = new ElementValue(ElementValueType.ClassName);
				v.className = FieldType.fromDescriptorString(constantPool[is.readUShort()].asString()).className;
				return v;
			case '@':
				// TODO : recurse
				throw new IllegalArgumentException("" + elementValueTag);
			case '[':
				int numVals = is.readUShort();
				ElementValue v1 = new ElementValue(ElementValueType.Array);
				v1.arrayValues = new ArrayList<>();
				for (int i1 = 0; i1 < numVals; i1++) {
					v1.arrayValues.add(readElementValue(constantPool, is));
				}
				return v1;
			default:
				throw new IllegalArgumentException("" + elementValueTag);
		}
	}

	static RuntimeAnnotation parse(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
		RuntimeAnnotation info = new RuntimeAnnotation();
		info.type = RuntimeAnnotation.class.getSimpleName();
		int classNameIndex = is.readUShort();
		info.annotationClassName = FieldType.fromDescriptorString(constantPool[classNameIndex].asString()).className;
		int pairsCount = is.readUShort();
		for (int i = 0; i < pairsCount; i++) {
			String elementName = constantPool[is.readUShort()].asString();
			ElementValue v = readElementValue(constantPool, is);
			info.elementValuePairs.put(elementName, v);
		}
		return info;
	}
}
