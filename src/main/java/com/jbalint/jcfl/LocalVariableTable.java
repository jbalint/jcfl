package com.jbalint.jcfl;

import java.util.ArrayList;
import java.util.List;

public class LocalVariableTable extends AttributeInfo {
	public static class LocalVariable {
		public int startPc;
		public int length;
		public String name;
		public String descriptor;
		public int index;
	}
	public List<LocalVariable> localVariableTable = new ArrayList<>();
}
