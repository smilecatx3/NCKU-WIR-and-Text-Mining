package wir.hw1.index;

import wir.hw1.Util;
import wir.hw1.database.Database;
import wir.hw1.database.DocumentTable;
import wir.hw1.database.TermFrequencyTable;
import wir.hw1.database.WordTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class Indexer {
    private DocumentTable documentTable = Database.getDocumentTable();
    private WordTable wordTable = Database.getWordTable();
    private TermFrequencyTable tfTable = Database.getTermFrequencyTable();

    private class ParsedData {
        int numTerms;
        Map<String, Integer> termTable;
        ParsedData(int numTerms, Map<String, Integer> termTable) {
            this.numTerms = numTerms;
            this.termTable = termTable;
        }
    }

    public void index(String dirPath) throws Exception {
        File[] files = new File(dirPath).listFiles();
        long startTime = Calendar.getInstance().getTimeInMillis();
        Future runIndex1 = Executors.newSingleThreadExecutor().submit(() -> {index(files, 0); return null;});
        Future runIndex2 = Executors.newSingleThreadExecutor().submit(() -> {index(files, 1); return null;});
        runIndex1.get();
        runIndex2.get();
        Database.updateDatabase();
        System.out.println(String.format("Index process completed (Elapsed %f seconds)", (Calendar.getInstance().getTimeInMillis()-startTime)/1000.0));
    }

    private void index(File[] files, int startIndex) throws SQLException, IOException {
        // Index each file in the directory
        for (int i=startIndex; i<files.length; i+=2) {
            File file = files[i];
            System.out.println(String.format("(%d/%d) Indexing file '%s' ... ", i+1, files.length, file.getName()));
            // Insert document ID to database
            int docID = documentTable.insert(file.getName());
            if (docID == -1) {
                System.out.println("The file already exists.");
                continue;
            }
            // Parse file
            ParsedData parsedData = parseFile(file);
            int numTerms = parsedData.numTerms;
            Map<String, Integer> termTable = parsedData.termTable;
            // Insert data to database
            for (Map.Entry<String, Integer> entry : termTable.entrySet()) {
                int wordID = wordTable.insert(entry.getKey());
                double tf = (double)entry.getValue() / numTerms;
                tfTable.insert(wordID, docID, tf);
            }
        }
    }

    private ParsedData parseFile(File file) throws IOException {
        int numTerms = 0;
        Map<String, Integer> termTable = new HashMap<>();
        StringBuilder term = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            char ch;
            while (reader.ready()) {
                ch = (char)reader.read();
                if (Util.isEnglishAlphabet(ch)) { // English word
                    term.append(ch);
                } else if (Character.isDigit(ch)) { // Number
                    term.append(ch);
                } else if ((ch == '/') && (term.length() > 0) && Character.isDigit(term.charAt(term.length()-1))) { // Date
                    term.append(ch);
                } else if (Character.isLetter(ch) || (term.length() > 0)) { // Chinese word
                    if (term.length() > 0) {
                        addWordToTable(termTable, term.toString().toLowerCase());
                        term = new StringBuilder();
                    } else {
                        addWordToTable(termTable, String.valueOf(ch));
                    }
                    numTerms++;
                }
            }
        }

        return new ParsedData(numTerms, termTable);
    }

    private void addWordToTable(Map<String, Integer> termTable, String term) {
        Integer count = 0;
        if (termTable.containsKey(term))
            count = termTable.get(term);
        termTable.put(term, count + 1);
    }
}
