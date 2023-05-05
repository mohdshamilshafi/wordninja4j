package com.shamilshafi;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Hello world!
 *
 */
public class App {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        try {
            LanguageModel languageModel = new LanguageModel("wordninja_words.txt");
            System.out.println(languageModel.split("jonathanwilliamshadalittlelamb"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}