package com.jbalint.jcfl;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import static com.jbalint.jcfl.ConstantPoolInfo.InfoType.*;

public class Loader {
	private static boolean debug = false;

	private static ConstantPoolInfo parseConstantPoolInfo(UnsignedDataInputStream is) throws IOException {
		int tag = is.read();
		if (tag == CLASS.tag) {
			ClassInfo info = new ClassInfo();
			info.nameIndex = is.readUShort();
			return info;
		} else if (tag == FIELD_REF.tag) {
			Fieldref info = new Fieldref();
			info.classIndex = is.readUShort();
			info.nameAndTypeIndex = is.readUShort();
			return info;
		} else if (tag == METHOD_REF.tag) {
			Methodref info = new Methodref();
			info.classIndex = is.readUShort();
			info.nameAndTypeIndex = is.readUShort();
			return info;
		} else if (tag == INTERFACE_METHOD_REF.tag) {
			InterfaceMethodref info = new InterfaceMethodref();
			info.classIndex = is.readUShort();
			info.nameAndTypeIndex = is.readUShort();
			return info;
		} else if (tag == STRING.tag) {
			StringInfo info = new StringInfo();
			info.index = is.readUShort();
			return info;
		} else if (tag == INTEGER.tag) {
			IntegerInfo info = new IntegerInfo();
			info.value = is.readInt();
			return info;
		} else if (tag == FLOAT.tag) {
			FloatInfo info = new FloatInfo();
			info.value = is.readFloat();
			return info;
		} else if (tag == LONG.tag) {
			LongInfo info = new LongInfo();
			info.value = is.readLong();
			return info;
		} else if (tag == DOUBLE.tag) {
			DoubleInfo info = new DoubleInfo();
			info.value = is.readDouble();
			return info;
		} else if (tag == NAME_AND_TYPE.tag) {
			NameAndType info = new NameAndType();
			info.nameIndex = is.readUShort();
			info.descriptorIndex = is.readUShort();
			return info;
		} else if (tag == UTF8.tag) {
			Utf8 info = new Utf8();
			int len = is.readUShort();
			info.value = new byte[len];
			is.readFully(info.value);
			return info;
		} else if (tag == METHOD_HANDLE.tag) {
			MethodHandle info = new MethodHandle();
			info.referenceKind = (byte) is.read();
			info.referenceIndex = is.readUShort();
			return info;
		} else if (tag == METHOD_TYPE.tag) {
			MethodType info = new MethodType();
			info.descriptorIndex = is.readUShort();
			return info;
        } else if (tag == INVOKE_DYNAMIC.tag) {
			InvokeDynamic info = new InvokeDynamic();
			info.bootstrapMethodAttrIndex = is.readUShort();
			info.nameAndTypeIndex = is.readUShort();
			return info;
		}
		throw new IllegalArgumentException("Unknown constant pool tag: " + tag);
	}

	private static FieldOrMethodInfo parseFieldOrMethodInfo(ClassFile cf, UnsignedDataInputStream is, char type) throws IOException {
		FieldOrMethodInfo info = new FieldOrMethodInfo();
        info.cf = cf;
		info.type = type;
		info.accessFlags = is.readUShort();
		info.nameIndex = is.readUShort();
		info.descriptorIndex = is.readUShort();
		info.constantPool = cf.constantPool;
		int attributesCount = is.readUShort();
		for (int i = 0; i < attributesCount; ++i) {
			AttributeInfo a = AttributeInfo.parseAttribute(cf.constantPool, is);
			// some are currently not parsed
			if (a == null) {
				continue;
			}
			if ("Code".equals(a.type)) {
				info.codeAttr = (Code) a;
			} else {
				info.attributes.add(a);
			}
		}
		return info;
	}

	public static ClassFile load(File file) throws IOException {
		return load(new FileInputStream(file));
	}

	/**
	 * Load and parse a Java class file.
	 */
	// alias shell='java -Xcheck:jni -esa -agentlib:yt -classpath build/classes/main java.util.prefs.Base64'
	public static ClassFile load(InputStream fileInputStream) throws IOException {
		ClassFile cf = new ClassFile();
		try (UnsignedDataInputStream is = new UnsignedDataInputStream(fileInputStream)) {
			cf.magic = is.readInt();
			is.readUShort(); // ignored
			cf.version = is.readUShort();
			int constantPoolCount = is.readUShort();
			cf.constantPool = new ConstantPoolInfo[constantPoolCount];
			for (int i = 1; i < constantPoolCount; ++i) {
				ConstantPoolInfo info = parseConstantPoolInfo(is);
				info.constantPool = cf.constantPool;
				cf.constantPool[i] = info;
				// these types take two entries (a bit odd)
				if (info.getClass().equals(LongInfo.class) || info.getClass().equals(DoubleInfo.class)) {
					i++;
				}
			}
			if (debug) {
				for (int i = 0; i < constantPoolCount; ++i) {
					System.err.printf("constantPool[%02d] = %s%n", i, cf.constantPool[i]);
				}
			}
			cf.accessFlags = is.readUShort();
			cf.thisClassIndex = is.readUShort();
			cf.superclassIndex = is.readUShort();
			int interfacesCount = is.readUShort();
			for (int i = 0; i < interfacesCount; ++i) {
				cf.interfaces.add((ClassInfo) cf.constantPool[is.readUShort()]);
			}
			int fieldsCount = is.readUShort();
			for (int i = 0; i < fieldsCount; ++i) {
				cf.fieldsAndMethods.add(parseFieldOrMethodInfo(cf, is, 'F'));
			}
			int methodsCount = is.readUShort();
			for (int i = 0; i < methodsCount; ++i) {
				cf.fieldsAndMethods.add(parseFieldOrMethodInfo(cf, is, 'M'));
			}
			int attributesCount = is.readUShort();
			for (int i = 0; i < attributesCount; ++i) {
				cf.attributes.add(AttributeInfo.parseAttribute(cf.constantPool, is));
			}
		}
		return cf;
	}
}
