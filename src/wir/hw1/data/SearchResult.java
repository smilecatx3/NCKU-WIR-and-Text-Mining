package wir.hw1.data;

import java.util.List;

public class SearchResult {
    private List<Document> documents;
    private double elapsedMillis;

    public SearchResult(List<Document> documents, double elapsedMillis) {
        this.documents = documents;
        this.elapsedMillis = elapsedMillis;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public double getElapsedMillis() {
        return elapsedMillis;
    }
}