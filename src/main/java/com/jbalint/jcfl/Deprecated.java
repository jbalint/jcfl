package com.jbalint.jcfl;

public class Deprecated extends AttributeInfo {
    @Override
    public <T> T accept(AttributeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
