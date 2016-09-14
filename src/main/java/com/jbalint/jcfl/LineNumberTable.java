package com.jbalint.jcfl;

import java.io.IOException;
import java.util.SortedMap;
import java.util.TreeMap;

public class LineNumberTable extends AttributeInfo {
	private static final String TYPE_NAME = LineNumberTable.class.getSimpleName();

	// FROM starting program counter TO line number
	public SortedMap<Integer, Integer> lineNumberTable = new TreeMap<>();

	public static LineNumberTable parse(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
		LineNumberTable info = new LineNumberTable();
		info.type = TYPE_NAME;
		int lineNumberTableLength = is.readUShort();
		for (int i = 0; i < lineNumberTableLength; ++i) {
			int startPc = is.readUShort();
			info.lineNumberTable.put(startPc, is.readUShort());
		}
		return info;
	}
}
