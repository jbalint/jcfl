package com.jbalint.jcfl;

import java.io.IOException;

public class ConstantValue extends AttributeInfo {
    public static final String TYPE_NAME = ConstantValue.class.getSimpleName();

	public ConstantPoolInfo constantValue;

    public static ConstantValue parse(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
        ConstantValue info = new ConstantValue();
        info.type = TYPE_NAME;
        info.constantValue = constantPool[is.readUShort()];
        return info;
    }

	@Override
	public String toString() {
		return "Constant: " + constantValue.asString();
	}

    @Override
    public <T> T accept(AttributeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
