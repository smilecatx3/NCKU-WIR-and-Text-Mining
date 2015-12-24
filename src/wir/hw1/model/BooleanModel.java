package wir.hw1.model;

import org.apache.commons.lang3.StringUtils;
import wir.hw1.SearchEngine;
import wir.hw1.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BooleanModel extends AbstractModel {
    public BooleanModel() throws SQLException { }

    @Override
    protected List<Document> computeScore(String query, List<Integer> wordIDs) throws Exception {
        List<Document> rankingList = new ArrayList<>();
        StringBuilder sql_wordID = new StringBuilder("(");
        for (Integer wordID : wordIDs)
            sql_wordID.append("`word_id`=").append(wordID).append(" OR ");
        sql_wordID.delete(sql_wordID.length()-4, sql_wordID.length()).append(")");
        String sql = String.format(
                "SELECT `doc_id` FROM " +
                    "(SELECT `doc_id`,COUNT(`word_id`) num FROM `term_frequency` WHERE %S GROUP BY `doc_id`) result " +
                "WHERE result.num=%d ORDER BY `doc_id`;", sql_wordID, wordIDs.size());
        try (ResultSet result = conn.createStatement().executeQuery(sql)) {
            result.setFetchSize(Integer.MAX_VALUE);
            while (result.next()) {
                String docName = documentTable.getName(result.getInt(1));
                String fileContent = Util.readFile(SearchEngine.DOC_DIR + docName, false);
                double score = 1;
                String snippet = null;
                int index = fileContent.indexOf(query);
                if (index >= 0) {
                    snippet = fileContent.substring(Math.max(0, index-50), Math.min(index+50, fileContent.length()));
                    score += StringUtils.countMatches(fileContent, query);
                }
                rankingList.add(new Document(docName, score, snippet));
            }
        }
        Collections.sort(rankingList);
        return rankingList;
    }
}
