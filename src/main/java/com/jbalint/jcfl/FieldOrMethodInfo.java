package com.jbalint.jcfl;

import java.util.ArrayList;
import java.util.List;

public class FieldOrMethodInfo {
	public char type;
	public int accessFlags;
	public int nameIndex;
	public int descriptorIndex;
	public List<AttributeInfo> attributes = new ArrayList<>();
	public ConstantPoolInfo constantPool[];

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (type == 'F') {
			sb.append("Field: ");
		} else if (type == 'M') {
			sb.append("Method: ");
		}
		sb.append(constantPool[nameIndex].asString());
		sb.append(constantPool[descriptorIndex].asString());
		return sb.toString();
	}
}

