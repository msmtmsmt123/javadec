package org.javadec;

import org.objectweb.asm.tree.AbstractInsnNode;

import static org.objectweb.asm.Opcodes.*;

public class InsnUtils {
    public static boolean isGoto(AbstractInsnNode insn) {
        return insn.getOpcode() == GOTO;
    }

    public static boolean isThrow(AbstractInsnNode insn) {
        return insn.getOpcode() == ATHROW;
    }

    public static boolean isReturn(AbstractInsnNode insn) {
        int opcode = insn.getOpcode();
        return opcode == IRETURN
                || opcode == LRETURN
                || opcode == FRETURN
                || opcode == DRETURN
                || opcode == ARETURN
                || opcode == RETURN;
    }

    public static boolean isJump(AbstractInsnNode insn) {
        return insn.getType() == AbstractInsnNode.JUMP_INSN;
    }

    public static boolean isLabel(AbstractInsnNode insn) {
        return insn != null && insn.getType() == AbstractInsnNode.LABEL;
    }
}
