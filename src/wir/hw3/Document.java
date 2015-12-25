package wir.hw3;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.io.File;
import java.io.IOException;
import java.util.Set;


public class Document implements Comparable<Document> {
    private File file;
    private RealVector vector;
    public String label;
    private double score;

    public Document(File file, Set<String> features) {
        try {
            this.file = file;

            // Compute feature vector
            String content = FileUtils.readFileToString(file);
            double[] vector = new double[features.size()];
            int index = 0;
            for (String feature : features)
                vector[index++] = StringUtils.countMatches(content, feature);
            this.vector = new ArrayRealVector(vector);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Document(File file, Set<String> features, String label) {
        this(file, features);
        this.label = label;
    }

    public RealVector getVector() {
        return vector;
    }

    public void updateScore(double score) {
        this.score = score;
    }

    @Override
    public int compareTo(Document other) {
        return (int)(other.score - this.score);
    }

    @Override
    public String toString() {
        return String.format("Label: \"%s\"; File: \"%s\"; Vector: \"%s\"", label, file.getName(), vector);
    }
}
