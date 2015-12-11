package com.jbalint.jcfl;

public class StringInfo extends ConstantPoolInfo {
	public int index;

	public StringInfo() {
		type = InfoType.STRING;
	}

	public String asString() {
		return constantPool[index].asString();
	}
}
