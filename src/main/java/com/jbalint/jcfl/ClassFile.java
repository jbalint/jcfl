package com.jbalint.jcfl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassFile {
	public int magic;
	public int version;
	public ConstantPoolInfo constantPool[];
	public int accessFlags;
	public String className;
	public String superclassName;
	public List<ClassInfo> interfaces = new ArrayList<>();
	public List<FieldOrMethodInfo> fieldsAndMethods = new ArrayList<>();
	public List<AttributeInfo> attributes = new ArrayList<>();

	public List<String> getInterfaces() {
		return interfaces.stream()
			.map(ClassInfo::asString)
			.collect(Collectors.toList());
	}

	public String getSimpleName() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Java class file:");
		sb.append("\n  version=" + version);
		sb.append("\n  class=" + className);
		sb.append("\n  superclass=" + superclassName);
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
