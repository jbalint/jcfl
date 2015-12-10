package com.jbalint.jcfl;

import java.util.ArrayList;
import java.util.List;

public class Code extends AttributeInfo {
	public static class CodeException {
		public int startPc;
		public int endPc;
		public int handlerPc;
		public int catchType;
	}

	public int maxStack;
	public int maxLocals;
	public byte[] code;
	public CodeException exceptionTable[];
	public List<AttributeInfo> attributes = new ArrayList<>();
}
