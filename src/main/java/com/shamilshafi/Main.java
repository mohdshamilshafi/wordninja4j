package com.shamilshafi;

import java.io.IOException;

/**
 * Hello world!
 *
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        try {
            LanguageModel languageModel = new LanguageModel("wordninja_words.txt.gz");
            System.out.println(languageModel.split("jonathanwilliamshadalittlelamb"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}