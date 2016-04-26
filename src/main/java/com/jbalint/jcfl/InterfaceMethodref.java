package com.jbalint.jcfl;

public class InterfaceMethodref extends Methodref {
	public InterfaceMethodref() {
		type = InfoType.INTERFACE_METHOD_REF;
	}

	@Override
	public String toString() {
		return "InterfaceMethodref: " + asString();
	}
}
