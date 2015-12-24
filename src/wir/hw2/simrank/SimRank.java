package wir.hw2.simrank;

import org.apache.commons.io.FileUtils;
import wir.hw2.graph.Graph;
import wir.hw2.graph.GraphFactory;
import wir.hw2.graph.node.INodeFactory;
import wir.hw2.graph.node.Node;
import wir.hw2.graph.node.NodeFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;


public class SimRank {

    public static void main(String[] args) throws IOException {
        run("graph_1.txt");
        run("graph_2.txt");
        run("graph_3.txt");
        run("graph_4.txt");
        run("graph_5.txt");
    }

    private static void run(String fileName) throws IOException {
        System.out.print(String.format("Running '%s' ... ", fileName));
        SimRank simRank = new SimRank(fileName);
        long startTime = System.currentTimeMillis();
        simRank.compute();
        long elapsedTime = System.currentTimeMillis() - startTime;
        FileUtils.writeStringToFile(new File(String.format("SimRank_%s.csv", fileName.replace(".txt", ""))), simRank.toString());
        System.out.println(String.format("done (%d ms)", elapsedTime));
    }


    private double decayFactor;
    private Map<NodePair, Double> scoreTable = new TreeMap<>();
    private NodePairFactory nodePairFactory = new NodePairFactory();

    public SimRank(String graphFile) {
        this(graphFile, 0.6);
    }

    @SuppressWarnings("unchecked")
    public SimRank(String graphFile, double decayFactor) {
        if ( (decayFactor < 0) || (decayFactor > 1) )
            throw new IllegalArgumentException("The decay factor should be in the range '0 <= C <= 1'");

        INodeFactory nodeFactory = new NodeFactory();
        GraphFactory graphFactory = new GraphFactory();
        Graph<Node> graph = graphFactory.create(graphFile, nodeFactory);
        this.decayFactor = decayFactor;

        for (Node page1 : graph.getNodes()) {
            for (Node page2 : graph.getNodes()) {
                if (page1.compareTo(page2) == 0)
                    scoreTable.put(nodePairFactory.create(page1, page2), 1.0);
                else if (page1.compareTo(page2) < 0)
                    scoreTable.put(nodePairFactory.create(page1, page2), 0.0);
            }
        }
    }

    public Map<NodePair, Double> compute() {
        final int K = 5; // Iteration times
        for (int i=0; i<K; i++) {
            for (Map.Entry<NodePair, Double> entry : scoreTable.entrySet()) {
                NodePair pair = entry.getKey();
                if (pair.node1 == pair.node2)
                    continue;
                int size1 = pair.node1.incomingNeighbors.size();
                int size2 = pair.node2.incomingNeighbors.size();
                double score = (size1*size2 == 0) ? 0 : decayFactor / (size1*size2) * getScore(pair);
                scoreTable.put(pair, score);
            }
        }
        return scoreTable;
    }

    private double getScore(NodePair pair) {
        double sum = 0;
        for (Node page1 : pair.node1.incomingNeighbors)
            for (Node page2 : pair.node2.incomingNeighbors)
                sum += scoreTable.get(nodePairFactory.create(page1, page2));
        return sum;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("node1,node2,similarity\n");
        for (Map.Entry<NodePair, Double> entry : scoreTable.entrySet()) {
            NodePair nodePair = entry.getKey();
            builder.append(nodePair.node1).append(",").append(nodePair.node2).append(",").append(entry.getValue()).append("\n");
        }
        return builder.toString();
    }
}
