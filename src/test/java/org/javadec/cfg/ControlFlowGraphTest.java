package org.javadec.cfg;

import org.javadec.ClassFileReader;
import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;

import static org.javadec.TestHelper.compileResource;
import static org.javadec.TestHelper.disassemble;

public class ControlFlowGraphTest {

    @Test
    public void testName() throws Exception {
        byte[] bytes = compileResource("sample.HelloWorld", "/sample/HelloWorld.java");
        System.out.println(disassemble(bytes));
        ClassNode classNode = ClassFileReader.readClass(bytes);

        ControlFlowGraph graph = new ControlFlowGraph(classNode.methods.get(1));
        System.out.println(graph);

        System.out.println();

        DominatorTree dominatorTree = new DominatorTree(graph.root);
        System.out.println(dominatorTree);
    }


}