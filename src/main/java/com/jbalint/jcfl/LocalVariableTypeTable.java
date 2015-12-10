package com.jbalint.jcfl;

import java.util.ArrayList;
import java.util.List;

public class LocalVariableTypeTable extends AttributeInfo {
	public static class LocalVariableType {
		public int startPc;
		public int length;
		public String name;
		public String signature;
		public int index;
	}

	public List<LocalVariableType> localVariableTypeTable = new ArrayList<>();
}
