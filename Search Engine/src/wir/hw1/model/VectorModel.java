package wir.hw1.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import wir.hw1.data.Document;
import wir.hw1.data.Query;
import wir.hw1.database.Database;
import wir.hw1.util.QueryFactory;
import wir.hw1.util.RankingResultBuilder;


/**
 * Doc's score = tf*idf + (doc contains all tokens of the query ? 1 : 0) + fullMatchCount*COEF
 */
public class VectorModel extends AbstractModel {
    private static Map<Integer, Double> idfTable = new HashMap<>(); // <Word_ID, idf-value>
    private PreparedStatement stmt_select_df;


    public VectorModel(QueryFactory queryFactory) throws SQLException {
        super(queryFactory);
        stmt_select_df = Database.getConnection().prepareStatement("SELECT COUNT(`word_id`) FROM `term_frequency` WHERE `word_id`=?;");
    }

    @Override
    protected List<Document> getRankingResult(Query query) throws SQLException {
        List<Document> rankingResult = new ArrayList<>();
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
        ExecutorService[] executorServices = new ExecutorService[2];
        for (int i=0; i<executorServices.length; i++)
            executorServices[i] = Executors.newCachedThreadPool();
        try {
            Future<List<Document>> thread1 = executorServices[0].submit(() -> new RankingResultBuilder(idfTable, query).run("part0"));
            Future<List<Document>> thread2 = executorServices[1].submit(() -> new RankingResultBuilder(idfTable, query).run("part1"));
            rankingResult.addAll(thread1.get());
            rankingResult.addAll(thread2.get());
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            for (ExecutorService executorService : executorServices)
                executorService.shutdownNow();
        }
        return rankingResult;
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
