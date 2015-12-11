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

	public String getName() {
		return constantPool[nameIndex].asString();
	}

	public String getDescriptor() {
		return constantPool[descriptorIndex].asString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (type == 'F') {
			sb.append("Field: ").append(getName()).append(" : ").append(getDescriptor());
		} else if (type == 'M') {
			sb.append("Method: ").append(getName()).append(getDescriptor());
		}
		for (AttributeInfo a : attributes) {
			sb.append("\n    ").append(a);
		}
		return sb.toString();
	}
}

