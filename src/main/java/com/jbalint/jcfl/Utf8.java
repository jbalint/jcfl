package com.jbalint.jcfl;

import java.nio.charset.Charset;

public class Utf8 extends ConstantPoolInfo {
	public byte[] value;

	public String asString() {
		return new String(value, Charset.forName("UTF-8"));
	}

	public String toString() {
		return "UTF8: " + asString();
	}
}
