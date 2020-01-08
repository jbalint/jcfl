package com.jbalint.jcfl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jbalint on 2020/01/07.
 */
public class RuntimeAnnotations extends AttributeInfo {
	public boolean visible = true;
	public List<RuntimeAnnotation> annotations = new ArrayList<>();

	@Override
	public <T> T accept(AttributeVisitor<T> visitor) {
		annotations.forEach(visitor::visit);
		return visitor.visit(this);
	}

	static RuntimeAnnotations parse(ConstantPoolInfo constantPool[], UnsignedDataInputStream is, boolean visible) throws IOException {
		RuntimeAnnotations info = new RuntimeAnnotations();
		info.visible = visible;
		info.type = RuntimeAnnotations.class.getSimpleName();
		int numAnnotations = is.readUShort();
		for (int i = 0; i < numAnnotations; i++) {
			info.annotations.add(RuntimeAnnotation.parse(constantPool, is));
		}
		return info;
	}
}
