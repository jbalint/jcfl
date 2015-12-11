package com.jbalint.jcfl;

public class ConstantValue extends AttributeInfo {
	public ConstantPoolInfo constantValue;

	@Override
	public String toString() {
		return "Constant: " + constantValue.asString();
	}
}
