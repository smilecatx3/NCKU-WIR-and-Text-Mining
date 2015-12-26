package wir.hw3.cluster;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wir.hw3.Document;


public abstract class AbstractClusterAlgorithm {
    protected Map<File, Document> documentList = new HashMap<>();
    protected Map<String, Cluster> clusters = new HashMap<>(); // <Cluster_ID, Vector>

    protected AbstractClusterAlgorithm(Set<String> features, List<File> files) {
        for (File file : files)
            documentList.put(file, new Document(file, features));
    }

    abstract public Collection<Cluster> cluster();
}
