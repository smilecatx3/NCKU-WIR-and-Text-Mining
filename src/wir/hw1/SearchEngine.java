package wir.hw1;

import wir.hw1.database.Database;
import wir.hw1.model.AbstractModel;
import wir.hw1.model.BooleanModel;
import wir.hw1.model.Document;
import wir.hw1.model.VectorModel;

import java.util.List;


public class SearchEngine {
    public static final String DOC_DIR = "C:/IR/hw1/data/";
    public enum ModelType { BOOLEAN_MODEL, VECTOR_MODEL }
    private static SearchEngine instance;
    private BooleanModel booleanModel;
    private VectorModel vectorModel;

    private SearchEngine() throws Exception {
        Database.initialize();
        booleanModel = new BooleanModel();
        vectorModel = new VectorModel();
    }

    public static SearchEngine getInstance() throws Exception {
        if (instance == null)
            instance = new SearchEngine();
        return instance;
    }

    public List<Document> search(String query, ModelType type) throws Exception {
        AbstractModel model = (type == ModelType.BOOLEAN_MODEL) ? booleanModel : vectorModel;
        return model.getRankingResult(query);
    }

    public static void main(String[] args) throws Exception {
//        Database.initialize();
//
//        if (args[0].substring(8).equals("true"))
//            new Indexer().index(DOC_DIR);
//
//        AbstractModel model = (args[1].substring(8).equals("model")) ? new BooleanModel() : new VectorModel();
//
//        Scanner keyboard = new Scanner(System.in);
//        while (true) {
//            System.out.print("Input query: ");
//            String query = keyboard.nextLine();
//            if (query.length() == 0)
//                break;
//            String[] result = model.getRankingResult(query);
////            for (String doc : result);
////                System.out.println(doc);
//        }
//
//        System.exit(0);
    }

}
