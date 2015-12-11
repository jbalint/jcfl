package com.jbalint.jcfl;

public class IntegerInfo extends ConstantPoolInfo {
	public int value;

	public IntegerInfo() {
		type = InfoType.INTEGER;
	}

	@Override
	public String asString() {
		return new Integer(value).toString();
	}

	@Override
	public String toString() {
		return "Integer: " + asString();
	}
}
