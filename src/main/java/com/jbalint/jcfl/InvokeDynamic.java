package com.jbalint.jcfl;

public class InvokeDynamic extends ConstantPoolInfo {
	public int bootstrapMethodAttrIndex;
	public int nameAndTypeIndex;

	public InvokeDynamic() {
		type = InfoType.INVOKE_DYNAMIC;
	}
}
