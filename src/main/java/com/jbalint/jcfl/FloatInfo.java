package com.jbalint.jcfl;

public class FloatInfo extends ConstantPoolInfo {
	public float value;

	public FloatInfo() {
		type = InfoType.FLOAT;
	}

	public String asString() {
		return new Float(value).toString();
	}

	@Override
	public String toString() {
		return "Float: " + asString();
	}
}
