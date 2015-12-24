package wir.hw1.database;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Database {
    private static Connection conn;
    private static WordTable wordTable;
    private static DocumentTable documentTable;
    private static TermFrequencyTable termFrequencyTable;


    public static void initialize() throws Exception {
        JSONObject config = new JSONObject(FileUtils.readFileToString(new File("data/database.json")));
        Database.conn = DriverManager.getConnection(
                config.getString("url"),
                config.getString("user"),
                config.getString("passwd"));
        conn.setAutoCommit(false);

        Future init_wordTable = Executors.newSingleThreadExecutor().submit(() -> {wordTable=WordTable.getInstance(); return null;});
        Future init_docTable = Executors.newSingleThreadExecutor().submit(() -> {documentTable=DocumentTable.getInstance(); return null;});
        Future init_tfTable = Executors.newSingleThreadExecutor().submit(() -> {termFrequencyTable=TermFrequencyTable.getInstance(); return null;});
        init_wordTable.get();
        init_docTable.get();
        init_tfTable.get();

        System.out.println("Database initialization completed\n");
    }

    public static void updateDatabase() throws SQLException {
        wordTable.updateDatabase();
        documentTable.updateDatabase();
        termFrequencyTable.updateDatabase();
        try {
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        conn.rollback();
        }
    }

    public static Connection getConnection() {
        return conn;
    }

    public static WordTable getWordTable() {
        return wordTable;
    }

    public static DocumentTable getDocumentTable() {
        return documentTable;
    }

    public static TermFrequencyTable getTermFrequencyTable() {
        return termFrequencyTable;
    }
}
