package com.jbalint.jcfl;

/**
 * Created by jbalint on 2020/01/08.
 */
public enum BaseType {
	B("byte"),
	C("char"),
	D("double"),
	F("float"),
	I("int"),
	J("long"),
	S("short"),
	Z("boolean"),
	L("instance"),
//	ARRAY("array"),
	;

	public final String typeName;

	BaseType(String typeName) {
		this.typeName = typeName;
	}
}
