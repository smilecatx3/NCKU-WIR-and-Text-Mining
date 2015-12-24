package wir.hw3;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class KNN {

    public static void main(String[] args) {

    }



    private int k;
    private Set<String> features;
    private List<Document> trainingSet = new ArrayList<>();

    public KNN(int k, Set<String> features, Map<File, String> trainingSet) {
        this.k = k;
        this.features = features;
        for (Map.Entry<File, String> entry : trainingSet.entrySet())
            this.trainingSet.add(new Document(entry.getKey(), features, entry.getValue()));
    }

    /**
     * Classifies the file to a class
     * @param file The file to be classified
     * @return The class label
     */
    public String classify(File file) {
        Document doc = new Document(file, features);

        // Compute the similarity to each document in the training set
        for (Document trainingDoc : trainingSet)
            trainingDoc.updateScore(cosineSimilarity(doc, trainingDoc));

        // Get the highest k similar docs
        Collections.sort(trainingSet);
        Map<String, Integer> labelTable = new HashedMap<>(); // <Label name, Count>
        for (int i=0; i<k; i++) {
            String label = trainingSet.get(i).getLabel();
            int count = (labelTable.get(label) == null) ? 0 : labelTable.get(label);
            labelTable.put(label, count+1);
        }

        // Infer the class by the max occurrence of label name
        String result = null;
        int max = -1;
        for (Map.Entry<String, Integer> entry : labelTable.entrySet())
            if (entry.getValue() > max)
                result = entry.getKey();
        return result;
    }

    private double cosineSimilarity(Document doc1, Document doc2) {
        RealVector vec1 = new ArrayRealVector(doc1.getVector());
        RealVector vec2 = new ArrayRealVector(doc2.getVector());
        return (vec1.dotProduct(vec2)) / (vec1.getNorm() * vec2.getNorm());
    }
}
