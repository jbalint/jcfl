package com.jbalint.jcfl;

import java.io.DataInputStream;
import java.io.InputStream;
import java.io.IOException;

public class UnsignedDataInputStream extends DataInputStream {
	public UnsignedDataInputStream(InputStream is) {
		super(is);
	}

	public int readUShort() throws IOException {
		int val = readShort();
		if (val < 0) {
			val = val & 0xFFFF;
		}
		return val;
	}

	public long readUInt() throws IOException {
		long val = readInt();
		if (val < 0) {
			val = val & 0xFFFFFFFF;
		}
		return val;
	}
}
