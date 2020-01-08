package com.jbalint.jcfl;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by jbalint on 2020/01/08.
 */
public class Descriptor {

	public ReturnDescriptor returnType;

	public List<FieldType> argumentTypes = new LinkedList<>();

	/**
	 * Parse the descriptor string, e.g {@code ([Lcom/jbalint/jcfl/ConstantPoolInfo;Lcom/jbalint/jcfl/UnsignedDataInputStream;)Lcom/jbalint/jcfl/ConstantValue;}
	 */
	public static Descriptor parse(String desc) {
		Descriptor descriptor = new Descriptor();

		// start at 1 to skip first opening paren
		for (int i = 1; i < desc.length(); /* increment inline */) {
			if (desc.charAt(i) == ')') {
				i++;
				descriptor.returnType = ReturnDescriptor.fromString(desc.substring(i));
				break;
			} else {
				int startPos = i;
				int len = 0;
				while (desc.charAt(startPos + len) == '[') {
					len++;
				}
				if (desc.charAt(startPos + len) == 'L') {
					int endIndex = desc.indexOf(';', startPos);
					descriptor.argumentTypes.add(FieldType.fromDescriptorString(desc.substring(startPos, endIndex + 1)));
					i = endIndex + 1;
				}
				else {
					descriptor.argumentTypes.add(FieldType.fromDescriptorString(desc.substring(startPos, startPos + len + 1)));
					i = i + len + 1;
				}
			}
		}

		return descriptor;
	}
}
