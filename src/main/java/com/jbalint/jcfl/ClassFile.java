package com.jbalint.jcfl;

import java.util.ArrayList;
import java.util.List;

public class ClassFile {
	public int magic;
	public int version;
	public ConstantPoolInfo constantPool[];
	public int accessFlags;
	public int thisClassIndex;
	public int superClassIndex;
	public List interfaces = new ArrayList<>();
	public List<FieldOrMethodInfo> fieldsAndMethods = new ArrayList<>();
	public List<AttributeInfo> attributes = new ArrayList<>();

	public String getClassName() {
		return constantPool[thisClassIndex].asString().replaceAll("/", ".");
	}

	public String getSuperClassName() {
		return constantPool[superClassIndex].asString().replaceAll("/", ".");
	}

	public String getSimpleName() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Java class file:");
		sb.append("\n  version=" + version);
		sb.append("\n  class=" + getClassName());
		sb.append("\n  superclass=" + getSuperClassName());
		for (FieldOrMethodInfo fm : fieldsAndMethods) {
			sb.append("\n  " + fm);
		}
		for (AttributeInfo a : attributes) {
			sb.append("\n  " + a);
		}
		sb.append("\n");
		return sb.toString();
	}
}
