package wir.hw1.database;


import java.sql.SQLException;

public class TermFrequencyTable extends AbstractTable {
    private static TermFrequencyTable instance;
    private String sql;

    private TermFrequencyTable() throws SQLException {
        super("term_frequency", "INSERT INTO `term_frequency`(`word_id`, `doc_id`, `value`) VALUES ");
        this.sql = "INSERT INTO `term_frequency`(`word_id`, `doc_id`, `value`) VALUES ";
    }

    static TermFrequencyTable getInstance() throws SQLException {
        if (instance == null)
            instance = new TermFrequencyTable();
        return instance;
    }

    @Override
    protected boolean hasNewData() {
        return sql_updateDatabase.length() > sql.length();
    }

    @Override
    synchronized void updateDatabase() throws SQLException {
        super.updateDatabase();
        sql_updateDatabase = new StringBuffer(sql);
    }

    public synchronized void insert(int word_id, int doc_id, double tf) throws SQLException {
        sql_updateDatabase.append(String.format("(%d, %d, %f), ", word_id, doc_id, tf));
        if (sql_updateDatabase.length() > 50_000_000)
            updateDatabase();
    }

}
