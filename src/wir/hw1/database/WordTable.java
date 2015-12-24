package wir.hw1.database;

import java.sql.SQLException;

public class WordTable extends TwoColumnTable {
    private static WordTable instance;

    private WordTable() throws SQLException {
        super("word");
    }

    static WordTable getInstance() throws SQLException {
        if (instance == null)
            instance = new WordTable();
        return instance;
    }
}
