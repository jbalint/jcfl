package com.jbalint.jcfl;

public class NameAndType extends ConstantPoolInfo {
	public int nameIndex;
	public int descriptorIndex;

	public String getName() {
		return constantPool[nameIndex].asString();
	}

	public String getDescriptor() {
		return constantPool[descriptorIndex].asString();
	}

	@Override
	public String asString() {
		if (getDescriptor().startsWith("(")) {
			return getName() + getDescriptor();
		} else {
			return getName() + ":" + getDescriptor();
		}
	}

	@Override
	public String toString() {
		return "NameAndType: " + asString();
	}
}
