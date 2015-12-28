package wir.hw3;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class TestSetCreator {

    public static void main(String[] args) {
        JSONObject config = Utils.loadConfig(args[0]);

        String docFolder = config.getString("doc_folder");
        Set<String> features = Utils.loadFeatures(config.getString("features"));
        TestSetCreator creator = new TestSetCreator(docFolder, features);
        Map<String, List<File>> testSet = creator.create();

        String outputFileName = config.getString("output_name");
        System.out.print(String.format("Writing to file '%s' ... ", outputFileName));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))){
            for (Map.Entry<String, List<File>> entry : testSet.entrySet()) {
                writer.write(String.format("# %s %n", entry.getKey()));
                for (File file : entry.getValue()) {
                    writer.write(file.getName());
                    writer.newLine();
                }
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done");
    }



    private File docFolder;
    private List<String> features;

    public TestSetCreator(String docFolder, Set<String> features) {
        this.docFolder = new File(docFolder);
        if (!this.docFolder.isDirectory())
            throw new IllegalArgumentException("The argument is not a directory");
        this.features = new ArrayList<>(features);
    }

    public Map<String, List<File>> create() {
        System.out.print("Creating test set ... [");
        Map<String, List<File>> result = new HashedMap<>();
        try {
            int progress = 0;
            for (File file : docFolder.listFiles()) {
                String feature = features.get((int)(Math.random()*features.size())); // Random select a feature
                if (FileUtils.readFileToString(file).contains(feature)) {
                    if (!result.containsKey(feature))
                        result.put(feature, new ArrayList<>());
                    result.get(feature).add(file);
                }
                if (++progress%3000 == 0)
                    System.out.print(".");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("] Done");
        return result;
    }
}
