package org.javadec.cfg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BasicBlock {
    public int start;
    public int end;
    public List<BasicBlock> succ = new ArrayList<>();

    public BasicBlock(int start) {
        this.start = start;
        this.end = start;
    }

    @Override
    public String toString() {
        return "[" + start + ":" + end + "]";
    }

    public void addSucc(BasicBlock... blocks) {
        Collections.addAll(succ, blocks);
    }
}
