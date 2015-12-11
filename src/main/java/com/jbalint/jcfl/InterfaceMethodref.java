package com.jbalint.jcfl;

public class InterfaceMethodref extends ConstantPoolInfo {
	public int classIndex;
	public int nameAndTypeIndex;

	public InterfaceMethodref() {
		type = InfoType.INTERFACE_METHOD_REF;
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
