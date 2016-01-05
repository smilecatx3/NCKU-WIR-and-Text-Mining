package wir.hw2.graph.node;

import java.util.ArrayList;
import java.util.List;


public class Node implements Comparable<Node> {
    private int name;
    public List<Node> incomingNeighbors = new ArrayList<>();
    public List<Node> outgoingNeighbors = new ArrayList<>();

    public Node(int name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }

    @Override
    public int compareTo(Node node) {
        return this.name - node.name;
    }
}
