package wir.hw1.database;

import java.sql.SQLException;

public class DocumentTable extends TwoColumnTable {
    private static DocumentTable instance;

    private DocumentTable() throws SQLException {
        super("document");
    }

    public static DocumentTable getInstance() throws SQLException {
        if (instance == null)
            instance = new DocumentTable();
        return instance;
    }

    @Override
    public synchronized int insert(String name) {
        int temp = nextIndex;
        int id = super.insert(name);
        return (temp == nextIndex) ? -1 : id;
    }
}
