package com.jbalint.jcfl;

import java.io.File;

/**
 * Utility to parse a class file
 */
public class ParseClassFile {

	public static void main(String args[]) throws Exception {
		ClassFile parse = ClassFileParser.parse(new File(args[0]));
		System.err.println(parse);
	}
}
