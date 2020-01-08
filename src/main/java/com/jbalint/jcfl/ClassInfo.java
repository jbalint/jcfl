package com.jbalint.jcfl;

public class ClassInfo extends ConstantPoolInfo {
	public int nameIndex;

	public ClassInfo() {
		type = InfoType.CLASS;
	}

	@Override
	public String asString() {
		return ClassBinaryName.binaryNameToClassName(constantPool[nameIndex].asString());
	}

	@Override
	public String toString() {
		return "Class: " + asString();
	}
}
