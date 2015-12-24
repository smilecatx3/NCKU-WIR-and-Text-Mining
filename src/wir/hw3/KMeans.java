package wir.hw3;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


public class KMeans {

    public static void main(String[] args) {

    }



    private Set<String> features;
    private Map<File, Document> documentList = new HashMap<>();

    public KMeans(Set<String> features) {
        this.features = features;
    }

    public Map<String, List<File>> cluster(List<File> files, List<File> initialClusterCenters) {
        // Initialization
        for (File file : files)
            documentList.put(file, new Document(file, features));

        RealVector[] clusterCenters = new RealVector[initialClusterCenters.size()];
        for (int i=0; i<clusterCenters.length; i++) {
            Document doc = documentList.get(initialClusterCenters.get(i));
            if (doc == null)
                throw new IllegalArgumentException("'files' should contain all the elements in 'initialClusterCenters'");
            clusterCenters[i] = new ArrayRealVector(doc.getVector());
        }

        // Iterate until the clusters are stable
        int moveCount; // # point moved to a new cluster
        do {
            moveCount = 0;
            for (Document doc : documentList.values()) {
                double min = Double.MAX_VALUE;
                for (int i=0 ; i<clusterCenters.length; i++) {
                    String oldCluster = doc.label;
                    if (doc.getVector().getDistance(clusterCenters[i]) < min)
                        if (!doc.label.equals(String.valueOf(i)))
                            doc.label = String.format("#%d", i+1);
                    moveCount += doc.label.equals(oldCluster) ? 0 : 1;
                }
            }
        } while (moveCount == 0);

        // Build result
        Map<String, List<File>> result = new TreeMap<>();
        for (Map.Entry<File, Document> entry : documentList.entrySet()) {
            String clusterName = entry.getValue().label;
            if (!result.containsKey(clusterName))
                result.put(clusterName, new ArrayList<>());
            result.get(clusterName).add(entry.getKey());
        }

        return result;
    }
}
