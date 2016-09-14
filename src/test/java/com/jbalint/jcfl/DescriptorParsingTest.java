package com.jbalint.jcfl;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by jbalint on 9/14/16.
 */
public class DescriptorParsingTest {

    private FieldOrMethodInfo createTestData(String descriptor) {
        FieldOrMethodInfo fmInfo = new FieldOrMethodInfo();
        fmInfo.descriptorIndex = 0;
        fmInfo.constantPool = new ConstantPoolInfo[1];
        fmInfo.constantPool[0] = new Utf8(descriptor);
        return fmInfo;
    }

    @Test
    public void simpleDescriptorParsing() throws Exception {
        FieldOrMethodInfo fmInfo = createTestData("(I)V");
        assertEquals("V", fmInfo.getReturnTypeName());
        assertArrayEquals(new String[] {"I"}, fmInfo.getArgumentTypeNames().toArray(new String[] {}));
    }

    @Test
    public void descriptorParsingClasses() throws Exception {
        FieldOrMethodInfo fmInfo = createTestData("(Ljava/lang/String;Ljava/lang/Object;)Ljava/util/List;");
        assertEquals("Ljava/util/List;", fmInfo.getReturnTypeName());
        assertArrayEquals(new String[] {"Ljava/lang/String;", "Ljava/lang/Object;"},
                fmInfo.getArgumentTypeNames().toArray());
    }

    @Test
    public void descriptorParsingArrays() throws Exception {
        FieldOrMethodInfo fmInfo = createTestData("([I[[[I[[Ljava/lang/String;)[[J");
        assertEquals("[[J", fmInfo.getReturnTypeName());
        assertArrayEquals(new String[] {"[I", "[[[I", "[[Ljava/lang/String;"},
                fmInfo.getArgumentTypeNames().toArray());
    }

    @Test
    public void descriptorParsingMixed() throws Exception {
        FieldOrMethodInfo fmInfo = createTestData("(I[JLjava/lang/Object;JD)I");
        assertEquals("I", fmInfo.getReturnTypeName());
        assertArrayEquals(new String[] {"I", "[J", "Ljava/lang/Object;", "J", "D"},
                fmInfo.getArgumentTypeNames().toArray());
    }
}
