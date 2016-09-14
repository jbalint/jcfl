package com.jbalint.jcfl;

import com.google.common.annotations.VisibleForTesting;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Utf8 extends ConstantPoolInfo {
	public byte[] value;

	public Utf8() {
		type = InfoType.UTF8;
	}

	@VisibleForTesting
	public Utf8(String value) {
		this();
		this.value = value.getBytes(StandardCharsets.UTF_8);
	}

	public String asString() {
		return new String(value, Charset.forName("UTF-8"));
	}

	public String toString() {
		return "UTF8: " + asString();
	}
}
