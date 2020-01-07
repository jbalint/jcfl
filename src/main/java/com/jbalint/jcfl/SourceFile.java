package com.jbalint.jcfl;

public class SourceFile extends AttributeInfo {
	public String sourceFile;

	@Override
	public String toString() {
		return type + ": " + sourceFile;
	}
    
    @Override
    public <T> T accept(AttributeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
