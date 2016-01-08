package wir.hw1;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.sql.SQLException;
import java.util.Scanner;

import wir.hw1.data.SearchResult;
import wir.hw1.database.Database;
import wir.hw1.index.Indexer;
import wir.hw1.model.AbstractModel;
import wir.hw1.model.BooleanModel;
import wir.hw1.model.VectorModel;
import wir.hw1.util.QueryFactory;

public class SearchEngine {
    public enum ModelType { BOOLEAN_MODEL, VECTOR_MODEL }
    private static SearchEngine instance;
    private static JSONObject config;
    private QueryFactory queryFactory;


    public static SearchEngine getInstance(JSONObject config) throws SQLException, ClassNotFoundException {
        if (instance == null) {
            SearchEngine.config = config;
            instance = new SearchEngine();
        }
        return instance;
    }

    private SearchEngine() throws SQLException, ClassNotFoundException {
        JSONObject databaseConfig = config.getJSONObject("database");
        Database.initialize(
                databaseConfig.getString("url"),
                databaseConfig.getString("user"),
                databaseConfig.getString("passwd")
        );
        queryFactory = new QueryFactory();
    }

    public AbstractModel createModel(ModelType type) throws SQLException {
        switch (type) {
            case BOOLEAN_MODEL:
            default:
                return new BooleanModel(queryFactory);
            case VECTOR_MODEL:
                return new VectorModel(queryFactory);
        }
    }

    public SearchResult search(String query, AbstractModel model) throws SQLException {
        double start = System.currentTimeMillis();
        return new SearchResult(model.getRankingResult(query), System.currentTimeMillis() - start);
    }

    public static JSONObject getConfig() {
        return config;
    }



    public static void main(String[] args) throws Exception {
        JSONObject config = new JSONObject(FileUtils.readFileToString(new File(args[0])));
        config.put("root", args[1]);
        SearchEngine searchEngine = SearchEngine.getInstance(config);

        if (SearchEngine.config.getBoolean("index")) {
            Indexer indexer = new Indexer();
            indexer.run(SearchEngine.config.getString("doc_folder"));
        } else {
            Scanner keyboard = new Scanner(System.in);
            while (true) {
                System.out.print("Input query: ");
                String query = keyboard.nextLine();
                System.out.print("[1] Boolean model  [2] Vector model : ");
                AbstractModel model = keyboard.nextLine().equals("boolean") ?
                        searchEngine.createModel(ModelType.BOOLEAN_MODEL) :
                        searchEngine.createModel(ModelType.VECTOR_MODEL);
                if (query.length() == 0)
                    break;
                SearchResult result = searchEngine.search(query, model);
                result.getDocuments().forEach(System.out::println);
            }
        }
    }

}
