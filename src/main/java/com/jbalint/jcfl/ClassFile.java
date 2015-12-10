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
	public List<FieldOrMethodInfo> fields = new ArrayList<>();
	public List<FieldOrMethodInfo> methods = new ArrayList<>();
	public List<AttributeInfo> attributes = new ArrayList<>();
}
