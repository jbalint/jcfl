package com.jbalint.jcfl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Exceptions extends AttributeInfo {
	public List<ClassInfo> exceptions = new ArrayList<>();

	public static Exceptions parse(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
		Exceptions info = new Exceptions();
		info.type = "Exceptions";
		int numberOfExceptions = is.readUShort();
		for (int i = 0; i < numberOfExceptions; ++i) {
			info.exceptions.add((ClassInfo) constantPool[is.readUShort()]);
		}
		return info;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Exceptions: [ ");
		for (ClassInfo c : exceptions) {
			sb.append(c.asString()).append(" ");
		}
		sb.append("]");
		return sb.toString();
	}
}
