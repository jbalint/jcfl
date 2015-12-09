package com.jbalint.jcfl;

import java.util.ArrayList;
import java.util.List;

public class ClassFile {
	public static class AttributeInfo {
		public short attributeNameIndex;
	}

	public static class FieldOrMethodInfo {
		public char type;
		public short accessFlags;
		public short nameIndex;
		public short descriptorIndex;
		public AttributeInfo[] attributes;
	}

	public int magic;
	public short version; // major version, we ignore minor as it's not used in practice
	public List<ConstantPoolInfo> constantPool = new ArrayList<>();
	public short accessFlags;
	public short thisClassIndex;
	public short superClassIndex;
	public List interfaces = new ArrayList<>();
	public List<FieldOrMethodInfo> fields = new ArrayList<>();
	public List<FieldOrMethodInfo> methods = new ArrayList<>();
	public List<AttributeInfo> attributes = new ArrayList<>();
}
