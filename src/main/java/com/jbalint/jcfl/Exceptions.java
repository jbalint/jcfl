package com.jbalint.jcfl;

import java.util.ArrayList;
import java.util.List;

public class Exceptions extends AttributeInfo {
	public List<ClassInfo> exceptions = new ArrayList<>();

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Exceptions: [ ");
		for (ClassInfo c : exceptions) {
			sb.append(c.asString()).append(" ");
		}
		sb.append("]");
		return sb.toString();
	}
}
