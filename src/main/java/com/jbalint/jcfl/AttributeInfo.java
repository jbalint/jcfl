package com.jbalint.jcfl;

import java.io.IOException;
import java.util.HashSet;

public abstract class AttributeInfo {
	public String type;

    static HashSet<String> ignoredTypes = new HashSet<>();

    public <T> T accept(AttributeVisitor<T> visitor) {
        if (!ignoredTypes.contains(this.type)) {
            ignoredTypes.add(this.type);
            System.out.println("(ignored method attribute type=" + this.type + ")");
        }

        return null;
    }

    /**
     * Parse an attribute
     */
    public static AttributeInfo parseAttribute(ConstantPoolInfo constantPool[], UnsignedDataInputStream is) throws IOException {
        int typeIndex = is.readUShort();
        String type = constantPool[typeIndex].asString();
        long length = is.readUInt();
        if ("ConstantValue".equals(type)) {
            return ConstantValue.parse(constantPool, is);
        } else if ("Code".equals(type)) {
            return Code.parse(constantPool, is);
        } else if ("StackMapTable".equals(type)) {
        } else if ("Exceptions".equals(type)) {
            return Exceptions.parse(constantPool, is);
        } else if ("BootstrapMethods".equals(type)) {
        } else if ("InnerClasses".equals(type)) {
            return InnerClasses.parse(constantPool, is);
        } else if ("EnclosingMethod".equals(type)) {
            return EnclosingMethod.parse(constantPool, is);
        } else if ("Synthetic".equals(type)) {
            Synthetic info = new Synthetic();
            info.type = type;
            return info;
        } else if ("Signature".equals(type)) {
            return Signature.parse(constantPool, is);
        } else if ("RuntimeVisibleAnnotations".equals(type)) {
            return RuntimeAnnotations.parse(constantPool, is, true);
        } else if ("RuntimeInvisibleAnnotations".equals(type)) {
            return RuntimeAnnotations.parse(constantPool, is, false);
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
            return LineNumberTable.parse(constantPool, is);
        } else if ("LocalVariableTable".equals(type)) {
            return LocalVariableTable.parse(constantPool, is);
        } else if ("LocalVariableTypeTable".equals(type)) {
            return LocalVariableTypeTable.parse(constantPool, is);
        } else if ("Deprecated".equals(type)) {
            Deprecated info = new Deprecated();
            info.type = type;
            return info;
        }
        // TODO handle ignored attributes. ideally they will all be parsed
        for (int i = 0; i < length; ++i) { is.read(); }
        return null;
    }
}
