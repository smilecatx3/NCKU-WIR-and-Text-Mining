package wir.hw3.cluster;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wir.hw3.Document;


public class KMeans extends AbstractClusterAlgorithm {

    public static void main(String[] args) {

    }



    public KMeans(Set<String> features, List<File> files, List<File> initialClusterCenters) {
        super(features, files);
        for (int i=0; i<initialClusterCenters.size(); i++) {
            Document doc = documentList.get(initialClusterCenters.get(i));
            if (doc == null)
                throw new IllegalArgumentException("'files' should contain all the elements in 'initialClusterCenters'");
            clusters.put(String.valueOf(i), new Cluster(doc.getVector()));
        }
    }

    @Override
    public Collection<Cluster> cluster() {
        // Iterate until the clusters are stable
        boolean isStable; // isStable = whether there are any points moved to a new cluster
        do {
            Map<String, Cluster> newClusters = new HashMap<>();

            // Group the points to their nearest center
            isStable = true;
            for (Document doc : documentList.values()) {
                // Compute min distance to each cluster's center
                String oldCluster = doc.label;
                double min = Double.MAX_VALUE;
                for (Map.Entry<String, Cluster> entry : clusters.entrySet())
                    if (doc.getVector().getDistance(entry.getValue().vector) < min)
                        doc.label = entry.getKey();
                if (!doc.label.equals(oldCluster))
                    isStable = false;

                // Update new clusters
                if (!newClusters.containsKey(doc.label))
                    newClusters.put(doc.label, new Cluster(doc.getVector().getDimension()));
                Cluster cluster = newClusters.get(doc.label);
                cluster.vector.add(doc.getVector());
                cluster.points.add(doc);
            }

            // Update cluster's centers for next iteration
            for (Cluster cluster : newClusters.values())
                cluster.vector.mapDivide(cluster.points.size()); // New center's vector = average(each point's vector)
            clusters.clear();
            clusters.putAll(newClusters);
        } while (!isStable);

        return clusters.values();
    }
}
