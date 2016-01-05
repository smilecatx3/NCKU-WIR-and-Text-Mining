package wir.hw1.database;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


class TwoColumnTable extends AbstractTable {
    protected BidiMap<Integer, String> cacheData = new DualHashBidiMap<>(); // Data fetched from database
    protected BidiMap<Integer, String> newData = new DualHashBidiMap<>();   // For updating database
    protected int nextIndex;

    protected TwoColumnTable(String tableName) throws SQLException {
        super(tableName, String.format("INSERT INTO `%s`(`id`, `name`) VALUES ", tableName));
        // Fetch data from database
        Connection conn = Database.getConnection();
        try (ResultSet result = conn.createStatement().executeQuery(String.format("SELECT * FROM `%s`;", tableName))) {
            while (result.next())
                cacheData.put(result.getInt(1), result.getString(2));
        }
        nextIndex = cacheData.size();
    }

    @Override
    protected boolean hasNewData() {
        return newData.size() > 0;
    }

    @Override
    synchronized void updateDatabase() throws SQLException {
        super.updateDatabase();
        cacheData.putAll(newData);
        newData.clear();
        sql_updateDatabase = null;
    }

    public synchronized int insert(String name) {
        Integer cacheID = cacheData.getKey(name);
        Integer newID = newData.getKey(name);
        int id = (cacheID != null) ? cacheID : (newID != null) ? newID : -1;
        if (id == -1) {
            sql_updateDatabase.append(String.format("(%d, '%s'), ", nextIndex, name));
            newData.put(nextIndex, name);
            return nextIndex++;
        } else {
            return id;
        }
    }

    public int size() {
        assert (newData.size() == 0) : "New data have not updated to database yet.";
        return cacheData.size();
    }

    public int getID(String name) {
        assert (newData.size() == 0) : "New data have not updated to database yet.";
        Integer id = cacheData.getKey(name);
        return (id == null) ? -1 : id;
    }

    public String getName(int id) {
        assert (newData.size() == 0) : "New data have not updated to database yet.";
        return cacheData.get(id);
    }

}
