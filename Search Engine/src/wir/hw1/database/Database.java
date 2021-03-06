package wir.hw1.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Database {
    private static Connection conn;
    private static Map<String, AbstractTable> tables = new HashMap<>();

    public static void initialize(String url, String user, String password) throws SQLException, ClassNotFoundException {
        // Read config and establish connection to database
        System.out.println(String.format("Establishing database connection to '%s' ...", url));
        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection(url, user, password);
        conn.setAutoCommit(false);

        // Initialize database tables
        System.out.println("Initializing database tables ...");
        ExecutorService[] executorServices = new ExecutorService[3];
        for (int i=0; i<executorServices.length; i++)
            executorServices[i] = Executors.newSingleThreadExecutor();
        try {
            Future[] initTables = new Future[executorServices.length];
            initTables[0] = executorServices[0].submit(() -> {tables.put("word", WordTable.getInstance()); return null;});
            initTables[1] = executorServices[1].submit(() -> {tables.put("document", DocumentTable.getInstance()); return null;});
            initTables[2] = executorServices[2].submit(() -> {tables.put("tf", TermFrequencyTable.getInstance()); return null;});
            for (Future future : initTables)
                future.get();
            System.out.println("Database initialization completed");
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            for (ExecutorService executorService : executorServices)
                executorService.shutdownNow();
        }
    }

    public static void update() throws SQLException {
        try {
            for (AbstractTable table : tables.values())
                table.updateDatabase();
            conn.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            conn.rollback();
        }
    }

    public static Connection getConnection() {
        return conn;
    }

    @SuppressWarnings("unchecked")
    public static <T extends AbstractTable> T getTable(String name) {
        return (T)tables.get(name);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        conn.close();
    }
}
