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

	public NameAndType getNameAndType() {
		return (NameAndType) constantPool[nameAndTypeIndex];
	}

	@Override
	public String asString() {
		return getClassName() + "." + getNameAndType().asString();
	}

	@Override
	public String toString() {
		return "Methodref: " + asString();
	}
}
