package com.jbalint.jcfl;

public class DoubleInfo extends ConstantPoolInfo {
	public double value;

	public DoubleInfo() {
		type = InfoType.DOUBLE;
	}

	@Override
	public String asString() {
		return new Double(value).toString();
	}

	@Override
	public String toString() {
		return "Double: " + asString();
	}
}
