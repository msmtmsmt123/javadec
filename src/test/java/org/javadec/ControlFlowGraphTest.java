package org.javadec;

import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayInputStream;

public class ControlFlowGraphTest {

    @Test
    public void testName() throws Exception {
        byte[] bytes = TestHelper.compileResource("sample.HelloWorld", "/sample/HelloWorld.java");
        System.out.println(TestHelper.disassemble(bytes));
        ClassNode classNode = ClassFileReader.readClass(new ByteArrayInputStream(bytes));
        ControlFlowGraph graph = new ControlFlowGraph(classNode.methods.get(1));
        System.out.println(graph);
    }


}