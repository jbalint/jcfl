package com.jbalint.jcfl;

public class Methodref extends ConstantPoolInfo {
	public int classIndex;
	public int nameAndTypeIndex;

	public Methodref() {
		type = InfoType.METHOD_REF;
	}

	public String getClassName() {
		return constantPool[classIndex].asString();
	}

	public String getNameAndType() {
		return constantPool[nameAndTypeIndex].asString();
	}

	@Override
	public String asString() {
		return getClassName() + "." + getNameAndType();
	}

	@Override
	public String toString() {
		return "Methodref: " + asString();
	}
}
