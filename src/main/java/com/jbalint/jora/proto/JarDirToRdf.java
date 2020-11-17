package com.jbalint.jora.proto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.complexible.common.base.Streams;
import com.stardog.stark.Statement;

import com.google.common.io.Files;
import com.jbalint.jcfl.ClassFile;
import com.jbalint.jcfl.ClassFileParser;

/**
 * Parse a dir containing JARs to RDF
 */
public class JarDirToRdf {

	static Set<Statement> jarToRdf(File rawFile) {
		try {
			Set<Statement> model = new HashSet<>();
			JarFile f = new JarFile(rawFile);
			System.err.println("Loading " + f.size() + " classes from " + rawFile);

			// can't do this with stream() because of all the IOException
			for (JarEntry e : Collections.list(f.entries())) {
				if (!e.getName().endsWith(".class")) {
					continue;
				}
				InputStream is = f.getInputStream(e);
				ClassFile cf = ClassFileParser.parse(is);
				ClassFileToRdf.toRdf(model, cf);
			}

			return model;
		}
		catch (IOException theE) {
			throw new UncheckedIOException(theE);
		}
	}

	public static void main(String args[]) throws IOException {

		File dir = new File(args[0]);
		String outfile = args[1];
		System.err.println("Analyzing " + dir.getName() + " with output to " + args[1].toString());
		FileOutputStream fos = new FileOutputStream(outfile);

		Streams.stream(Files.fileTraverser()
		                    .depthFirstPostOrder(dir))
		       .filter(f -> f.isFile() && f.getName().endsWith(".jar"))
		       .map(JarDirToRdf::jarToRdf)
		       .forEach(m -> ClassFileToRdf.writeTurtleString(m, fos));

		fos.close();
	}
}
