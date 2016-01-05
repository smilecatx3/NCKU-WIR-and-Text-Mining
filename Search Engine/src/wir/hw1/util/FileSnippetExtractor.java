package wir.hw1.util;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

import wir.hw1.data.Query;

public class FileSnippetExtractor {
    private String data;
    private String lowerCaseData;
    private Query query;


    public FileSnippetExtractor(File file, Query query) {
        try {
            data = FileUtils.readFileToString(file);
            lowerCaseData = data.toLowerCase();
            this.query = query;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getSnippet() {
        // (1) try to find full match
        int index = lowerCaseData.indexOf(query.getString());
        if (index >= 0) {
            return getSubstring(index);
        } else {
            // (2) Try to find splitted tokens of the query
            for (String word : query.getTokens().keySet()) {
                index = lowerCaseData.indexOf(word);
                if (index >= 0)
                    return getSubstring(index);
            }
            // (3) Try to find synonyms of the query
            for (String word : query.getSynonyms().keySet()) {
                index = lowerCaseData.indexOf(word);
                if (index >= 0)
                    return getSubstring(index);
            }
        }
        return null;
    }

    public int getFullMatchCount() {
        return StringUtils.countMatches(lowerCaseData, query.getString());
    }

    public int getPartialMatchCount() {
        int count = 0;
        for (String word : query.getTokens().keySet())
            count += StringUtils.countMatches(lowerCaseData, word);
        return count;
    }

    private String getSubstring(int index) {
        return data.substring(Math.max(0, index-50), Math.min(index+50, data.length()));
    }

}
