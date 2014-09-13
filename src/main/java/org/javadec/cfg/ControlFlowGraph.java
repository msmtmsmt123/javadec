package org.javadec.cfg;


import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.javadec.InsnUtils.*;

public class ControlFlowGraph {
    final Map<AbstractInsnNode, BasicBlock> blockMap = new LinkedHashMap<>();
    private final MethodNode methodNode;
    BasicBlock root;
    private InsnList insnList;

    public ControlFlowGraph(MethodNode methodNode) {
        this.methodNode = methodNode;
        insnList = methodNode.instructions;
        identifyBlocks();
        connectBlocks();
    }

    private void identifyBlocks() {
        root = startBlock(0);

        BasicBlock block = root;
        for (int i = 0; i < insnList.size(); i++) {
            AbstractInsnNode insn = insnList.get(i);
            if (isLabel(insn)) {
                block = startBlock(i);
            } else if (isJump(insn) || isReturn(insn) || isThrow(insn)) {
                block.end = i;
                block = startBlock(i + 1);
            } else {
                block.end = i;
            }
        }
    }

    private void connectBlocks() {
        for (BasicBlock basicBlock : blockMap.values()) {
            AbstractInsnNode endInsn = insnList.get(basicBlock.end);
            if (endInsn instanceof JumpInsnNode) {
                JumpInsnNode jump = (JumpInsnNode) endInsn;
                basicBlock.succ.add(blockMap.get(jump.label));
            }
            if (!isGoto(endInsn) && !isReturn(endInsn) && !isThrow(endInsn)) {
                basicBlock.succ.add(blockMap.get(endInsn.getNext()));
            }
        }
    }

    private BasicBlock startBlock(int startIndex) {
        if (startIndex >= insnList.size()) return null;

        AbstractInsnNode insn = insnList.get(startIndex);
        BasicBlock block = blockMap.get(insn);
        if (block != null) return block;

        block = new BasicBlock();
        block.start = startIndex;
        block.end = startIndex;
        blockMap.put(insn, block);
        return block;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append(methodNode.name).append(" {\n");
        for (BasicBlock bb : blockMap.values()) {
            for (BasicBlock succ : bb.succ) {
                sb.append("  b").append(bb.start).append(" -> b").append(succ.start).append("\n");
            }
            sb.append("  b").append(bb.start).append("[shape=box,label=\"").append(bb).append("\"];\n");
        }
        sb.append("}\n");
        return sb.toString();
    }
}

