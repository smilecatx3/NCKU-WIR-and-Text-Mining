package wir.hw3.classify;

import org.apache.commons.io.FileUtils;
import org.apache.commons.math3.linear.RealVector;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import wir.hw3.Document;
import wir.hw3.Utils;


public class KNN {

    public static void main(String[] args) throws IOException {
        JSONObject config = Utils.loadConfig(args[0]);

        // Prepare training set
        Map<File, String> trainingSet = new LinkedHashMap<>(); // [File, Label]
        String docFolder = config.getString("doc_folder");
        JSONObject data = new JSONObject(FileUtils.readFileToString(new File(config.getString("knn_training_set"))));
        for (Iterator<String> iterator=data.keys(); iterator.hasNext(); ) {
            String label = iterator.next();
            JSONArray docs = data.getJSONArray(label);
            for (int i=0; i<docs.length(); i++)
                trainingSet.put(new File(docFolder, docs.getString(i)), label);
        }

        Set<String> features = Utils.loadFeatures(config.getString("features"));
        List<File> testSet = Utils.loadTestSet(config.getString("test_set"), docFolder);

        long start = System.currentTimeMillis();
        KNN knn = new KNN(config.getInt("k"), features, trainingSet);
        System.out.println(knn);
        testSet.forEach(knn::classify);
        System.out.println(String.format("Elapsed %d ms", System.currentTimeMillis() - start));
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
     * @param file The file to be classified
     * @return The class label
     */
    public String classify(File file) {
        // A helper structure that helps determine the max occurrence of labels
        class Label {
            String name; Integer count=0;
            public Label(String name) { this.name = name; }
        }

        // Compute the similarity to each document in the training set
        Document doc = new Document(file, features);
        for (Document trainingDoc : trainingSet)
            trainingDoc.score = cosineSimilarity(doc, trainingDoc);

        // Get the highest k similar docs
        trainingSet.sort((doc1, doc2) -> doc2.score.compareTo(doc1.score));
        Map<String, Label> labelTable = new HashMap<>(); // [Label name, Label]
        for (int i=0; i<k; i++) {
            if (trainingSet.get(i).score > 0) { // Ignore the docs that its similarity is 0
                String label = trainingSet.get(i).label;
                if (!labelTable.containsKey(label))
                    labelTable.put(label, new Label(label));
                labelTable.get(label).count++;
            }
        }

        // Get the label with max occurrence
        List<Label> labels = new ArrayList<>(labelTable.values());
        labels.sort((label1, label2) -> label2.count.compareTo(label1.count));
        String label = (labels.size() == 0) ? "N/A" : labels.get(0).name;
        System.out.println(String.format("%s => %s; vector: %s", doc.getFile().getName(), label, Utils.formatVector(doc.getVector())));
        return label;
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
