package wir.hw1.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
import wir.hw1.data.Query;
import wir.hw1.database.Database;
import wir.hw1.database.WordTable;
import wir.hw1.index.Tokenizer;

public class QueryFactory {
    private Tokenizer tokenizer;
    private WordTable db_wordTable = Database.getTable("word");
    private WordNetDatabase database;

    public QueryFactory(Tokenizer tokenizer, String dicPath) {
        this.tokenizer = tokenizer;
        System.setProperty("wordnet.database.dir", dicPath);
        this.database = WordNetDatabase.getFileInstance();
    }

    public Query create(String query) {
        Map<String, Integer> tokens = new HashMap<>(); // <Word, Word_ID>
        Map<String, Integer> synonyms = new HashMap<>();

        for (String token : tokenizer.tokenize(query)) {
            putWordToTable(token, tokens);

            if (token.matches("\\p{Alpha}+"))
                findSynonym(token, synonyms);
        }

        return new Query(query, tokens, synonyms);
    }

    /**
     * Tries to find a synonym of the word in the database.
     */
    private void findSynonym(String word, Map<String, Integer> synonyms) {
        Synset[] synsets = database.getSynsets(word);

        Map<Integer, String[]> wordFormTable = new TreeMap<>(Collections.reverseOrder()); // <tag count, synonyms> (Sort by the synset's tag count)
        for (Synset synset : synsets)
            wordFormTable.put(synset.getTagCount(word), synset.getWordForms());

        for (String[] wordForms : wordFormTable.values())
            for (String wordForm : wordForms)
                if (!wordForm.equals(word))
                    if (putWordToTable(wordForm, synonyms))
                        break;
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
