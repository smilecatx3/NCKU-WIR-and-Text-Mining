package wir.hw2.graph.node;

import java.util.HashMap;
import java.util.Map;


public abstract class AbstractNodeFactory implements INodeFactory {
    private Map<Integer, Node> nodeTable = new HashMap<>();


    protected abstract Node createNode(int name);

    @Override
    public Node create(int name) {
        if (!nodeTable.containsKey(name))
            nodeTable.put(name, createNode(name));
        return nodeTable.get(name);
    }
}
