package com.jbalint.jcfl;

public class MethodHandle extends ConstantPoolInfo {
	public byte referenceKind;
	public int referenceIndex;

	public MethodHandle() {
		type = InfoType.METHOD_HANDLE;
	}
}
