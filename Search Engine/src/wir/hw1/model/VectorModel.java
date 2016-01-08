package wir.hw1.model;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Collection<Integer> wordIDs = query.getTokens().values();
        initIdfTable(wordIDs);
        return new RankingResultBuilder(idfTable, query).run();
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
