package wir.hw2.pagerank;

import org.apache.commons.io.FileUtils;
import wir.hw2.graph.Graph;
import wir.hw2.graph.GraphFactory;
import wir.hw2.graph.node.INodeFactory;
import wir.hw2.graph.node.Node;

import java.io.File;
import java.io.IOException;


public class PageRank {

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
//        run("graph_1-2.txt");
        run("graph_1-2.txt");
        run("graph_2-2.txt");
        run("graph_3-2.txt");
        run("graph_4-2.txt");
    }

    private static void run(String fileName) throws IOException {
        System.out.print(String.format("Running '%s' ... ", fileName));
        PageRank pageRank = new PageRank(fileName, 0.85);
        long startTime = System.currentTimeMillis();
        Graph<PageRankNode> graph = pageRank.compute();
        long elapsedTime = System.currentTimeMillis() - startTime;
        FileUtils.writeStringToFile(new File(String.format("PageRank_%s.csv", graph)), pageRank.toString());
        System.out.println(String.format("done (%d ms)", elapsedTime));
    }


    private static final double THRESHOLD = 0.0001;
    private Graph<PageRankNode> graph;
    private int numPages;
    private double dampingFactor;

    @SuppressWarnings("unchecked")
    public PageRank(String graphFile, double dampingFactor) {
        if ( (dampingFactor < 0.85) || (dampingFactor >= 1) )
            throw new IllegalArgumentException("The damping factor should be in the range '0.85 <= d < 1'");

        INodeFactory nodeFactory = new PageRankNodeFactory();
        GraphFactory graphFactory = new GraphFactory();
        this.graph = graphFactory.create(graphFile, nodeFactory);
        this.numPages = graph.getNodes().size();
        this.dampingFactor = dampingFactor;

        for (PageRankNode page : graph.getNodes())
            page.score = 1.0 / numPages;
    }

    public Graph<PageRankNode> compute() {
        double pageRankSum = 0;
        double previousPageRankSum;
        do {
            previousPageRankSum = pageRankSum;
            pageRankSum = 0;
            for (PageRankNode page : graph.getNodes()) {
                page.score = ((1 - dampingFactor) / numPages);
                for (Node inPage : page.incomingNeighbors)
                    page.score += dampingFactor * getScore((PageRankNode)inPage);
                pageRankSum += page.score;
            }
        } while (Math.abs(pageRankSum - previousPageRankSum) > THRESHOLD);

        return graph;
    }

    private double getScore(PageRankNode page) {
        int outDegree = (page.outgoingNeighbors.size() == 0) ? this.numPages : page.outgoingNeighbors.size();
        return page.score / outDegree;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("id,score\n");
        for (PageRankNode node : graph.getNodes())
            builder.append(node).append(",").append(node.score).append("\n");
        return builder.toString();
    }
}
