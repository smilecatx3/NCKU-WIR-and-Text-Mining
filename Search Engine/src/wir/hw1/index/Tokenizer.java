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
                String string = word.getString();
                if (string.length() > 50) // Ignore the word which length is too long
                    continue;
                String lastWord = (words.size() > 0) ? words.get(words.size()-1) : "";
                if (Character.isLetterOrDigit(string.charAt(0))) {
                    if (lastWord.matches("(\\d+/){1,2}")) // Date (Assume there is '/' in units.dic)
                        words.set(words.size()-1, lastWord+string);
                    else if (!string.equals("/"))
                        words.add(string.toLowerCase());
                } else if (string.equals("/")) {
                    if (lastWord.matches("(\\d+/*){1,2}")) // Date (Assume there is '/' in units.dic)
                        words.set(words.size()-1, lastWord+string);
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
}

// TODO Known bug: negative number will be trimed / 2015-06-08