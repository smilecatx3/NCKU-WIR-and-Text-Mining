package wir.hw1.model;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import wir.hw1.SearchEngine;
import wir.hw1.data.Document;
import wir.hw1.data.Query;
import wir.hw1.database.Database;
import wir.hw1.util.FileSnippetExtractor;
import wir.hw1.util.QueryFactory;

/**
 * Doc's score = doc contains all tokens of the query ? (fullMatchCount + partialMatchCount*COEF) : 0
 */
public class BooleanModel extends AbstractModel {
    private static double COEF_PARTIAL_MATCH = SearchEngine.getConfig().getJSONObject("param").getDouble("partial_match");
    private static String docFolder = SearchEngine.getConfig().getString("doc_folder");


    public BooleanModel(QueryFactory queryFactory) {
        super(queryFactory);
    }

    @Override
    protected List<Document> getRankingResult(Query query) throws SQLException {
        List<Document> rankingList = new ArrayList<>();
        Collection<Integer> wordIDs = query.getTokens().values();

        // Prepare sql statement
        String sql_wordID = sql_wordID(wordIDs);
        String sql = String.format(
                "SELECT `doc_id` FROM " +
                        "(SELECT `doc_id`,COUNT(`word_id`) num FROM `term_frequency` WHERE %S GROUP BY `doc_id`) result " +
                        "WHERE result.num=%d ORDER BY `doc_id`;", sql_wordID, wordIDs.size());

        try (ResultSet result = Database.getConnection().createStatement().executeQuery(sql)) {
            result.setFetchSize(Integer.MAX_VALUE);
            while (result.next()) {
                String docName = db_documentTable.getName(result.getInt(1));
                FileSnippetExtractor extractor = new FileSnippetExtractor(new File(docFolder, docName), query);
                String snippet = extractor.getSnippet();
                double score = extractor.getFullMatchCount();
                score += extractor.getPartialMatchCount()* COEF_PARTIAL_MATCH;
                if (score > 0)
                    rankingList.add(new Document(docName, score, snippet));
            }
        }
        return rankingList;
    }

}
