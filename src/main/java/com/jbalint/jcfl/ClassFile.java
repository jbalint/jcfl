package com.jbalint.jcfl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClassFile {
	public int magic;
	public int version;
	public ConstantPoolInfo constantPool[];
	public int accessFlags;
	public int thisClassIndex;
	public int superclassIndex;
	public List<ClassInfo> interfaces = new ArrayList<>();
	public List<FieldOrMethodInfo> fieldsAndMethods = new ArrayList<>();
	public List<AttributeInfo> attributes = new ArrayList<>();

	public String getClassName() {
		return constantPool[thisClassIndex].asString();
	}

	public String getSuperclassName() {
		if (constantPool[superclassIndex] == null) {
			// this should only happen for java.lang.Object. just make it point to itself.
			return "java/lang/Object;";
		} else {
			return constantPool[superclassIndex].asString();
		}
	}

	public List<String> getInterfaces() {
		return interfaces.stream()
			.map(ci -> ci.asString())
			.collect(Collectors.toList());
	}

	public String getSimpleName() {
		throw new UnsupportedOperationException("TODO");
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Java class file:");
		sb.append("\n  version=" + version);
		sb.append("\n  class=" + getClassName());
		sb.append("\n  superclass=" + getSuperclassName());
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
