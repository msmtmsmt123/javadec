package org.javadec.cfg;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DominatorTreeTest {

    @Test
    public void testDominatorTree() {
        //      0
        //      |
        //   +--1
        //   |  |
        //   |  2---+
        //   |  |   |
        //   +->3   6
        //      |   |
        //      4   7
        //      |   |
        //      5<--+
        BasicBlock v0 = new BasicBlock(0);
        BasicBlock v1 = new BasicBlock(1);
        BasicBlock v2 = new BasicBlock(2);
        BasicBlock v3 = new BasicBlock(3);
        BasicBlock v4 = new BasicBlock(4);
        BasicBlock v5 = new BasicBlock(5);
        BasicBlock v6 = new BasicBlock(6);
        BasicBlock v7 = new BasicBlock(7);

        v0.addSucc(v1);
        v1.addSucc(v2, v3);
        v2.addSucc(v3, v6);
        v3.addSucc(v4);
        v4.addSucc(v5);
        v5.addSucc();
        v6.addSucc(v7);
        v7.addSucc(v5);

        DominatorTree tree = new DominatorTree(v0);

        //      0
        //      |
        //   +--1--+
        //   |  |  |
        //   3  2  5
        //   |  |
        //   4  6
        //      |
        //      7
        assertNull(tree.idom(v0));
        assertEquals(v0, tree.idom(v1));
        assertEquals(v1, tree.idom(v2));
        assertEquals(v1, tree.idom(v3));
        assertEquals(v3, tree.idom(v4));
        assertEquals(v1, tree.idom(v5));
        assertEquals(v2, tree.idom(v6));
        assertEquals(v6, tree.idom(v7));
        System.out.println(tree);
    }
}