package wir.hw2.hits;

import wir.hw2.graph.node.Node;


public class HITSNode extends Node {
    public double authority = 1.0;
    public double hub = 1.0;

    public HITSNode(int name) {
        super(name);
    }
}
