package com.jbalint.jcfl;

public class DoubleInfo extends ConstantPoolInfo {
	public double value;

	@Override
	public String asString() {
		return new Double(value).toString();
	}

	@Override
	public String toString() {
		return "Double: " + asString();
	}
}
