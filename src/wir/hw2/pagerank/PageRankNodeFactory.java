package wir.hw2.pagerank;

import wir.hw2.graph.node.AbstractNodeFactory;
import wir.hw2.graph.node.Node;


public class PageRankNodeFactory extends AbstractNodeFactory {

    @Override
    protected Node createNode(int name) {
        return new PageRankNode(name);
    }

}
