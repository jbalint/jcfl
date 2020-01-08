package com.jbalint.jcfl;

/**
 * Created by jbalint on 2020/01/08.
 */
public class ReturnDescriptor {

	public FieldType returnType;

	public boolean isVoid() {
		return returnType == null;
	}

	@Override
	public String toString() {
		if (isVoid()) {
			return "V";
		}
		return returnType.toString();
	}

	static ReturnDescriptor fromString(String s) {
		ReturnDescriptor returnDescriptor = new ReturnDescriptor();
		if (!"V".equals(s)) {
			returnDescriptor.returnType = FieldType.fromDescriptorString(s);
		}
		return returnDescriptor;
	}
}
