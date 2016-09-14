package com.jbalint.jcfl;

import java.io.IOException;

public class Signature extends AttributeInfo {
	private static final String TYPE_NAME = Signature.class.getSimpleName();

	public String signature;

	@Override
	public String toString() {
		return "Signature: " + signature;
	}

	public static Signature parse(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
		Signature info = new Signature();
		info.type = TYPE_NAME;
		info.signature = constantPool[is.readUShort()].asString();
		return info;
	}
}