package com.jbalint.jcfl;

public class ClassInfo extends ConstantPoolInfo {
	public int nameIndex;

	@Override
	public String asString() {
		return constantPool[nameIndex].asString();
	}

	@Override
	public String toString() {
		return "Class: " + asString();
	}
}
