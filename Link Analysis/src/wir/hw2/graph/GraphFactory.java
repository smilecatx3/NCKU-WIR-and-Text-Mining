package wir.hw2.graph;

import wir.hw2.graph.node.INodeFactory;
import wir.hw2.graph.node.Node;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


public class GraphFactory {

    @SuppressWarnings("unchecked")
    public Graph create(String graphFile, INodeFactory factory) {
        Graph graph = new Graph(graphFile);
        try (BufferedReader reader = new BufferedReader(new FileReader(graphFile))){
            while (reader.ready()) {
                String[] nodes = reader.readLine().split(",");
                Node node1 = factory.create(Integer.parseInt(nodes[0]));
                Node node2 = factory.create(Integer.parseInt(nodes[1]));
                node1.outgoingNeighbors.add(node2);
                node2.incomingNeighbors.add(node1);
                graph.addNode(node1);
                graph.addNode(node2);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return graph;
    }

}
