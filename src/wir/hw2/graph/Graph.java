package wir.hw2.graph;

import wir.hw2.graph.node.Node;

import java.util.Set;
import java.util.TreeSet;


public class Graph<V extends Node> {
    private String name;
    private Set<V> nodeSet = new TreeSet<>();

    public Graph(String name) {
        this.name = name;
    }

    public void addNode(V node) {
        nodeSet.add(node);
    }

    public Set<V> getNodes() {
        return nodeSet;
    }

    @Override
    public String toString() {
        return name.replace(".txt", "");
    }
}
