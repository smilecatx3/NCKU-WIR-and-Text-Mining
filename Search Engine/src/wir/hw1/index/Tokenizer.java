package wir.hw1.index;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Tokenizer {
    private Seg seg;

    public Tokenizer(File dicFolder) {
        System.setProperty("mmseg.dic.path", dicFolder.getAbsolutePath());
        Logger.getLogger(Dictionary.class.getName()).setUseParentHandlers(false); // Disable log message
        seg = new ComplexSeg(Dictionary.getInstance());
    }

    /**
     * Splits the sentence into several tokens.
     * @param sentence The sentence to be tokenized
     * @return A list contains the tokens of the sentence
     */
    public List<String> tokenize(String sentence) {
        List<String> words = new ArrayList<>();
        try (Reader input = new StringReader(sentence)) {
            MMSeg mmSeg = new MMSeg(input, seg);
            Word word;
            while ((word = mmSeg.next()) != null) {
                String string = word.getString().trim();
                if (string.length() > 50) // Ignore the word which length is too long
                    continue;
                String lastWord = (words.size() > 0) ? words.get(words.size()-1) : "";
                if (isValidWord(string) || string.equals("/")) {
                    if (isDateFormat(lastWord, string)) // Date (Assume there is '/' in units.dic)
                        words.set(words.size()-1, lastWord+string);
                    else if (!string.equals("/"))
                        words.add(string.toLowerCase());
                } else if (string.equals("%")) { // Percentage (Assume there is '%' in units.dic)
                    if (lastWord.matches("\\d+"))
                        words.set(words.size()-1, lastWord+string);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    private boolean isValidWord(String str) {
        return str.matches("[" +
                "\\p{Alnum}" +
                "\\p{InCJK_UNIFIED_IDEOGRAPHS}" +
                "\\p{InBOPOMOFO}" +
                "\\p{InCJK_SYMBOLS_AND_PUNCTUATION}" +
                "\\p{InHIRAGANA}" + "\\p{InKATAKANA}" +
                "\\p{InHANGUL_SYLLABLES}" +
                "]+");
    }

    private boolean isDateFormat(String lastWord, String word) {
        int numSlash = StringUtils.countMatches(lastWord, "/");
        switch (numSlash) {
            case 0: // eg: "2016"
                return !lastWord.startsWith("0") && lastWord.matches("\\d{2,4}") && word.equals("/"); // A.D. or Chinese year representation (eg: 2016 or 105)
            case 1: // eg: "2016/" or "2016/01"
                return (word.equals("/")) ? !lastWord.endsWith("/") :
                        lastWord.endsWith("/") && word.matches("0[1-9]|[1-9]|1[1-2]"); // 01~12 or 1~12 month
            case 2: // eg: "2016/01/" or "2016/01/25"
                return lastWord.endsWith("/") && word.matches("0[1-9]|[1-9]|[1-2][0-9]|3[0-1]"); // 01~31 or 1~31 day (No further check, i.e. 2/30 is valid)
            default:
                return false;
        }
    }
}
