package com.jbalint.jcfl;

/**
 * Created by jbalint on 2020/01/08.
 */
public class ClassBinaryName {

	/**
	 * Transform a "binary" name to a class name
	 *
	 * This can be one of the following:
	 * * A class name (without L;)
	 * * An array type (which ends with a BaseType "descriptor"(?) which is a single char
	 *   or class name (with L;)
	 */
	public static String binaryNameToClassName(String binaryName) {
		if (binaryName.startsWith("[") && binaryName.endsWith(";")) {
			binaryName = binaryName.replaceAll("\\[L", "[").replaceAll(";$", "");
		}
		return binaryName.replaceAll("/", ".");
	}

	public static String classNameToBinaryName(String className) {
		return className.replaceAll("\\.", "/");
	}
}
