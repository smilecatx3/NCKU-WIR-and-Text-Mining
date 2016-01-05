package wir.hw3.cluster;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wir.hw3.Document;


public class HierarchicalCluster extends AbstractClusterAlgorithm {
    private int numClusters; // #clusters after merging

    public HierarchicalCluster(Set<String> features, List<File> files, int numClusters) {
        super(features, files);
        this.numClusters = numClusters;
        // At first, each document is itself a cluster
        int index = 0; // Represent for cluster's id
        for (Document doc : documentList.values()) {
            Cluster cluster = new Cluster();
            cluster.points.add(doc);
            clusters.put(String.valueOf(++index), cluster);
        }
    }

    @Override
    public Collection<Cluster> cluster() {
        // A structure that records the names of two clusters to be combined and the distance between them
        class CombinedCluster {
            String cluster1, cluster2;
            double distance = Double.MAX_VALUE;
        }

        // Merge the clusters until #clusters=numClusters
        while (clusters.size() > numClusters) {
            CombinedCluster combinedCluster = new CombinedCluster();
            // Compute the distance between each cluster
            for (Map.Entry<String, Cluster> cluster1 : clusters.entrySet()) {
                for (Map.Entry<String, Cluster> cluster2 : clusters.entrySet()) {
                    if (cluster1.getKey().equals(cluster2.getKey()))
                        continue;
                    // Use single-linkage agglomerative algorithm to determine the distance of two clusters
                    double minDistance = Double.MAX_VALUE;
                    for (Document doc1 : cluster1.getValue().points) {
                        for (Document doc2 : cluster2.getValue().points) {
                            double distance = doc1.getVector().getDistance(doc2.getVector());
                            minDistance = (distance < minDistance) ? distance : minDistance;
                        }
                    }
                    // Update the clusters to be combined
                    if (minDistance < combinedCluster.distance) {
                        combinedCluster.cluster1 = cluster1.getKey();
                        combinedCluster.cluster2 = cluster2.getKey();
                        combinedCluster.distance = minDistance;
                    }
                }
            }
            // Combine the two nearest clusters
            Cluster newCluster = clusters.get(combinedCluster.cluster1);
            newCluster.points.addAll(clusters.get(combinedCluster.cluster2).points);
            clusters.put(String.format("%s+%s", combinedCluster.cluster1, combinedCluster.cluster2), newCluster);
            clusters.remove(combinedCluster.cluster1);
            clusters.remove(combinedCluster.cluster2);
        }

        return clusters.values();
    }
}
