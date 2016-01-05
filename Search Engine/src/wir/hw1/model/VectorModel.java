package wir.hw1.model;

import org.json.JSONObject;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import wir.hw1.data.Query;
import wir.hw1.util.FileSnippetExtractor;
import wir.hw1.data.Document;
import wir.hw1.util.QueryFactory;


/**
 * Doc's score = tf*idf + (doc contains all tokens of the query ? 1 : 0) + fullMatchCount*COEF
 */
public class VectorModel extends AbstractModel {
    private static Double COEF_FULL_MATCH = null;
    private static PreparedStatement stmt_select_df = null;
    private static Map<Integer, Double> idfTable = new HashMap<>(); // <Word_ID, idf-value>
    private List<Document> rankingResult = new ArrayList<>();
    private Query query;


    public VectorModel(QueryFactory queryFactory, JSONObject config) throws SQLException {
        super(queryFactory, config);
        // Initialize static fields
        if (COEF_FULL_MATCH == null) {
            COEF_FULL_MATCH = config.getJSONObject("param").getDouble("full_match");
            stmt_select_df = conn.prepareStatement("SELECT COUNT(`word_id`) FROM `term_frequency` WHERE `word_id`=?;");
        }
    }

    @Override
    protected List<Document> getRankingResult(Query query) throws SQLException {
        this.query = query;

        Collection<Integer> wordIDs = query.getTokens().values();
        initIdfTable(wordIDs);

        // Prepare sql statements (2 for running parallelly)
        String sql_wordID = sql_wordID(wordIDs);
        int middlePoint = (int)(0.5*db_documentTable.size());
        String sql1 = String.format(
                "SELECT * FROM `term_frequency` WHERE `doc_id`<%d AND %s ORDER BY `doc_id`;",
                middlePoint, sql_wordID);
        String sql2 = String.format(
                "SELECT * FROM `term_frequency` WHERE `doc_id`>=%d AND %s ORDER BY `doc_id`;",
                middlePoint, sql_wordID);

        // Start computing the score of each document
        ExecutorService[] executorService = new ExecutorService[2];
        for (int i=0; i<executorService.length; i++)
            executorService[i] = Executors.newSingleThreadExecutor();
        try {
            Future thread1 = executorService[0].submit(() -> {computeScore(sql1); return null;});
            Future thread2 = executorService[1].submit(() -> {computeScore(sql2); return null;});
            thread1.get();
            thread2.get();
            Collections.sort(rankingResult);
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return rankingResult;
    }

    private void computeScore(String sql) throws SQLException {
        Collection<Integer> wordIDs = this.query.getTokens().values();
        int numWords = wordIDs.size();

        // Compute score of each document
        try (ResultSet result = conn.createStatement().executeQuery(sql)) {
            result.setFetchSize(Integer.MAX_VALUE);
            int prevDocID = -1; // Record the previous doc_id of each iteration
            int count = 0; // #words in 'wordIDs' that doc contains
            double score = 0; // Doc's score

            while (result.next()) {
                int docID = result.getInt(2);
                double tf = result.getDouble(3);
                double idf = idfTable.get(result.getInt(1));
                // Encounter next document, add previous document to rankingList
                if ((docID != prevDocID) && (prevDocID != -1)) {
                    addDocToList(prevDocID, score, count==numWords);
                    score = count = 0;
                }
                score += tf*idf;
                count++;
                prevDocID = docID;
                // Last document
                if (result.isLast())
                    addDocToList(docID, score, count==numWords);
            }
        }
    }

    private void addDocToList(int docID, double score, boolean useFullMatch) {
        String docName = db_documentTable.getName(docID);
        String snippet = null; // Snippet of the search result
        if (useFullMatch) {
            FileSnippetExtractor extractor = new FileSnippetExtractor(new File(docFolder, docName), query);
            snippet = extractor.getSnippet();
            score += 1 + extractor.getFullMatchCount()*COEF_FULL_MATCH;
        }
        if (score > 0)
            rankingResult.add(new Document(docName, score, snippet));
    }

    private void initIdfTable(Collection<Integer> wordIDs) throws SQLException {
        int numDoc = db_documentTable.size();
        for (int wordID : wordIDs) {
            if (!idfTable.containsKey(wordID)) {
                stmt_select_df.setInt(1, wordID);
                try (ResultSet result = stmt_select_df.executeQuery()) {
                    result.next();
                    idfTable.put(wordID, (double)result.getInt(1)/numDoc);
                }
            }
        }
    }
}
