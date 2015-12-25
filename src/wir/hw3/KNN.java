package wir.hw3;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.linear.RealVector;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class KNN {

    public static void main(String[] args) throws IOException {
        // Prepare features and training set
        Set<String> features = new LinkedHashSet<>();
        Map<File, String> trainingSet = new LinkedHashMap<>();
        JSONObject data = new JSONObject(FileUtils.readFileToString(new File("data/hw3/features.json")));

        JSONArray featureArray = data.getJSONArray("features");
        for (int i=0; i<featureArray.length(); i++)
            features.add(featureArray.getString(i));

        // Iterate over classes
        String docFolder = data.getString("doc_folder");
        JSONObject labels = data.getJSONObject("classes");
        for (Iterator<String> iterator=labels.keys(); iterator.hasNext(); ) {
            String label = iterator.next();
            JSONArray docs = labels.getJSONArray(label);
            for (int i=0; i<docs.length(); i++)
                trainingSet.put(new File(docFolder, docs.getString(i)), label);
        }

        KNN knn = new KNN(2, features, trainingSet);
        System.out.println(knn);
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
     * Classifies a file
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
            String label = trainingSet.get(i).label;
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

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("===== Features ===== \n");
        for (String feature : features)
            builder.append(feature).append(", ");
        builder.delete(builder.lastIndexOf(","), builder.length()).append("\n");

        builder.append("===== Traning Set ===== \n");
        for (Document document : trainingSet)
            builder.append(document).append("\n");

        return builder.toString();
    }

    private double cosineSimilarity(Document doc1, Document doc2) {
        RealVector vec1 = doc1.getVector();
        RealVector vec2 = doc2.getVector();
        return (vec1.dotProduct(vec2)) / (vec1.getNorm() * vec2.getNorm());
    }
}
