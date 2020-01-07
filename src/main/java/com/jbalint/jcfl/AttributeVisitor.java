package com.jbalint.jcfl;

public interface AttributeVisitor<T> {
    T visit(ConstantValue attribute);
    T visit(Code attribute);
    T visit(Deprecated attribute);
    T visit(Exceptions attribute);
    T visit(InnerClasses attribute);
    T visit(LineNumberTable attribute);
    T visit(LocalVariableTable attribute);
    T visit(LocalVariableTypeTable attribute);
    T visit(SourceFile attribute);
}
