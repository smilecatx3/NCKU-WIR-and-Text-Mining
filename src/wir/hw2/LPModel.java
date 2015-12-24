package wir.hw2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LPModel {

    public static void main(String[] args) {
        LPModel model = new LPModel(5, 5);
        model.createGraph(true, "graph_LP1.txt");
        model.createGraph(false, "graph_LP2.txt");
    }


    private int width;
    private int height;

    public LPModel(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void createGraph(boolean organized, String outputFile) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
            for (int i=0; i<height; i++) {
                for (int j=1; j<=width; j++) {
                    int index = j + i*width;
                    if (index-width > 0) // up
                        writer.write(String.format("%d,%d%n", index, index-width));
                    if (j-1 > 0) // left
                        writer.write(String.format("%d,%d%n", index, index-1));
                    if (j+1 <= width) { // right
                        if (organized)
                            writer.write(String.format("%d,%d%n", index, index+1));
                        else
                            writer.write(String.format("%d,%d%n", index, i*width + width)); // rewire to rightmost
                    }
                    if (index+width <= height*width) // down
                        writer.write(String.format("%d,%d%n", index, index+width));
                }
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
