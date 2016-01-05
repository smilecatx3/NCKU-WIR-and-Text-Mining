package wir.hw3.cluster;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

import java.util.ArrayList;
import java.util.List;

import wir.hw3.Document;


public class Cluster {
    public RealVector vector;
    public List<Document> points = new ArrayList<>();


    public Cluster() { }

    public Cluster(int vectorSize) {
        vector = new ArrayRealVector(vectorSize);
    }

    public Cluster(RealVector vector) {
        this.vector = vector;
    }
}
