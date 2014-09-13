/*
 * Apache License 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 * Based on https://svn.apache.org/repos/asf/flex/falcon/trunk/compiler/src/org/apache/flex/abc/graph/algorithms/DominatorTree.java
 */

package org.javadec.cfg;

import java.util.*;

/**
 * An implementation of the O(n log n) Lengauer-Tarjan algorithm for building the
 * <a href="http://en.wikipedia.org/wiki/Dominator_%28graph_theory%29">dominator tree</a>
 * of a {@link org.javadec.cfg.ControlFlowGraph flowgraph}.
 */
public class DominatorTree {
    /**
     * Semidominator numbers by block.
     */
    private int[] semi;
    /**
     * Parents by block.
     */
    private int[] parent;
    /**
     * Predecessors by block.
     */
    private BitSet[] pred;
    /**
     * Blocks in DFS order; used to look up a block from its semidominator
     * numbering.
     */
    private List<BasicBlock> vertex = new ArrayList<>();
    /**
     * Blocks by semidominator block.
     */
    private BitSet[] bucket;
    /**
     * idominator map, built iteratively.
     */
    private int[] idom;

    /**
     * Auxiliary data structure used by the O(m log n) eval/link implementation:
     * ancestor relationships in the forest (the processed tree as it's built
     * back up).
     */
    private int[] ancestor;
    /**
     * Auxiliary data structure used by the O(m log n) eval/link implementation:
     * node with least semidominator seen during traversal of a path from node
     * to subtree root in the forest.
     */
    private int[] label;


    /**
     * Construct a DominatorTree from a root.
     *
     * @param root the root of the graph.
     */
    public DominatorTree(BasicBlock root) {
        dfs(root, new HashSet<>());
        init();
        computeDominators();
    }


    /**
     * Get immediate dominator.
     *
     * @param node The node of interest
     * @return immediate dominator (if it has one).
     */
    public BasicBlock getIdom(BasicBlock node) {
        return node(idom[dfnum(node)]);
    }

    /**
     * Depth-first search the graph and initialize data structures.
     *
     * @param node the root of the flowgraph.  One of these is
     *             the start block, the others are exception handlers.
     */
    private void dfs(BasicBlock node, Set<BasicBlock> visited) {
        if (!visited.add(node)) {
            return;
        }

        vertex.add(node);

        for (BasicBlock child : node.succ) {
            dfs(child, visited);
        }
    }

    private void init() {
        int size = vertex.size();
        idom = new int[size];
        semi = new int[size];
        label = new int[size];
        parent = new int[size];
        ancestor = new int[size];
        bucket = new BitSet[size];
        pred = new BitSet[size];

        for (int i = 0; i < bucket.length; i++) {
            idom[i] = -1;
            semi[i] = -1;
            label[i] = -1;
            parent[i] = -1;
            ancestor[i] = -1;
            bucket[i] = new BitSet(size);
            pred[i] = new BitSet(size);
        }

        for (int dfnum = 0; dfnum < size; dfnum++) {
            semi[dfnum] = dfnum; //  Initial assumption: the node's semidominator is itself.
            label[dfnum] = dfnum;

            for (BasicBlock childBlock : node(dfnum).succ) {
                int child = dfnum(childBlock);
                pred[child].set(dfnum);
                if (semi[child] == -1) {
                    parent[child] = dfnum;
                }
            }
        }
    }

    /**
     * Steps 2, 3, and 4 of Lengauer-Tarjan.
     */
    private void computeDominators() {
        int lastSemiNumber = semi.length - 1;

        for (int w = lastSemiNumber; w > 0; w--) {
            int p = parent[w];

            //  step 2: compute semidominators
            //  for each v in pred(w)...
            int semidominator = semi[w];

            for (int v = pred[w].nextSetBit(0); v >= 0; v = pred[w].nextSetBit(v + 1)) {
                semidominator = Math.min(semidominator, semi[eval(v)]);
            }

            semi[w] = semidominator;
            bucket[semidominator].set(w);

            //  Link w into the forest via its parent, p
            link(p, w);

            //  step 3: implicitly compute idominators
            //  for each v in bucket(parent(w)) ...
            for (int v = bucket[p].nextSetBit(0); v >= 0; v = bucket[p].nextSetBit(v + 1)) {
                int u = eval(v);
                if (semi[u] < semi[v]) {
                    idom[v] = u;
                } else {
                    idom[v] = p;
                }
            }

            bucket[p].clear();
        }

        // step 4: explicitly compute idominators
        for (int w = 1; w <= lastSemiNumber; w++) {
            if (idom[w] != semi[w]) {
                idom[w] = idom[idom[w]];
            }
        }
    }

    /**
     * Extract the node with the least-numbered semidominator in the (processed)
     * ancestors of the given node.
     *
     * @param v - the node of interest.
     * @return "If v is the root of a tree in the forest, return v. Otherwise,
     * let r be the root of the tree which contains v. Return any vertex u != r
     * of miniumum semi(u) on the path r-*v."
     */
    private int eval(int v) {
        //  This version of Lengauer-Tarjan implements
        //  eval(v) as a path-compression procedure.
        compress(v);
        return label[v];
    }

    /**
     * Traverse ancestor pointers back to a subtree root, then propagate the
     * least semidominator seen along this path through the "label" map.
     */
    private void compress(int v) {
        Stack<Integer> worklist = new Stack<>();
        worklist.add(v);

        int a = ancestor[v];

        //  Traverse back to the subtree root.
        while (a >= 0) {
            worklist.push(a);
            a = ancestor[a];
        }

        //  Propagate semidominator information forward.
        int ancestor = worklist.pop();
        int leastSemi = semi[label[ancestor]];

        while (!worklist.empty()) {
            int descendent = worklist.pop();
            int currentSemi = semi[label[descendent]];

            if (currentSemi > leastSemi) {
                label[descendent] = label[ancestor];
            } else {
                leastSemi = currentSemi;
            }

            //  Prepare to process the next iteration.
            ancestor = descendent;
        }
    }

    /**
     * Simple version of link(parent,child) simply links the child into the
     * parent's forest, with no attempt to balance the subtrees or otherwise
     * optimize searching.
     */
    private void link(int parent, int child) {
        ancestor[child] = parent;
    }


    private BasicBlock node(int dfnum) {
        if (dfnum == -1) {
            return null;
        }
        return vertex.get(dfnum);
    }

    private int dfnum(BasicBlock node) {
        if (node == null) {
            return -1;
        }

        for (int i = 0; i < vertex.size(); i++) {
            if (vertex.get(i).equals(node)) {
                return i;
            }
        }
        throw new NoSuchElementException();
    }
}