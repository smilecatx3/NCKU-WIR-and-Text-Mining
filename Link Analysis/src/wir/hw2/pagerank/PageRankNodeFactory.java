package wir.hw2.pagerank;

import wir.hw2.graph.node.Node;
import wir.hw2.graph.node.AbstractNodeFactory;


public class PageRankNodeFactory extends AbstractNodeFactory {

    @Override
    protected Node createNode(int name) {
        return new PageRankNode(name);
    }

}
