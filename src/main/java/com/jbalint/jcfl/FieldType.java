package com.jbalint.jcfl;

import static com.jbalint.jcfl.BaseType.L;

/**
 * FieldType as declared by JLS
 */
public class FieldType {

	public int arrayDimensions = 0;

	/**
	 * Class name, if {@code baseType == L}
	 */
	public String className;

	public BaseType baseType;

	/**
	 * Construct a FieldType descriptor. This must be the same as it is represented
	 * in the class file
	 */
	@Override
	public String toString() {
		StringBuilder typeString = new StringBuilder();
		for (int i = 0; i < arrayDimensions; i++) {
			typeString.append("[");
		}
		switch (baseType) {
			case L:
				typeString.append("L").append(ClassBinaryName.classNameToBinaryName(className)).append(";");
				break;
			default:
				typeString.append(baseType.name());
		}
		return typeString.toString();
	}

	public static FieldType fromDescriptorString(String s) {
		FieldType fieldType = new FieldType();
		int pos = 0;
		while (s.charAt(pos) == '[') {
			fieldType.arrayDimensions++;
			pos++;
		}
		char baseTypeChar = s.charAt(pos);
		// TODO : NOTE: this L; containment is part of the descriptor representation
		if (baseTypeChar == 'L') {
			fieldType.className = ClassBinaryName.binaryNameToClassName(s.substring(pos + 1, s.length() - 1));
			fieldType.baseType = L;
		}
		else {
			fieldType.baseType = BaseType.valueOf(new String(new char[] {baseTypeChar}));
		}
		return fieldType;
	}
}
