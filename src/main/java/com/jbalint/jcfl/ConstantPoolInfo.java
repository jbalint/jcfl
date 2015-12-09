package com.jbalint.jcfl;

public class ConstantPoolInfo {
	public static enum InfoType {
		CLASS(7), FIELD_REF(9), METHOD_REF(10), INTERFACE_METHOD_REF(11),
		STRING(8), INTEGER(3), FLOAT(4), LONG(5), DOUBLE(6), NAME_AND_TYPE(12), UTF8(1),
		METHOD_HANDLE(15), METHOD_TYPE(16), INVOKE_DYNAMIC(18);

		public final byte tag;

		InfoType(int t) {
			tag = (byte) t;
		}
	}

	public InfoType type;

	public static class ClassInfo extends ConstantPoolInfo {
		public short nameIndex;
	}

	public static class Fieldref extends ConstantPoolInfo {
		public short classIndex;
		public short nameAndTypeIndex;
	}

	public static class Methodref extends ConstantPoolInfo {
		public short classIndex;
		public short nameAndTypeIndex;
	}

	public static class InterfaceMethodref extends ConstantPoolInfo {
		public short classIndex;
		public short nameAndTypeIndex;
	}

	public static class StringInfo extends ConstantPoolInfo {
		public short index;
	}

	public static class IntegerInfo extends ConstantPoolInfo {
		public int value;
	}

	public static class FloatInfo extends ConstantPoolInfo {
		public float value;
	}

	public static class LongInfo extends ConstantPoolInfo {
		public long value;
	}

	public static class DoubleInfo extends ConstantPoolInfo {
		public double value;
	}

	public static class NameAndType extends ConstantPoolInfo {
		public short nameIndex;
		public short descriptorIndex;
	}

	public static class Utf8 extends ConstantPoolInfo {
		public byte[] value;
	}

	public static class MethodHandle extends ConstantPoolInfo {
		public byte referenceKind;
		public short referenceIndex;
	}

	public static class MethodType extends ConstantPoolInfo {
		public short descriptorIndex;
	}

	public static class InvokeDynamic extends ConstantPoolInfo {
		public short bootstrapMethodAttrIndex;
		public short nameAndTypeIndex;
	}
}
