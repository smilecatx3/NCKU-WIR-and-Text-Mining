package wir.hw2.simrank;

import wir.hw2.graph.node.Node;

import java.util.HashMap;
import java.util.Map;


public class NodePairFactory {
    private Map<String, NodePair> table = new HashMap<>();

    public NodePair create(Node node1, Node node2) {
        if (node1.compareTo(node2) > 0) {
            Node temp = node1;
            node1 = node2;
            node2 = temp;
        }
        String key = String.format("%s,%s", node1, node2);
        if (!table.containsKey(key))
            table.put(key, new NodePair(node1, node2));
        return table.get(key);
    }
}
