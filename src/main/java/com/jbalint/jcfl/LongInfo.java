package com.jbalint.jcfl;

public class LongInfo extends ConstantPoolInfo {
	public long value;

	public LongInfo() {
		type = InfoType.LONG;
	}

	@Override
	public String asString() {
		return new Long(value).toString();
	}

	@Override
	public String toString() {
		return "Long: " + asString();
	}
}
