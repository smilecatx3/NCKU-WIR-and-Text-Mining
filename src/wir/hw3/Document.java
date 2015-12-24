package wir.hw3;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Set;


public class Document implements Comparable<Document> {
    private String name;
    private double[] vector;
    private String label;
    private double score;

    public Document(File file, Set<String> features) {
        try {
            this.name = file.getName();
            this.vector = new double[features.size()];

            // Compute feature vector
            String content = FileUtils.readFileToString(file);
            int index = 0;
            for (String feature : features)
                this.vector[index++] = StringUtils.countMatches(content, feature);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Document(File file, Set<String> features, String label) {
        this(file, features);
        this.label = label;
    }

    public double[] getVector() {
        return vector;
    }

    public String getLabel() {
        return label;
    }

    public void updateScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass()) && name.equals(((Document) o).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public int compareTo(Document other) {
        return (int)(other.score - this.score);
    }
}
