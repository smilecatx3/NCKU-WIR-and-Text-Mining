package wir.hw1.database;

import java.sql.SQLException;

public class DocumentTable extends TwoColumnTable {
    private static DocumentTable instance;

    private DocumentTable() throws SQLException {
        super("document");
    }

    static DocumentTable getInstance() throws SQLException {
        if (instance == null)
            instance = new DocumentTable();
        return instance;
    }

    /**
     * @param name The file name to be inserted into document table
     * @return The doc id, or -1 indicates the file already exists
     */
    @Override
    public synchronized int insert(String name) {
        int temp = nextIndex;
        int id = super.insert(name);
        return (temp == nextIndex) ? -1 : id;
    }
}
