package wir.hw1;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

import wir.hw1.data.SearchResult;
import wir.hw1.database.Database;
import wir.hw1.index.Indexer;
import wir.hw1.index.Tokenizer;
import wir.hw1.model.AbstractModel;
import wir.hw1.model.BooleanModel;
import wir.hw1.model.VectorModel;
import wir.hw1.util.QueryFactory;

public class SearchEngine {
    public enum ModelType { BOOLEAN_MODEL, VECTOR_MODEL }
    private static SearchEngine instance;
    private JSONObject config;
    private Tokenizer tokenizer;
    private QueryFactory queryFactory;


    public static SearchEngine getInstance(String configPath) throws IOException, SQLException {
        if (instance == null)
            instance = new SearchEngine(configPath);
        return instance;
    }

    private SearchEngine(String configPath) throws IOException, SQLException {
        config = new JSONObject(FileUtils.readFileToString(new File(configPath)));

        JSONObject databaseConfig = config.getJSONObject("database");
        Database.initialize(
                databaseConfig.getString("url"),
                databaseConfig.getString("user"),
                databaseConfig.getString("passwd")
        );

        tokenizer = new Tokenizer(config.getJSONObject("dict_path").getString("mmseg"));
        queryFactory = new QueryFactory(tokenizer, config.getJSONObject("dict_path").getString("wordnet"));
    }

    public AbstractModel createModel(ModelType type) throws SQLException {
        switch (type) {
            case BOOLEAN_MODEL:
                return new BooleanModel(queryFactory, config);
            case VECTOR_MODEL:
                return new VectorModel(queryFactory, config);
            default:
                throw new IllegalArgumentException("No such type");
        }
    }

    public SearchResult search(String query, AbstractModel model) throws SQLException {
        double start = System.currentTimeMillis();
        return new SearchResult(model.getRankingResult(query), System.currentTimeMillis() - start);
    }



    public static void main(String[] args) throws Exception {
        SearchEngine searchEngine = SearchEngine.getInstance("data/hw1/config.json");

        if (searchEngine.config.getBoolean("index")) {
            Indexer indexer = new Indexer(searchEngine.tokenizer);
            indexer.run(searchEngine.config.getString("doc_folder"));
        }

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
