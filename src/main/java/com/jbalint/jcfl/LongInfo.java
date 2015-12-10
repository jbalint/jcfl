package com.jbalint.jcfl;

public class LongInfo extends ConstantPoolInfo {
	public long value;

	@Override
	public String asString() {
		return new Long(value).toString();
	}

	@Override
	public String toString() {
		return "Long: " + asString();
	}
}
