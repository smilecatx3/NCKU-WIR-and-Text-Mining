package wir.hw1.data;

public class Document implements Comparable<Document> {
    private String name;
    private double score;
    private String snippet = "(Not Available)";


    public Document(String name, double score, String snippet) {
        this.name = name;
        this.score = score;
        if (snippet != null)
            this.snippet = snippet;
    }

    @Override
    public int compareTo(Document doc) {
        return (doc.score>this.score) ? 1 : (doc.score<this.score) ? -1 : 0;
    }

    public String getName() {
        return name;
    }

    public double getScore() {
        return score;
    }

    public String getSnippet() {
        return snippet;
    }

}
