package wir.hw1.model;

import org.apache.commons.lang3.StringUtils;
import wir.hw1.SearchEngine;
import wir.hw1.Util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class VectorModel extends AbstractModel {
    public VectorModel() throws SQLException { }

    @Override
    protected List<Document> computeScore(String query, List<Integer> wordIDs) throws Exception {
        StringBuilder[] sqls = getSQL_selectTF(wordIDs);
        Future<List<Document>> thread1 = Executors.newSingleThreadExecutor().submit(() -> computeScore(query, wordIDs, sqls[0].toString()));
        Future<List<Document>> thread2 = Executors.newSingleThreadExecutor().submit(() -> computeScore(query, wordIDs, sqls[1].toString()));
        List<Document> rankingList = new ArrayList<>();
        rankingList.addAll(thread1.get());
        rankingList.addAll(thread2.get());
        Collections.sort(rankingList);
        return rankingList;
    }

    private StringBuilder[] getSQL_selectTF(List<Integer> wordIDs) {
        // Construct query's word_id SQL statement
        StringBuilder sql_wordID = new StringBuilder("(");
        for (Integer wordID : wordIDs)
            sql_wordID.append("`word_id`=").append(wordID).append(" OR ");
        sql_wordID.delete(sql_wordID.length()-4, sql_wordID.length()).append(")");
        // Construct SQL statements for selecting TF values
        StringBuilder[] sqls = new StringBuilder[2];
        int numDoc = documentTable.size();
        sqls[0] = new StringBuilder(String.format(
                "SELECT * FROM `term_frequency` WHERE `doc_id`<%d AND %s ORDER BY `doc_id`;",
                (int)(0.5*numDoc), sql_wordID));
        sqls[1] = new StringBuilder(String.format(
                "SELECT * FROM `term_frequency` WHERE `doc_id`>=%d AND %s ORDER BY `doc_id`;",
                (int)(0.5*numDoc), sql_wordID));
        return sqls;
    }

    private List<Document> computeScore(String query, List<Integer> wordIDs, String sql) throws SQLException {
        List<Document> rankingList = new ArrayList<>();
        int numWordID = wordIDs.size();
        // Compute score of each document
        try (ResultSet result = conn.createStatement().executeQuery(sql)) {
            result.setFetchSize(Integer.MAX_VALUE);
            int prevDocID = -1;
            int count = 0;
            double score = 0;

            while (result.next()) {
                int docID = result.getInt(2);
                double tf = result.getDouble(3);
                double idf = idfTable.get(result.getInt(1));
                if ((docID != prevDocID) && (prevDocID != -1) && !result.isLast()) { // next document, add previous document to list
                    addToList(rankingList, prevDocID, score, count == numWordID, query);
                    score = count = 0;
                }
                score += tf*idf;
                count++;
                prevDocID = docID;
                if (result.isLast()) // last document
                    addToList(rankingList, docID, score, count == numWordID, query);
            }
        }
        return rankingList;
    }

    private void addToList(List<Document> rankingList, int docID, double score, boolean countMatches, String query) {
        String docName = documentTable.getName(docID);
        String snippet = null;
        if (countMatches) {
            String fileContent = Util.readFile(SearchEngine.DOC_DIR + docName, false);
            int index = fileContent.indexOf(query);
            if (index >= 0) {
                snippet = fileContent.substring(Math.max(0, index-50), Math.min(index+50, fileContent.length()));
                score += 1 + (StringUtils.countMatches(fileContent, query)*0.01);
            }
        }
        if (score > 0)
            rankingList.add(new Document(docName, score, snippet));
    }
}
