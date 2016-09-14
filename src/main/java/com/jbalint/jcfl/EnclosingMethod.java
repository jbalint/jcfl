package com.jbalint.jcfl;

import java.io.IOException;

public class EnclosingMethod extends AttributeInfo {
	public static final String TYPE_NAME = EnclosingMethod.class.getSimpleName();

	public ClassInfo clazz;
	public NameAndType method;

	public static EnclosingMethod parse(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
		EnclosingMethod info = new EnclosingMethod();
		info.type = TYPE_NAME;
		info.clazz = (ClassInfo) constantPool[is.readUShort()];
		// may be null
		info.method = (NameAndType) constantPool[is.readUShort()];
		return info;
	}
}