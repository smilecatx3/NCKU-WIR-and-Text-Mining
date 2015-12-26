package wir.hw3;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


public class Utils {

    public static Set<String> loadFeatures(String fileName) {
        Set<String> features = new LinkedHashSet<>();
        try {
            for (String feature : FileUtils.readFileToString(new File(fileName)).split(","))
                features.add(feature.trim());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return features;
    }

    public static List<File> loadTestSet(String fileName, String docFolder) {
        List<File> fileList = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            while (reader.ready()) {
                String line = reader.readLine();
                if (!line.startsWith("#") && line.length()>0)
                    fileList.add(new File(docFolder, line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileList;
    }

    public static JSONObject loadConfig(String fileName) {
        JSONObject config = null;
        try {
            config = new JSONObject(FileUtils.readFileToString(new File(fileName)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return config;
    }

}
