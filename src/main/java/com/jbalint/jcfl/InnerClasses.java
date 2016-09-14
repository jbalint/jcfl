package com.jbalint.jcfl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class InnerClasses extends AttributeInfo {
	public static final String TYPE_NAME = InnerClasses.class.getSimpleName();

	public static class InnerClass {
		public int innerClassInfoIndex;
		public int outerClassInfoIndex;
		public int innerNameIndex;
		public int innerClassAccessFlags;
	}
	public List<InnerClass> classes = new ArrayList<>();

	public static InnerClasses parse(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
		InnerClasses info = new InnerClasses();
		info.type = TYPE_NAME;
		int numberOfClasses = is.readUShort();
		for (int i = 0; i < numberOfClasses; ++i) {
			InnerClasses.InnerClass inner = new InnerClasses.InnerClass();
			inner.innerClassInfoIndex = is.readUShort();
			inner.outerClassInfoIndex = is.readUShort();
			inner.innerNameIndex = is.readUShort();
			inner.innerClassAccessFlags = is.readUShort();
			info.classes.add(inner);
		}
		return info;
	}
}
