package com.jbalint.jcfl;

public class Fieldref extends ConstantPoolInfo {
	public int classIndex;
	public int nameAndTypeIndex;

	public Fieldref() {
		type = InfoType.FIELD_REF;
	}

	@Override
	public String asString() {
		return constantPool[classIndex].asString() + "." + constantPool[nameAndTypeIndex].asString();
	}

	@Override
	public String toString() {
		return "Fieldref: " + asString();
	}
}
