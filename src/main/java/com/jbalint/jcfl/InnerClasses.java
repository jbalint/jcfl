package com.jbalint.jcfl;

import java.util.ArrayList;
import java.util.List;

public class InnerClasses extends AttributeInfo {
	public static class InnerClass {
		public int innerClassInfoIndex;
		public int outerClassInfoIndex;
		public int innerNameIndex;
		public int innerClassAccessFlags;
	}
	public List<InnerClass> classes = new ArrayList<>();
}
