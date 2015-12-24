package wir.hw2.simrank;

import wir.hw2.graph.node.Node;


public class NodePair implements Comparable<NodePair> {
    public Node node1, node2;

    public NodePair(Node node1, Node node2) {
        this.node1 = node1;
        this.node2 = node2;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", node1.toString(), node2.toString());
    }

    @Override
    public int compareTo(NodePair node) {
        int result = node1.compareTo(node.node1);
        return (result == 0) ? node2.compareTo(node.node2) : result;
    }
}
