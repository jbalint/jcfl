package com.jbalint.jora.proto;

import java.util.Collections;
import java.io.InputStream;
import java.io.IOException;
import java.util.function.Function;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.jbalint.jcfl.ClassFile;
import com.jbalint.jcfl.Loader;

public class LoadJar {
	public static void main(String args[]) throws IOException {
		Load1 loader = new Load1();

		JarFile f = new JarFile(args[0]);
		System.err.println("Loading " + f.size() + " classes from " + f);
		// can't do this with stream() because of all the IOException
		for (JarEntry e : Collections.list(f.entries())) {
			if (!e.getName().endsWith(".class")) {
				continue;
			}
			InputStream is = f.getInputStream(e);
			ClassFile cf = Loader.load(is);
			loader.load(cf);
		}
	}
}
