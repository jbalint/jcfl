
package com.jbalint.jora.proto;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.complexible.common.openrdf.model.Models2;
import com.jbalint.jcfl.ClassFile;
import com.jbalint.jcfl.Loader;
import org.openrdf.model.Model;

/**
 * Read a JAR file and convert all classes to javap RDF
 * representation.
 */
public class JarToRdf {
	public static void main(String args[]) throws IOException {
		Model model = Models2.newModel();

		JarFile f = new JarFile(args[0]);
		System.err.println("Loading " + f.size() + " classes from " + args[0]);

		// can't do this with stream() because of all the IOException
		for (JarEntry e : Collections.list(f.entries())) {
			if (!e.getName().endsWith(".class")) {
				continue;
			}
			InputStream is = f.getInputStream(e);
			ClassFile cf = Loader.load(is);
			ClassToRdf.toRdf(model, cf);
		}

		String outfile = "output/" + new java.io.File(args[0]).getName() + ".javap.ttl";
		FileOutputStream fos = new FileOutputStream(outfile);
		ClassToRdf.writeN3String(model, fos);
		fos.close();
	}
}
