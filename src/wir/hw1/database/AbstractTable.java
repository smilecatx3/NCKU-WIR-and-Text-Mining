package wir.hw1.database;

import java.sql.Connection;
import java.sql.SQLException;


abstract class AbstractTable {
    protected StringBuffer sql_updateDatabase;

    protected abstract boolean hasNewData();

    protected AbstractTable(String tableName, String sql) throws SQLException {
        System.out.println(String.format("Initializing table `%s` ...", tableName));
        sql_updateDatabase = new StringBuffer(sql);
    }

    synchronized void updateDatabase() throws SQLException {
        assert (sql_updateDatabase != null) : "The SQL statement for updating database is not initialized.";
        if (!hasNewData())
            return;
        Connection conn = Database.getConnection();
        String sql = sql_updateDatabase.deleteCharAt(sql_updateDatabase.lastIndexOf(",")).toString();
        if (sql.length() > 180)
            System.out.println("Updating database ... SQL = " + sql.substring(0, 180) + " ...");
        else
            System.out.println("Updating database ... SQL = " + sql);
        conn.createStatement().executeUpdate(sql);
    }
}
