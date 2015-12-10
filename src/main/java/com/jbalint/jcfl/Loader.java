package com.jbalint.jcfl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static com.jbalint.jcfl.ConstantPoolInfo.InfoType.*;

public class Loader {
	private static boolean debug = true;

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
			throw new UnsupportedOperationException("InvokeDynamic not supported");
		}
		throw new IllegalArgumentException("Unknown constant pool tag: " + tag);
	}

	private static FieldOrMethodInfo parseFieldOrMethodInfo(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
		FieldOrMethodInfo info = new FieldOrMethodInfo();
		info.type = 'F';
		info.accessFlags = is.readUShort();
		info.nameIndex = is.readUShort();
		info.descriptorIndex = is.readUShort();
		info.constantPool = constantPool;
		int attributesCount = is.readUShort();
		for (int i = 0; i < attributesCount; ++i) {
			info.attributes.add(parseAttribute(constantPool, is));
		}
		return info;
	}

	private static AttributeInfo parseAttribute(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
		int typeIndex = is.readUShort();
		String type = constantPool[typeIndex].asString();
		long length = is.readUInt();
		if ("ConstantValue".equals(type)) {
			ConstantValue info = new ConstantValue();
			info.type = type;
			info.constantValue = constantPool[is.readUShort()];
			return info;
		} else if ("Code".equals(type)) {
			Code info = new Code();
			info.type = type;
			info.maxStack = is.readUShort();
			info.maxLocals = is.readUShort();
			long codeLength = is.readUInt();
			info.code = new byte[(int) codeLength];
			is.readFully(info.code);
			int exceptionTableLength = is.readUShort();
			info.exceptionTable = new Code.CodeException[exceptionTableLength];
			for (int i = 0; i < exceptionTableLength; ++i) {
				Code.CodeException exc = new Code.CodeException();
				exc.startPc = is.readUShort();
				exc.endPc = is.readUShort();
				exc.handlerPc = is.readUShort();
				exc.catchType = is.readUShort();
				info.exceptionTable[i] = exc;
			}
			int attributesCount = is.readUShort();
			for (int i = 0; i < attributesCount; ++i) {
				info.attributes.add(parseAttribute(constantPool, is));
			}
			return info;
		} else if ("StackMapTable".equals(type)) {
			// skipped (TODO: implement if we ever need this)
			for (int i = 0; i < length; ++i) {
				is.read();
			}
			return null;
		} else if ("Exceptions".equals(type)) {
			Exceptions info = new Exceptions();
			info.type = type;
			int numberOfExceptions = is.readUShort();
			for (int i = 0; i < numberOfExceptions; ++i) {
				info.exceptions.add((ClassInfo) constantPool[is.readUShort()]);
			}
			return info;
		} else if ("BootstrapMethods".equals(type)) {
		} else if ("InnerClasses".equals(type)) {
			InnerClasses info = new InnerClasses();
			info.type = type;
			int numberOfClasses = is.readUShort();
			for (int i = 0; i < numberOfClasses; ++i) {
				InnerClasses.InnerClass inner = new InnerClasses.InnerClass();
				inner.innerClassInfoIndex = is.readUShort();
				inner.outerClassInfoIndex = is.readUShort();
				inner.innerNameIndex = is.readUShort();
				inner.innerClassAccessFlags = is.readUShort();
				info.classes.add(inner);
			}
			return info;
		} else if ("EnclosingMethod".equals(type)) {
			EnclosingMethod info = new EnclosingMethod();
			info.type = type;
			info.clazz = (ClassInfo) constantPool[is.readUShort()];
			// may be null
			info.method = (NameAndType) constantPool[is.readUShort()];
			return info;
		} else if ("Synthetic".equals(type)) {
			Synthetic info = new Synthetic();
			info.type = type;
			return info;
		} else if ("Signature".equals(type)) {
			Signature info = new Signature();
			info.type = type;
			info.signature = constantPool[is.readUShort()].asString();
			return info;
		} else if ("RuntimeVisibleAnnotations".equals(type)) {
		} else if ("RuntimeInvisibleAnnotations".equals(type)) {
		} else if ("RuntimeVisibleParameterAnnotations".equals(type)) {
		} else if ("RuntimeInvisibleParameterAnnotations".equals(type)) {
		} else if ("RuntimeVisibleTypeAnnotations".equals(type)) {
		} else if ("RuntimeInvisibleTypeAnnotations".equals(type)) {
		} else if ("AnnotationDefault".equals(type)) {
		} else if ("MethodParameters".equals(type)) {
		} else if ("SourceFile".equals(type)) {
			SourceFile info = new SourceFile();
			info.type = type;
			info.sourceFile = constantPool[is.readUShort()].asString();
			return info;
		} else if ("SourceDebugExtension".equals(type)) {
		} else if ("LineNumberTable".equals(type)) {
			LineNumberTable info = new LineNumberTable();
			info.type = type;
			int lineNumberTableLength = is.readUShort();
			for (int i = 0; i < lineNumberTableLength; ++i) {
				int startPc = is.readUShort();
				info.lineNumberTable.put(startPc, is.readUShort());
			}
			return info;
		} else if ("LocalVariableTable".equals(type)) {
			LocalVariableTable info = new LocalVariableTable();
			info.type = type;
			int localVariableTableLength = is.readUShort();
			for (int i = 0; i < localVariableTableLength; ++i) {
				LocalVariableTable.LocalVariable v = new LocalVariableTable.LocalVariable();
				v.startPc = is.readUShort();
				v.length = is.readUShort();
				v.name = constantPool[is.readUShort()].asString();
				v.descriptor = constantPool[is.readUShort()].asString();
				v.index = is.readUShort();
				info.localVariableTable.add(v);
			}
			return info;
		} else if ("LocalVariableTypeTable".equals(type)) {
			LocalVariableTypeTable info = new LocalVariableTypeTable();
			info.type = type;
			int localVariableTypeTableLength = is.readUShort();
			for (int i = 0; i < localVariableTypeTableLength; ++i) {
				LocalVariableTypeTable.LocalVariableType t = new LocalVariableTypeTable.LocalVariableType();
				t.startPc = is.readUShort();
				t.length = is.readUShort();
				t.name = constantPool[is.readUShort()].asString();
				t.signature = constantPool[is.readUShort()].asString();
				t.index = is.readUShort();
				info.localVariableTypeTable.add(t);
			}
			return info;
		} else if ("Deprecated".equals(type)) {
		}
		throw new IllegalArgumentException("Unsupported attribute type: " + type);
	}

	/**
	 * Load and parse a Java class file.
	 */
	// alias shell='java -Xcheck:jni -esa -agentlib:yt -classpath build/classes/main java.util.prefs.Base64'
	public static ClassFile load(File physicalFile) throws IOException {
		ClassFile cf = new ClassFile();
		try (UnsignedDataInputStream is = new UnsignedDataInputStream(new FileInputStream(physicalFile))) {
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
			cf.superClassIndex = is.readUShort();
			int interfacesCount = is.readUShort();
			for (int i = 0; i < interfacesCount; ++i) {
				cf.interfaces.add(cf.constantPool[is.readUShort()]);
			}
			int fieldsCount = is.readUShort();
			for (int i = 0; i < fieldsCount; ++i) {
				cf.fields.add(parseFieldOrMethodInfo(cf.constantPool, is));
			}
			int methodsCount = is.readUShort();
			for (int i = 0; i < methodsCount; ++i) {
				cf.fields.add(parseFieldOrMethodInfo(cf.constantPool, is));
			}
			int attributesCount = is.readUShort();
			for (int i = 0; i < attributesCount; ++i) {
				cf.attributes.add(parseAttribute(cf.constantPool, is));
			}
		}
		return cf;
	}
}
