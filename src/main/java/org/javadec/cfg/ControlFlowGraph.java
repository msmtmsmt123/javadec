package org.javadec.cfg;


import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static org.javadec.InsnUtils.*;

public class ControlFlowGraph {

    public final BasicBlock root;
    private final String name;

    public ControlFlowGraph(String name, BasicBlock root) {
        this.name = name;
        this.root = root;
    }

    public static ControlFlowGraph create(MethodNode methodNode) {
        return new Builder(methodNode).build();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("digraph ").append(name).append(" {\n");
        appendBlock(sb, root, new HashSet<>());
        sb.append("}\n");
        return sb.toString();
    }

    private void appendBlock(StringBuilder sb, BasicBlock bb, Set<BasicBlock> visited) {
        if (!visited.add(bb)) return;

        for (BasicBlock succ : bb.succ) {
            sb.append("  b").append(bb.start).append(" -> b").append(succ.start).append("\n");
            appendBlock(sb, succ, visited);
        }
        sb.append("  b").append(bb.start).append("[shape=box,label=\"").append(bb).append("\"];\n");
    }


    private static class Builder {
        private final InsnList insnList;
        private final Map<AbstractInsnNode, BasicBlock> blockMap = new LinkedHashMap<>();
        private final MethodNode methodNode;
        private BasicBlock root;

        public Builder(MethodNode methodNode) {
            this.methodNode = methodNode;
            insnList = methodNode.instructions;
        }

        public ControlFlowGraph build() {
            identifyBlocks();
            connectBlocks();
            return new ControlFlowGraph(methodNode.name, root);
        }

        private BasicBlock startBlock(int startIndex) {
            if (startIndex >= insnList.size()) {
                return null;
            }

            AbstractInsnNode insn = insnList.get(startIndex);
            BasicBlock block = blockMap.get(insn);
            if (block != null) {
                return block;
            }

            block = new BasicBlock(startIndex);
            blockMap.put(insn, block);
            return block;
        }

        private void connectBlocks() {
            for (BasicBlock basicBlock : blockMap.values()) {
                AbstractInsnNode endInsn = insnList.get(basicBlock.end);
                if (endInsn instanceof JumpInsnNode) {
                    JumpInsnNode jump = (JumpInsnNode) endInsn;
                    basicBlock.addSucc(blockMap.get(jump.label));
                }
                if (!isGoto(endInsn) && !isReturn(endInsn) && !isThrow(endInsn)) {
                    basicBlock.addSucc(blockMap.get(endInsn.getNext()));
                }
            }
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
    }
}

