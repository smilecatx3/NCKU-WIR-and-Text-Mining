package wir.hw1.index;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import wir.hw1.database.Database;
import wir.hw1.database.DocumentTable;
import wir.hw1.database.TermFrequencyTable;
import wir.hw1.database.WordTable;

public class Indexer {
    private Tokenizer tokenizer;
    private int numFiles;
    private int progress = 0;


    public Indexer(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
    }

    /**
     * Indexes the files in dirPath into database
     * @param dirPath The directory path
     */
    public void run(String dirPath) {
        File[] files = new File(dirPath).listFiles();
        if (files == null)
            throw new IllegalArgumentException(String.format("%s is not a directory", dirPath));
        numFiles = files.length;
        System.out.println(String.format("Start indexing %d files into database ...", numFiles));

        // Split files into 2 subsets for indexing parallelly
        File[] subset1 = Arrays.copyOfRange(files, 0, files.length/2);
        File[] subset2 = Arrays.copyOfRange(files, files.length/2, files.length);
        assert (subset1.length + subset2.length) == files.length;

        ExecutorService[] executorServices = new ExecutorService[2];
        for (int i=0; i<executorServices.length; i++)
            executorServices[i] = Executors.newSingleThreadExecutor();
        try {
            long startTime = System.currentTimeMillis();
            Future[] runIndex = new Future[executorServices.length];
            runIndex[0] = executorServices[0].submit(() -> {index(subset1); return null;});
            runIndex[1] = executorServices[1].submit(() -> {index(subset2); return null;});
            for (Future future : runIndex)
                future.get();
            Database.update();
            System.out.println(String.format("Index completed (Elapsed %.2f seconds)", (System.currentTimeMillis()-startTime)/1000.0));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            for (ExecutorService executorService : executorServices)
                executorService.shutdownNow();
        }
     }

    /**
     * Indexes files into database
     * @param files The files to be indexed
     */
    private void index(File[] files) throws SQLException, IOException {
        DocumentTable db_documentTable = Database.getTable("document");
        WordTable db_wordTable = Database.getTable("word");
        TermFrequencyTable db_tfTable = Database.getTable("tf");

        for (File file : files) {
            System.out.println(String.format("(%d/%d) Indexing file '%s' ... ", ++progress, numFiles, file.getName()));

            // Insert document ID to database
            int docID = db_documentTable.insert(file.getName());
            if (docID == -1) {
                System.out.println(String.format("The file '%s' already exists. Skip", file.getName()));
                continue;
            }

            // Parse file
            List<String> words = tokenizer.tokenize(FileUtils.readFileToString(file));
            Map<String, Double> wordTable = new HashMap<>(); // <Word, #Occurrence>
            for (String word : words) {
                Double count = wordTable.get(word);
                count = (count == null) ? 0 : count;
                wordTable.put(word, count+1);
            }

            // Insert to database
            int numWords = words.size();
            for (Map.Entry<String, Double> entry : wordTable.entrySet()) {
                int wordID = db_wordTable.insert(entry.getKey());
                double tf = entry.getValue() / numWords;
                db_tfTable.insert(wordID, docID, tf);
            }
        }
    }

}
