package com.jbalint.jcfl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

public class FieldOrMethodInfo {
    public ClassFile cf;
	public char type;
	public int accessFlags;
	public int nameIndex;
	public int descriptorIndex;
	public List<AttributeInfo> attributes = new ArrayList<>();
	public ConstantPoolInfo constantPool[];

	static class Descriptor {
		String returnType;
		List<String> argumentTypes = new LinkedList<>();
	}

	private Descriptor descriptor;

	// method-only fields
	public Code codeAttr;
	/**
	 * List of methods called by this method. Defined after {@link #analyzeCode()} has been called.
	 */
	public List<Methodref> calledMethods = new ArrayList<>();

	public String getName() {
		return constantPool[nameIndex].asString();
	}

	public String getDescriptor() {
		return constantPool[descriptorIndex].asString();
	}

	private static int arrayDepth(String s, int startIndex) {
	    int end = startIndex;
	    while (s.charAt(end++) == '[');
        return end - startIndex - 1;
    }

	private void parseDescriptor() {
		descriptor = new Descriptor();
		String desc = constantPool[descriptorIndex].asString();
		// start at 1 to skip first opening paren
		for (int i = 1; i < desc.length(); /* increment inline */) {
			if (desc.charAt(i) == ')') {
				i++;
				descriptor.returnType = desc.substring(i);
                break;
			} else {
				switch (desc.charAt(i)) {
					case '[':
					    // need to look at first char after array depth prefix
					    int d = arrayDepth(desc, i);
                        if (desc.charAt(d + i) == 'L') {
                            String type = desc.substring(i, desc.indexOf(';', d + i) + 1);
                            i += type.length();
                            descriptor.argumentTypes.add(type);
                        } else {
                            descriptor.argumentTypes.add(desc.substring(i, i + d + 1));
                            i += d + 1;
                        }
                        break;
					case 'L':
						String type = desc.substring(i, desc.indexOf(';', i) + 1);
						i += type.length();
						descriptor.argumentTypes.add(type);
						break;
					default:
						descriptor.argumentTypes.add(desc.substring(i, i+1));
                        i++;
						break;
				}
			}
		}
	}

	public String getReturnTypeName() {
		if (descriptor == null) {
			parseDescriptor();
		}
		return descriptor.returnType;
	}

	public List<String> getArgumentTypeNames() {
		if (descriptor == null) {
			parseDescriptor();
		}
		return descriptor.argumentTypes;
	}

	public boolean isAbstract() {
		return (accessFlags & 0x0400) > 0;
	}

	public boolean isNative() {
		return (accessFlags & 0x0100) > 0;
	}

	public boolean isStatic() {
		return (accessFlags & 0x0008) > 0;
	}

	public void analyzeCode() {
		if (type != 'M') {
			throw new IllegalArgumentException("Code can only be analyzed on methods");
		}
		if (isAbstract() || isNative()) {
			return;
		}
		byte code[] = codeAttr.code;
		if (false) {
			StringBuilder sb = new StringBuilder("Code:");
			for (int i = 0; i < code.length; ++i) {
				if ((i % 16) == 0) {
					sb.append(String.format("\n%04x: ", i));
				} else if ((i % 8) == 0) {
					sb.append("  ");
				}
				sb.append(String.format("%02x ", code[i]));
			}
			System.err.println(sb);
		}
		for (int i = 0; i < code.length; ++i) {
			//System.err.printf("code[%d]=0x%02x%n", i, code[i] & 0xFF);
			switch ((code[i] & 0xff)) {
			case 0x32: // aaload
			case 0x53: // aastore
			case 0x01: // aconst_null
			case 0x2a: // aload_0
			case 0x2b: // aload_1
			case 0x2c: // aload_2
			case 0x2d: // aload_3
			case 0xb0: // areturn
			case 0xbe: // arraylength
			case 0x4b: // astore_0
			case 0x4c: // astore_1
			case 0x4d: // astore_2
			case 0x4e: // astore_3
			case 0xbf: // athrow
			case 0x33: // baload
			case 0x54: // bastore
			case 0x34: // caload
			case 0x55: // castore
			case 0x90: // d2f
			case 0x8e: // d2i
			case 0x8f: // d2l
			case 0x63: // dadd
			case 0x31: // daload
			case 0x52: // dastore
			case 0x98: // dcmpg
			case 0x97: // dcmpl
			case 0x0e: // dconst_0
			case 0x0f: // dconst_1
			case 0x6f: // ddiv
			case 0x26: // dload_0
			case 0x27: // dload_1
			case 0x28: // dload_2
			case 0x29: // dload_3
			case 0x6b: // dmul
			case 0x77: // dneg
			case 0x73: // drem
			case 0xaf: // dreturn
			case 0x47: // dstore_0
			case 0x48: // dstore_1
			case 0x49: // dstore_2
			case 0x4a: // dstore_3
			case 0x67: // dsub
			case 0x59: // dup
			case 0x5a: // dup_x1
			case 0x5b: // dup_x2
			case 0x5c: // dup2
			case 0x5d: // dup2_x1
			case 0x5e: // dup2_x2
			case 0x8d: // f2d
			case 0x8b: // f2i
			case 0x8c: // f2l
			case 0x62: // fadd
			case 0x30: // faload
			case 0x51: // fastore
			case 0x96: // fcmpg
			case 0x95: // fcmpl
			case 0x0b: // fconst_0
			case 0x0c: // fconst_1
			case 0x0d: // fconst_2
			case 0x6e: // fdiv
			case 0x22: // fload_0
			case 0x23: // fload_1
			case 0x24: // fload_2
			case 0x25: // fload_3
			case 0x6a: // fmul
			case 0x76: // fneg
			case 0x72: // frem
			case 0xae: // freturn
			case 0x43: // fstore_0
			case 0x44: // fstore_1
			case 0x45: // fstore_2
			case 0x46: // fstore_3
			case 0x66: // fsub
			case 0x91: // i2b
			case 0x92: // i2c
			case 0x87: // i2d
			case 0x86: // i2f
			case 0x85: // i2l
			case 0x93: // i2s
			case 0x60: // iadd
			case 0x2e: // iaload
			case 0x7e: // iand
			case 0x4f: // iastore
			case 0x02: // iconst_m1
			case 0x03: // iconst_0
			case 0x04: // iconst_1
			case 0x05: // iconst_2
			case 0x06: // iconst_3
			case 0x07: // iconst_4
			case 0x08: // iconst_5
			case 0x6c: // idiv
			case 0x1a: // iload_0
			case 0x1b: // iload_1
			case 0x1c: // iload_2
			case 0x1d: // iload_3
			case 0x68: // imul
			case 0x74: // ineg
			case 0x80: // ior
			case 0x70: // irem
			case 0xac: // ireturn
			case 0x78: // ishl
			case 0x7a: // ishr
			case 0x3b: // istore_0
			case 0x3c: // istore_1
			case 0x3d: // istore_2
			case 0x3e: // istore_3
			case 0x64: // isub
			case 0x7c: // iushr
			case 0x82: // ixor
			case 0x8a: // l2d
			case 0x89: // l2f
			case 0x88: // l2i
			case 0x61: // ladd
			case 0x2f: // laload
			case 0x7f: // land
			case 0x50: // lastore
			case 0x94: // lcmp
			case 0x09: // lconst_0
			case 0x0a: // lconst_1
			case 0x6d: // ldiv
			case 0x1e: // lload_0
			case 0x1f: // lload_1
			case 0x20: // lload_2
			case 0x21: // lload_3
			case 0x69: // lmul
			case 0x75: // lneg
			case 0x81: // lor
			case 0x71: // lrem
			case 0xad: // lreturn
			case 0x79: // lshl
			case 0x7b: // lshr
			case 0x3f: // lstore_0
			case 0x40: // lstore_1
			case 0x41: // lstore_2
			case 0x42: // lstore_3
			case 0x65: // lsub
			case 0x7d: // lushr
			case 0x83: // lxor
			case 0xc2: // monitorenter
			case 0xc3: // monitorexit
			case 0x00: // nop
			case 0x57: // pop
			case 0x58: // pop2
			case 0xb1: // return
			case 0x35: // saload
			case 0x56: // sastore
			case 0x5f: // swap
				break;
			case 0x19: // aload
			case 0x3a: // astore
			case 0x10: // bipush
			case 0x18: // dload
			case 0x39: // dstore
			case 0x17: // fload
			case 0x38: // fstore
			case 0x15: // iload
			case 0x36: // istore
			case 0x12: // ldc
			case 0x16: // lload
			case 0x37: // lstore
			case 0xbc: // newarray
			case 0xa9: // ret
				i++;
				break;
			case 0xbd: // anewarray
			case 0xc0: // checkcast
			case 0xb4: // getfield
			case 0xb2: // getstatic
			case 0xa7: // goto
			case 0xc8: // goto_w
			case 0xa5: // if_acmpeq
			case 0xa6: // if_acmpne
			case 0x9f: // if_icmpeq
			case 0xa0: // if_icmpne
			case 0xa1: // if_icmplt
			case 0xa2: // if_icmpge
			case 0xa3: // if_icmpgt
			case 0xa4: // if_icmple
			case 0x99: // ifeq
			case 0x9a: // ifne
			case 0x9b: // iflt
			case 0x9c: // ifge
			case 0x9d: // ifgt
			case 0x9e: // ifle
			case 0xc7: // ifnonnull
			case 0xc6: // ifnull
			case 0x84: // iinc
			case 0xc1: // instanceof
			case 0xa8: // jsr
			case 0x13: // ldc_w
			case 0x14: // ldc2_w
			case 0xbb: // new
			case 0xb5: // putfield
			case 0xb3: // putstatic
			case 0x11: // sipush
				i += 2;
				break;
			case 0xba: // invokedynamic
			case 0xc9: // jsr_w
				i += 4;
				break;
			case 0xab: { // lookupswitch
				i++;
				while ((i % 4) != 0) {
					++i;
				}
				i += 4;
				int npairs = ((code[i] & 0xFF) << 24) | ((code[i+1] & 0xFF) << 16) | ((code[i+2] & 0xFF) << 8) | (code[i+3] & 0xFF);
				i += 4;
				i += npairs * 8;
				i--;
				break;
			}
			case 0xc5: // multianewarray
				i += 3;
				break;
			case 0xaa: { // tableswitch
				i++;
				while ((i % 4) != 0) {
					++i;
				}
                int theDefault = (code[i] << 24) | ((code[i+1] & 0xff) << 16) | ((code[i+2] & 0xff) << 8) | (code[i+3] & 0xff);
				i += 4;
				// these are signed
				int low = (code[i] << 24) | ((code[i+1] & 0xff) << 16) | ((code[i+2] & 0xff) << 8) | (code[i+3] & 0xff);
				i += 4;
				int high = (code[i] << 24) | ((code[i+1] & 0xff) << 16) | ((code[i+2] & 0xff) << 8) | (code[i+3] & 0xff);
				i += 4;
                if (high != 3) {
                    if (high < low) {
                        System.err.println(String.format("def=%d, low=%d, high=%d\n", theDefault, low, high));
                        System.err.printf("\nDefault: ");
                        for (int j = i - 12; j < i - 8; ++j) {
                            System.err.printf("%02x ", code[j]);
                        }
                        System.err.printf("\nlow: ");
                        for (int j = i - 8; j < i - 4; ++j) {
                            System.err.printf("%02x ", code[j]);
                        }
                        System.err.printf("\nhigh: ");
                        for (int j = i - 4; j < i; ++j) {
                            System.err.printf("%02x ", code[j]);
                        }
                        System.err.printf("\n");
                    }
                }
                if (high < low) {
                    throw new IllegalArgumentException(String.format("tableswitch high(%d) < low(%d) at code[%d]", high, low, i));
                }
				i += 4 * (high - low + 1);
				i--;
				break;
			}
			case 0xc4: { // wide
				i++;
				if ((code[i++] & 0xFF) == 0x84) { // iinc_w
					i += 4;
				} else {
					i += 2;
				}
				i--;
				break;
			}
			case 0xb9: // invokeinterface
			case 0xb7: // invokespecial
			case 0xb8: // invokestatic
			case 0xb6: { // invokevirtual
				i++;
				int index = ((code[i] & 0xFF) << 8) | (code[i+1] & 0xFF);
				calledMethods.add((Methodref) constantPool[index]);
				i += 2;
				i--;
				break;
			}
			default:
                // for (int j = 0; j < code.length; ++j) {
                //     System.err.println(String.format("[%05d]: %02x", j, code[j] & 0xFF));
                // }
				throw new IllegalArgumentException(String.format("Unknown instruction byte '0x%02x' at code[%d]", code[i] & 0xFF, i));
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
        sb.append(cf.getClassName()).append(" - ");
		if (type == 'F') {
			sb.append("Field: ").append(getName()).append(" : ").append(getDescriptor());
		} else if (type == 'M') {
			sb.append("Method: ").append(getName()).append(getDescriptor());
		}
		for (AttributeInfo a : attributes) {
			sb.append("\n    ").append(a);
		}
		return sb.toString();
	}
}
