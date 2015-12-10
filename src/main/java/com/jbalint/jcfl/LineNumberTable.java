package com.jbalint.jcfl;

import java.util.SortedMap;
import java.util.TreeMap;

public class LineNumberTable extends AttributeInfo {
	// FROM starting program counter TO line number
	public SortedMap<Integer, Integer> lineNumberTable = new TreeMap<>();
}
