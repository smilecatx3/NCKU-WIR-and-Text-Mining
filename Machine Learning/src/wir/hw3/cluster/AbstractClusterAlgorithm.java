package wir.hw3.cluster;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wir.hw3.Utils;
import wir.hw3.Document;


public abstract class AbstractClusterAlgorithm {

    public static void main(String[] args) throws IOException {
        JSONObject config = Utils.loadConfig(args[0]);

        String docFolder = config.getString("doc_folder");
        Set<String> features = Utils.loadFeatures(config.getString("features"));
        List<File> files = Utils.loadTestSet(config.getString("test_set"), docFolder);
        List<File> initialClusterCenters = Utils.loadTestSet(config.getString("kmeans_init_center"), docFolder);

        long start = System.currentTimeMillis();
        AbstractClusterAlgorithm method = config.getString("cluster_method").equals("kmeans") ?
                new KMeans(features, files, initialClusterCenters) :
                new HierarchicalCluster(features, files, config.getInt("num_clusters"));
        Collection<Cluster> clusters = method.cluster();
        System.out.println(String.format("Elapsed %d ms%n", System.currentTimeMillis() - start));

        int id = 0;
        for (Cluster cluster : clusters) {
            System.out.println(String.format("Cluster #%d (%d docs)", ++id, cluster.points.size()));
            for (Document doc : cluster.points)
                System.out.println(String.format("%s => %s", doc.getFile().getName(), Utils.formatVector(doc.getVector())));
            System.out.println();
        }
    }



    protected Map<File, Document> documentList = new HashMap<>();
    protected Map<String, Cluster> clusters = new HashMap<>(); // [Cluster_ID, Vector]

    protected AbstractClusterAlgorithm(Set<String> features, List<File> files) {
        for (File file : files)
            documentList.put(file, new Document(file, features));
    }

    abstract public Collection<Cluster> cluster();
}
