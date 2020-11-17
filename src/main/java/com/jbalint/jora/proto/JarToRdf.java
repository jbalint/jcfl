
package com.jbalint.jora.proto;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.stardog.stark.Statement;

import com.jbalint.jcfl.ClassFile;
import com.jbalint.jcfl.ClassFileParser;

/**
 * Read a JAR file and convert all classes to javap RDF
 * representation.
 */
public class JarToRdf {
	public static void main(String args[]) throws IOException {
		Set<Statement> model = new HashSet<>();

		JarFile f = new JarFile(args[0]);
		System.err.println("Loading " + f.size() + " classes from " + args[0]);

		// can't do this with stream() because of all the IOException
		for (JarEntry e : Collections.list(f.entries())) {
			if (!e.getName().endsWith(".class")) {
				continue;
			}
			InputStream is = f.getInputStream(e);
			ClassFile cf = ClassFileParser.parse(is);
			ClassFileToRdf.toRdf(model, cf);
		}

		String outfile = "output/" + new java.io.File(args[0]).getName() + ".javap.ttl";
		FileOutputStream fos = new FileOutputStream(outfile);
		ClassFileToRdf.writeTurtleString(model, fos);
		fos.close();
	}
}
