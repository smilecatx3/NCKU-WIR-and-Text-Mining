package wir.hw1.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import wir.hw1.data.Document;
import wir.hw1.data.Query;
import wir.hw1.database.Database;
import wir.hw1.database.DocumentTable;
import wir.hw1.util.QueryFactory;

public abstract class AbstractModel {
    private QueryFactory queryFactory;
    protected DocumentTable db_documentTable = Database.getTable("document");

    public AbstractModel(QueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    public List<Document> getRankingResult(String query) throws SQLException {
        System.out.println(String.format("Search \"%s\" ... ", query));
        Query q = queryFactory.create(query);
        List<Document> rankingResult = q.isValid() ? getRankingResult(q) : new ArrayList<>();
        Collections.sort(rankingResult);
        System.out.println(String.format("Got %d results.", rankingResult.size()));
        return rankingResult;
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
