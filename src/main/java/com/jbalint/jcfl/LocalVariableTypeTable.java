package com.jbalint.jcfl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalVariableTypeTable extends AttributeInfo {
	public static final String TYPE_NAME = LocalVariableTypeTable.class.getSimpleName();

	public static class LocalVariableType {
		public int startPc;
		public int length;
		public String name;
		public String signature;
		public int index;
	}

	public List<LocalVariableType> localVariableTypeTable = new ArrayList<>();

	public static LocalVariableTypeTable parse(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
		LocalVariableTypeTable info = new LocalVariableTypeTable();
		info.type = TYPE_NAME;
		int localVariableTypeTableLength = is.readUShort();
		for (int i = 0; i < localVariableTypeTableLength; ++i) {
			LocalVariableTypeTable.LocalVariableType t = new LocalVariableTypeTable.LocalVariableType();
			t.startPc = is.readUShort();
			t.length = is.readUShort();
			t.name = constantPool[is.readUShort()].asString();
			t.signature = constantPool[is.readUShort()].asString();
			t.index = is.readUShort();
			info.localVariableTypeTable.add(t);
		}
		return info;
	}
}