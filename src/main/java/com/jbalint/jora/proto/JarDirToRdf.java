package com.jbalint.jora.proto;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.complexible.common.openrdf.model.Models2;
import com.google.common.io.Files;
import com.jbalint.jcfl.ClassFile;
import com.jbalint.jcfl.Loader;
import org.openrdf.model.Model;

/**
 * Created by jbalint on 2017/06/10.
 */
public class JarDirToRdf {

	static Model jarToRdf(File rawFile) {
		try {
			Model model = Models2.newModel();
			JarFile f = new JarFile(rawFile);
			System.err.println("Loading " + f.size() + " classes from " + rawFile);

			// can't do this with stream() because of all the IOException
			for (JarEntry e : Collections.list(f.entries())) {
				if (!e.getName().endsWith(".class")) {
					continue;
				}
				InputStream is = f.getInputStream(e);
				ClassFile cf = Loader.load(is);
				ClassToRdf.toRdf(model, cf);
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

		Files.fileTreeTraverser()
		     .postOrderTraversal(dir)
		     .filter(f -> f.isFile() && f.getName().endsWith(".jar"))
		     .transform(JarDirToRdf::jarToRdf)
		     .forEach(m -> ClassToRdf.writeN3String(m, fos));

		fos.close();
	}
}
