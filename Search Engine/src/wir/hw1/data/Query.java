package wir.hw1.data;

import java.util.Map;

public class Query {
    private String string;
    private Map<String, Integer> tokens; // <Token, Word_ID>
    private Map<String, Integer> synonyms; // <Token, Word_ID>

    public Query(String string, Map<String, Integer> tokens, Map<String, Integer> synonyms) {
        this.string = string;
        this.tokens = tokens;
        this.synonyms = synonyms;
    }

    public String getString() {
        return string;
    }

    public Map<String, Integer> getTokens() {
        return tokens;
    }

    public Map<String, Integer> getSynonyms() {
        return synonyms;
    }

    /** @return true if the database contains any word of tokens or synonyms */
    public boolean isValid() {
        return tokens.size() + synonyms.size() != 0;
    }
}
