package wir.hw1;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class Util {

    public static String readFile(String fileName, boolean newLine) {
        StringBuilder data = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"))) {
            while (reader.ready())
                data.append(reader.readLine()).append(newLine ? "\n" : "");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data.toString();
    }

    public static boolean isEnglishAlphabet(int c) {
        return (c>='A' && c<='Z') || (c>='a' && c<='z');
    }

}
