package wir.hw1.util;

import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.WordNetException;
import wir.hw1.SearchEngine;
import wir.hw1.data.Query;
import wir.hw1.database.Database;
import wir.hw1.database.WordTable;
import wir.hw1.index.Tokenizer;

public class QueryFactory {
    private Tokenizer tokenizer;
    private WordTable db_wordTable = Database.getTable("word");
    private WordNetDatabase database;


    public QueryFactory() {
        JSONObject config = SearchEngine.getConfig();
        String root = config.getString("root");
        File mmsegDict = new File(root, config.getJSONObject("dict_path").getString("mmseg"));
        File wordnetDict = new File(root, config.getJSONObject("dict_path").getString("wordnet"));
        this.tokenizer = new Tokenizer(mmsegDict);
        System.setProperty("wordnet.database.dir", wordnetDict.getAbsolutePath());
        this.database = WordNetDatabase.getFileInstance();
    }

    public Query create(String query) {
        Map<String, Integer> tokens = new HashMap<>(); // <Word, Word_ID>
        Map<String, Integer> synonyms = new HashMap<>();
        StringBuilder log = new StringBuilder();

        log.append("Tokens: ");
        for (String token : tokenizer.tokenize(query)) {
            log.append(token).append(", ");
            putWordToTable(token, tokens);

            if (token.matches("\\p{Alpha}+"))
                findSynonym(token, synonyms);
        }
        log.append("\n");

        if (synonyms.size() > 0) {
            log.append("Found synonyms: ");
            for (String synonym : synonyms.keySet())
                log.append(synonym).append(", ");
            log.append("\n");
        }

        System.out.println(log);
        return new Query(query, tokens, synonyms);
    }

    /**
     * Tries to find a synonym of the word in the database.
     */
    private void findSynonym(String word, Map<String, Integer> synonyms) {
        Synset[] synsets = database.getSynsets(word);

        Map<Integer, String[]> wordFormTable = new TreeMap<>(Collections.reverseOrder()); // <tag count, synonyms> (Sort by the synset's tag count)
        for (Synset synset : synsets) {
            try {
                wordFormTable.put(synset.getTagCount(word), synset.getWordForms());
            } catch (WordNetException e) {
                if (!e.getMessage().matches(".*Attempted to get the tag count for '.*' from a synset that does not contain it.*"))
                    e.printStackTrace();
            }
        }

        for (String[] wordForms : wordFormTable.values())
            for (String wordForm : wordForms)
                if (!wordForm.equals(word))
                    if (putWordToTable(wordForm, synonyms))
                        return;
    }

    /**
     * Puts the word into the table [word, word_id]
     * @return true if database contains the word (word_id != -1), false otherwise.
     */
    private boolean putWordToTable(String word, Map<String, Integer> table) {
        int wordID = db_wordTable.getID(word);
        if (wordID != -1) {
            table.put(word, wordID);
            return true;
        } else {
            return false;
        }
    }

}
