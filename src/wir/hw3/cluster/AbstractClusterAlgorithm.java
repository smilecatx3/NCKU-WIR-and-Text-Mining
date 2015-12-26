package wir.hw3.cluster;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wir.hw3.Document;
import wir.hw3.Utils;


public abstract class AbstractClusterAlgorithm {

    public static void main(String[] args) throws IOException {
        String docFolder = Utils.loadConfig("data/hw3/config.json").getString("doc_folder");
        List<File> files = Utils.loadTestSet("data/hw3/testset2.txt", docFolder);
        List<File> initialClusterCenters = Utils.loadTestSet("data/hw3/testset2_init.txt", docFolder);
        Set<String> features = Utils.loadFeatures("data/hw3/features.txt");

//        AbstractClusterAlgorithm method = new KMeans(features, files, initialClusterCenters);
        AbstractClusterAlgorithm method = new HierarchicalCluster(features, files, 300);
        Collection<Cluster> clusters = method.cluster();
        int id = 0;
        for (Cluster cluster : clusters) {
            System.out.println(String.format("Cluster #%d (%d docs)", ++id, cluster.points.size()));
            for (Document doc : cluster.points)
                System.out.println(String.format("%s => %s", doc.getFile().getName(), Utils.formatVector(doc.getVector())));
            System.out.println();
        }
    }



    protected Map<File, Document> documentList = new HashMap<>();
    protected Map<String, Cluster> clusters = new HashMap<>(); // <Cluster_ID, Vector>

    protected AbstractClusterAlgorithm(Set<String> features, List<File> files) {
        for (File file : files)
            documentList.put(file, new Document(file, features));
    }

    abstract public Collection<Cluster> cluster();
}
