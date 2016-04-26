package com.jbalint.jcfl;

public class SourceFile extends AttributeInfo {
	public String sourceFile;

	@Override
	public String toString() {
		return type + ": " + sourceFile;
	}
}
