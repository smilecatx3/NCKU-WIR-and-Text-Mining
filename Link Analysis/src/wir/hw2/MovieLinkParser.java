package wir.hw2;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


// http://www.imdb.com/interfaces
public class MovieLinkParser {

    public static void main(String[] args) {
        MovieLinkParser parser = new MovieLinkParser();
        parser.parse("movie-links.list", "graph_movie.txt");
    }


    private int count = 1; // number of movies
    private Map<String, Integer> idTable = new HashMap<>();
    private Map<Integer, List<String>> movieTable = new TreeMap<>();

    public void parse(String inputFile, String outputFile) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile), "ISO8859_1"))) {
            Pattern pattern_name = Pattern.compile("(.+) \\(\\d+\\).*");
            Pattern pattern_link = Pattern.compile(".*followed by (.+) \\(\\d+\\).*");
            while (reader.ready()) {
                String line = reader.readLine();
                Matcher matcher = pattern_name.matcher(line);
                if (matcher.matches()) {
                    List<String> list = updateTable(matcher.group(1));
                    while ((line = reader.readLine()).length() > 0) {
                        matcher = pattern_link.matcher(line);
                        if (matcher.matches())
                            list.add(matcher.group(1));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
            int count = 0; // just dump 150 nodes
            for (Map.Entry<Integer, List<String>> entry : movieTable.entrySet()) {
                int id = entry.getKey();
                for (String link : entry.getValue()) {
                    writer.write(String.format("%d,%d", id, idTable.get(link)));
                    writer.newLine();
                }

                if (entry.getValue().size() > 0)
                    count++;
                if (count >= 150)
                    break;
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<String> updateTable(String name) {
        if (!idTable.containsKey(name)) {
            idTable.put(name, count++);
            movieTable.put(idTable.get(name), new ArrayList<>());
        }
        return movieTable.get(idTable.get(name));
    }

}
