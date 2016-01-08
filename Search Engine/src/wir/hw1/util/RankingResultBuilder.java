package wir.hw1.util;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import wir.hw1.SearchEngine;
import wir.hw1.data.Document;
import wir.hw1.data.Query;
import wir.hw1.database.Database;
import wir.hw1.database.DocumentTable;

/** We need this class for multi-thread use */
public class RankingResultBuilder {
    private Connection conn = Database.getConnection();
    private DocumentTable db_documentTable = Database.getTable("document");
    private String docFolder = SearchEngine.getConfig().getString("doc_folder");
    private double COEF_FULL_MATCH = SearchEngine.getConfig().getJSONObject("param").getDouble("full_match");
    private Map<Integer, Double> idfTable;
    private Query query;


    public RankingResultBuilder(Map<Integer, Double> idfTable, Query query) {
        this.idfTable = idfTable;
        this.query = query;
    }

    public List<Document> run(String partition) throws SQLException, ExecutionException, InterruptedException {
        List<Integer> wordIDs = new ArrayList<>(query.getTokens().values());
        int numWords = wordIDs.size();
        Map<Integer, Map<Integer, Double>> data = new HashMap<>(); // <Word_ID, <Doc_ID, TF>>

        // Select data from database
        ExecutorService[] executorServices = new ExecutorService[numWords];
        List<Future<Map<Integer, Double>>> futures = new ArrayList<>();
        for (Integer wordID : wordIDs) {
            String sql = String.format("SELECT `doc_id`,`value` FROM `term_frequency` PARTITION(%s) WHERE `word_id`=%d;", partition, wordID);
            for (int i=0; i<executorServices.length; i++) {
                executorServices[i] = Executors.newCachedThreadPool();
                futures.add(executorServices[i].submit(() -> execSQL(sql)));
            }
        }
        for (int i=0; i<numWords; i++)
            data.put(wordIDs.get(i), futures.get(i).get());

        // Compute the score of each document
        Map<Integer, Double[]> docTable = new HashMap<>(); // <doc_ID, [tf*idf, #occurrence of word_id]>
        for (Map.Entry<Integer, Map<Integer, Double>> entry1 : data.entrySet()) {
            double idf = idfTable.get(entry1.getKey());
            for (Map.Entry<Integer, Double> entry2 : entry1.getValue().entrySet()) {
                int docID = entry2.getKey();
                if (!docTable.containsKey(docID))
                    docTable.put(docID, new Double[] {0.0, 0.0});
                Double[] values = docTable.get(docID);
                docTable.put(docID, new Double[] {values[0]+entry2.getValue()*idf, values[1]+1});
            }
        }

        // Build result
        List<Document> rankingResult = new ArrayList<>();
        for (Map.Entry<Integer, Double[]> entry : docTable.entrySet()) {
            String docName = db_documentTable.getName(entry.getKey());
            double score = entry.getValue()[0];
            int occurrence = entry.getValue()[1].intValue();

            String snippet = null; // Snippet of the search result
            if (occurrence == numWords) { // Use full-match
                FileSnippetExtractor extractor = new FileSnippetExtractor(new File(docFolder, docName), query);
                snippet = extractor.getSnippet();
                score += 1 + extractor.getFullMatchCount()*COEF_FULL_MATCH;
            }
            if (score > 0)
                rankingResult.add(new Document(docName, score, snippet));
        }
        return rankingResult;
    }

    private Map<Integer, Double> execSQL(String sql) throws SQLException {
        Map<Integer, Double> table = new HashMap<>(); // <Doc_ID, TF>
        try (ResultSet result = conn.createStatement().executeQuery(sql)) {
            result.setFetchSize(Integer.MAX_VALUE);
            while (result.next())
                table.put(result.getInt(1), Double.parseDouble(result.getString(2)));
        }
        return table;
    }
}
