package wir.hw2.hits;

import org.apache.commons.io.FileUtils;

import wir.hw2.graph.Graph;
import wir.hw2.graph.GraphFactory;
import wir.hw2.graph.node.Node;
import wir.hw2.graph.node.INodeFactory;

import java.io.File;
import java.io.IOException;


public class HITS {

    public static void main(String[] args) throws IOException {
//        run("graph_1.txt");
//        run("graph_2.txt");
//        run("graph_3.txt");
//        run("graph_4.txt");
//        run("graph_5.txt");
//        run("graph_6.txt");
//        run("graph_LP1.txt");
//        run("graph_LP2.txt");
//        run("graph_movie.txt");
//        run("graph_petersen.txt");
        run("graph_1-1.txt");
        run("graph_2-1.txt");
        run("graph_3-1.txt");
        run("graph_4-1.txt");
    }

    private static void run(String fileName) throws IOException {
        System.out.print(String.format("Running '%s' ... ", fileName));
        HITS hits = new HITS(fileName);
        long startTime = System.currentTimeMillis();
        Graph graph = hits.compute();
        long elapsedTime = System.currentTimeMillis() - startTime;
        FileUtils.writeStringToFile(new File(String.format("HITS_%s.csv", graph)), hits.toString());
        System.out.println(String.format("done (%d ms)", elapsedTime));
    }


    private static final double THRESHOLD = 0.0001;
    private Graph<HITSNode> graph;
    private double previousAuthSum = 0;
    private double previousHubSum = 0;

    @SuppressWarnings("unchecked")
    public HITS(String graphFile) {
        INodeFactory nodeFactory = new HITSNodeFactory();
        GraphFactory graphFactory = new GraphFactory();
        this.graph = graphFactory.create(graphFile, nodeFactory);
    }

    public Graph<HITSNode> compute() {
        while ( (updateAuthorityScore() + updateHubScore()) > THRESHOLD );
        return graph;
    }

    private double updateAuthorityScore() {
        double norm = 0; // For normalization
        double sum = 0;
        for (HITSNode page1 : graph.getNodes()) {
            page1.authority = 0;
            for (Node page2 : page1.incomingNeighbors)
                page1.authority += ((HITSNode)page2).hub;
            norm += Math.pow(page1.authority, 2);
        }
        norm = Math.sqrt(norm);
        for (HITSNode page : graph.getNodes()) {
            page.authority /= norm;
            sum += page.authority;
        }

        double diff = Math.abs(sum - previousAuthSum);
        previousAuthSum = sum;
        return diff;
    }

    private double updateHubScore() {
        double norm = 0; // For normalization
        double sum = 0;
        for (HITSNode page1 : graph.getNodes()) {
            page1.hub = 0;
            for (Node page2 : page1.outgoingNeighbors)
                page1.hub += ((HITSNode)page2).authority;
            norm += Math.pow(page1.hub, 2);
        }
        norm = Math.sqrt(norm);
        for (HITSNode page : graph.getNodes()) {
            page.hub /= norm;
            sum += page.hub;
        }

        double diff = Math.abs(sum - previousHubSum);
        previousHubSum = sum;
        return diff;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("id,authority,hub\n");
        for (HITSNode node : graph.getNodes())
            builder.append(node).append(",").append(node.authority).append(",").append(node.hub).append("\n");
        return builder.toString();
    }
}
