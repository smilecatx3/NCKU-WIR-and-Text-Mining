package wir.hw1.model;

import wir.hw1.Util;
import wir.hw1.database.Database;
import wir.hw1.database.DocumentTable;
import wir.hw1.database.WordTable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class AbstractModel {
    private PreparedStatement stmt_select_df; //TODO move to subclass
    protected Connection conn = Database.getConnection();
    protected Map<Integer, Double> idfTable = new HashMap<>();
    protected DocumentTable documentTable = Database.getTable("document");
    protected WordTable wordTable = Database.getTable("word");

    protected abstract List<Document> computeScore(String query, List<Integer> wordIDs) throws Exception;

    protected AbstractModel() throws SQLException {
        stmt_select_df = conn.prepareStatement("SELECT COUNT(`word_id`) FROM `term_frequency` WHERE `word_id`=?;");
    }

    private List<Integer> splitQuery(String query) throws SQLException {
        String[] tokens = query.split("\\s+");
        List<Integer> wordIDs = new ArrayList<>();
        for (String token : tokens) {
            char firstChar = token.charAt(0);
            if (Util.isEnglishAlphabet(firstChar) || Character.isDigit(firstChar)) { // English word or number or date
                int wordID = wordTable.getID(token);
                if (wordID != -1)
                    wordIDs.add(wordID);
            } else {
                for (int i=0; i<token.length(); i++) {
                    int wordID = wordTable.getID(String.valueOf(token.charAt(i)));
                    if (wordID != -1)
                        wordIDs.add(wordID);
                }
            }
        }
        return wordIDs;
    }

    private void initializeIdfTable(List<Integer> wordIDs) throws SQLException {
        int numDoc = documentTable.size();
        for (int wordID : wordIDs) {
            stmt_select_df.setInt(1, wordID);
            try (ResultSet result = stmt_select_df.executeQuery()) {
                result.next();
                idfTable.put(wordID, (double)result.getInt(1)/numDoc);
            }
        }
    }

    public List<Document> getRankingResult(String query) throws Exception {
        System.out.print(String.format("Search \"%s\" ... ", query));
        List<Integer> wordIDs = splitQuery(query);
        initializeIdfTable(wordIDs);
        return (wordIDs.size() == 0) ? new ArrayList<>() : computeScore(query, wordIDs);
    }
}
