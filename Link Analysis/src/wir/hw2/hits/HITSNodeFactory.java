package wir.hw2.hits;

import wir.hw2.graph.node.AbstractNodeFactory;
import wir.hw2.graph.node.Node;


public class HITSNodeFactory extends AbstractNodeFactory {

    @Override
    protected Node createNode(int name) {
        return new HITSNode(name);
    }

}
