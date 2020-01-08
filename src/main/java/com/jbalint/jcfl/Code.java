package com.jbalint.jcfl;

import java.io.IOException;
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

    public static Code parse(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
        Code info = new Code();
        info.type = "Code";
        info.maxStack = is.readUShort();
        info.maxLocals = is.readUShort();
        long codeLength = is.readUInt();
        info.code = new byte[(int) codeLength];
        is.readFully(info.code);
        int exceptionTableLength = is.readUShort();
        info.exceptionTable = new CodeException[exceptionTableLength];
        for (int i = 0; i < exceptionTableLength; ++i) {
            CodeException exc = new CodeException();
            exc.startPc = is.readUShort();
            exc.endPc = is.readUShort();
            exc.handlerPc = is.readUShort();
            exc.catchType = is.readUShort();
            info.exceptionTable[i] = exc;
        }
        int attributesCount = is.readUShort();
        for (int i = 0; i < attributesCount; ++i) {
	        AttributeInfo attr = AttributeInfo.parseAttribute(constantPool, is);
	        if (attr != null) {
		        info.attributes.add(attr);
	        }
        }
        return info;
    }

    @Override
    public <T> T accept(AttributeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
