package com.jbalint.jcfl;

public class Fieldref extends ConstantPoolInfo {
	public int classIndex;
	public int nameAndTypeIndex;

	@Override
	public String asString() {
		return constantPool[classIndex].asString() + "." + constantPool[nameAndTypeIndex].asString();
	}

	@Override
	public String toString() {
		return "Fieldref: " + asString();
	}
}
