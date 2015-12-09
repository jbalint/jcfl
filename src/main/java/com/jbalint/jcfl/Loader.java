package com.jbalint.jcfl;

import java.io.*;

import com.jbalint.jcfl.ConstantPoolInfo.*;
import com.jbalint.jcfl.ClassFile.*;
import static com.jbalint.jcfl.ConstantPoolInfo.InfoType.*;

public class Loader {
	private static ConstantPoolInfo parseConstantPoolInfo(DataInputStream is) throws IOException {
		int tag = is.read();
		if (tag == CLASS.tag) {
			ClassInfo info = new ClassInfo();
			info.nameIndex = is.readShort();
			return info;
		} else if (tag == FIELD_REF.tag) {
			Fieldref info = new Fieldref();
			info.classIndex = is.readShort();
			info.nameAndTypeIndex = is.readShort();
			return info;
		} else if (tag == METHOD_REF.tag) {
			Methodref info = new Methodref();
			info.classIndex = is.readShort();
			info.nameAndTypeIndex = is.readShort();
			return info;
		} else if (tag == INTERFACE_METHOD_REF.tag) {
			InterfaceMethodref info = new InterfaceMethodref();
			info.classIndex = is.readShort();
			info.nameAndTypeIndex = is.readShort();
			return info;
		} else if (tag == STRING.tag) {
			StringInfo info = new StringInfo();
			info.index = is.readShort();
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
			info.nameIndex = is.readShort();
			info.descriptorIndex = is.readShort();
			return info;
		} else if (tag == UTF8.tag) {
			Utf8 info = new Utf8();
			int len = is.readShort();
			info.value = new byte[len];
			is.readFully(info.value);
			System.err.println("UTF8:" + new String(info.value, java.nio.charset.Charset.forName("UTF-8")));
			return info;
		} else if (tag == METHOD_HANDLE.tag) {
			MethodHandle info = new MethodHandle();
			info.referenceKind = (byte) is.read();
			info.referenceIndex = is.readShort();
			return info;
		} else if (tag == METHOD_TYPE.tag) {
			MethodType info = new MethodType();
			info.descriptorIndex = is.readShort();
			return info;
        } else if (tag == INVOKE_DYNAMIC.tag) {
			throw new UnsupportedOperationException("InvokeDynamic not supported");
		}
		throw new IllegalArgumentException("Unknown constant pool tag: " + tag);
	}

	private static FieldOrMethodInfo parseFieldOrMethodInfo(DataInputStream is) throws IOException {
		FieldOrMethodInfo info = new FieldOrMethodInfo();
		info.type = 'F';
		info.accessFlags = is.readShort();
		return info;
	}

	// alias shell='java -Xcheck:jni -esa -agentlib:yt -classpath build/classes/main java.util.prefs.Base64'
	public static ClassFile load(File physicalFile) throws IOException {
		ClassFile cf = new ClassFile();
		try (DataInputStream is = new DataInputStream(new FileInputStream(physicalFile))) {
			cf.magic = is.readInt();
			is.readShort(); // ignored
			cf.version = is.readShort();
			int constantPoolCount = is.readShort() - 1;
			System.err.println("Reading " + constantPoolCount + " constants");
			cf.constantPool.add(new ConstantPoolInfo()); // so CP indexes are exact
			for (int i = 0; i < constantPoolCount; ++i) {
				cf.constantPool.add(parseConstantPoolInfo(is));
				if (cf.constantPool.get(cf.constantPool.size()-1).getClass().equals(LongInfo.class) ||
					cf.constantPool.get(cf.constantPool.size()-1).getClass().equals(DoubleInfo.class)) {
					// these types take two entries (a bit odd)
					i++;
				}
			}
			cf.accessFlags = is.readShort();
			cf.thisClassIndex = is.readShort();
			cf.superClassIndex = is.readShort();
			int interfacesCount = is.readShort();
			for (int i = 0; i < interfacesCount; ++i) {
				cf.interfaces.add(cf.constantPool.get(is.readShort()));
			}
			int fieldsCount = is.readShort();
			for (int i = 0; i < fieldsCount; ++i) {
				cf.fields.add(parseFieldOrMethodInfo(is));
			}
			int methodsCount = is.readShort();
			for (int i = 0; i < methodsCount; ++i) {
				cf.fields.add(parseFieldOrMethodInfo(is));
			}
			int attributesCount = is.readShort();
			for (int i = 0; i < attributesCount; ++i) {
			}
		}
		return cf;
	}
}
