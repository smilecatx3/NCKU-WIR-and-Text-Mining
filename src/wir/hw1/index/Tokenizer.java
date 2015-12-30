package wir.hw1.index;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Tokenizer {
    private Seg seg;

    public Tokenizer(String dicPath) {
        System.setProperty("mmseg.dic.path", dicPath);
        Logger.getLogger(Dictionary.class.getName()).setUseParentHandlers(false); // Disable log message
        seg = new ComplexSeg(Dictionary.getInstance());
    }

    /**
     * Tokenize a given sentence into a words list
     * @param sentence The sentence to be tokenized
     * @return A list contains the tokens of the sentence
     */
    public List<String> tokenize(String sentence) {
        List<String> words = new ArrayList<>();
        try (Reader input = new StringReader(sentence)) {
            MMSeg mmSeg = new MMSeg(input, seg);
            Word word;
            while ((word = mmSeg.next()) != null) {
                String string = word.getString();
                if (Character.isLetterOrDigit(string.charAt(0))) {
                    words.add(string.toLowerCase());
                } else if (string.matches("/|%")) { // Date or percentage (Assume there are '%' and '/' in units.dic)
                    String lastWord = words.get(words.size()-1);
                    if (lastWord.matches("\\d+"))
                        words.set(words.size()-1, lastWord+string);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }
}

// Known bug: negative number will be trimed / 2015-06-08