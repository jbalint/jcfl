package com.jbalint.jcfl;

import com.stardog.stark.IRI;
import com.stardog.stark.Values;

/**
 * Created by jbalint on 2020/01/08.
 */
public class JcflVocab {

	public static final String NS = "http://jbalint/javap#";

	public static IRI JAVAP(String name) {
		return Values.iri(NS + java.net.URLEncoder.encode(name));
	}

	public static IRI classIri(String className) {
		return JAVAP(className);
	}
}
