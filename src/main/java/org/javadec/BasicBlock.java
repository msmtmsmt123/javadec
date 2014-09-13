package org.javadec;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {
    public int start;
    public int end;
    public List<BasicBlock> succ = new ArrayList<>();

    @Override
    public String toString() {
        return "[" + start + ":" + end + "]";
    }
}
