package com.jbalint.jcfl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalVariableTable extends AttributeInfo {
	private static final String TYPE_NAME = LocalVariableTable.class.getSimpleName();

	public static class LocalVariable {
		public int startPc;
		public int length;
		public String name;
		public String descriptor;
		public int index;
	}
	public List<LocalVariable> localVariableTable = new ArrayList<>();

	public static LocalVariableTable parse(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
		LocalVariableTable info = new LocalVariableTable();
		info.type = TYPE_NAME;
		int localVariableTableLength = is.readUShort();
		for (int i = 0; i < localVariableTableLength; ++i) {
			LocalVariableTable.LocalVariable v = new LocalVariableTable.LocalVariable();
			v.startPc = is.readUShort();
			v.length = is.readUShort();
			v.name = constantPool[is.readUShort()].asString();
			v.descriptor = constantPool[is.readUShort()].asString();
			v.index = is.readUShort();
			info.localVariableTable.add(v);
		}
		return info;
	}

    @Override
    public <T> T accept(AttributeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
