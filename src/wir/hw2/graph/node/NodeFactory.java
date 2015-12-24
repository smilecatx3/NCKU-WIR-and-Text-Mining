package wir.hw2.graph.node;

public class NodeFactory extends AbstractNodeFactory {

    @Override
    protected Node createNode(int name) {
        return new Node(name);
    }

}
