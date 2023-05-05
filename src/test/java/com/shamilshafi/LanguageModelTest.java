package com.shamilshafi;

import junit.framework.TestCase;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class LanguageModelTest extends TestCase {

    public void testSplit() {
        try {
            LanguageModel languageModel = new LanguageModel("test_wordninja_words.txt.gz");
            List<String> result = languageModel.split("seriouslybruh");
            assertEquals(result.size(), 2);
            assertEquals(result, Arrays.asList(new String[]{"seriously", "bruh"}));

            result = languageModel.split("seriouslybruh   oops");
            assertEquals(result.size(), 4);
            assertEquals(result, Arrays.asList(new String[]{"seriously", "bruh", "   ", "oops"}));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}