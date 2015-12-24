package wir.hw1.model;

public class Document implements Comparable {
    private String name;
    private double score;
    private String snippet;

    Document(String name, double score, String snippet) {
        this.name = name;
        this.score = score;
        this.snippet = (snippet==null) ? "(Not Available)" : snippet;
    }

    @Override
    public int compareTo(Object o) {
        Document doc = (Document)o;
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
