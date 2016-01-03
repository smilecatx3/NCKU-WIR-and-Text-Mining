package wir.hw1.model;

import org.json.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import wir.hw1.data.Document;
import wir.hw1.data.Query;
import wir.hw1.database.Database;
import wir.hw1.database.DocumentTable;
import wir.hw1.util.QueryFactory;

public abstract class AbstractModel {
    private QueryFactory queryFactory;
    protected static JSONObject config;
    protected static String docFolder;
    protected Connection conn = Database.getConnection();
    protected DocumentTable db_documentTable = Database.getTable("document");


    public AbstractModel(QueryFactory queryFactory, JSONObject config) {
        this.queryFactory = queryFactory;
        if (AbstractModel.config == null) {
            AbstractModel.config = config;
            docFolder = config.getString("doc_folder");
        }
    }

    public List<Document> getRankingResult(String query) throws SQLException {
        System.out.println(String.format("Search \"%s\" ... ", query));
        Query q = queryFactory.create(query);
        return q.isValid() ? getRankingResult(q) : new ArrayList<>();
    }

    /** Constructs SQL statement of selecting wordIDs */
    protected String sql_wordID(Collection<Integer> wordIDs) {
        StringBuilder sql_wordID = new StringBuilder("(");
        for (Integer wordID : wordIDs)
            sql_wordID.append("`word_id`=").append(wordID).append(" OR ");
        sql_wordID.delete(sql_wordID.length()-4, sql_wordID.length()).append(")");
        return sql_wordID.toString();
    }

    /**
     * Given the query, computes the scores of all documents in the database.
     * @return Ranking result (a sorted document list)
     */
    protected abstract List<Document> getRankingResult(Query query) throws SQLException;
}
