package com.shamilshafi;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class LanguageModel {
    private HashMap<String, Double> wordCost;
    private int maxWord;

    public LanguageModel(String wordFile) throws IOException {
        // Build a cost dictionary, assuming Zipf's law and cost = -Math.log(probability).
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                new GZIPInputStream(this.getClass().getResourceAsStream("/"+wordFile))));
        Pattern regex = Pattern.compile("\\s+");
        List<String> words = reader.lines().collect(Collectors.toList());
        reader.close();

        wordCost = new HashMap<>();
        for (int i = 0; i < words.size(); i++) {
            double probability = Math.log((i + 1) * Math.log(words.size()));
            wordCost.put(words.get(i), probability);
        }

        maxWord = 0;
        for (String word : words) {
            if (word.length() > maxWord) {
                maxWord = word.length();
            }
        }
    }

    private LanguageModel.Pair<Double, Integer> bestMatch(int i, String s, List<Double> cost) {
        List<Double> candidates = cost.subList(Math.max(0, i - maxWord), i);

        Stream<LanguageModel.Pair<Double, Integer>> candidatesStream = IntStream.range(0, candidates.size())
                .mapToObj(k -> new LanguageModel.Pair<>(candidates.get(candidates.size()-k-1) + wordCost.getOrDefault(s.substring(i - k - 1, i)
                        .toLowerCase(), Double.MAX_VALUE), k+1));

        return candidatesStream.min(Comparator.comparingDouble(LanguageModel.Pair::getKey)).orElse(new LanguageModel.Pair<>(Double.MAX_VALUE, 0));
    }

    public List<String> split(String s) {
        final Pattern splitRegex = Pattern.compile("\\s+");
        /* Uses dynamic programming to infer the location of spaces in a string without spaces. */
        Matcher matcher = splitRegex.matcher(s);
        List<String> punctuations = new ArrayList<>();
        List<String> texts = Arrays.asList(splitRegex.split(s));
        while (matcher.find()) {
            punctuations.add(matcher.group());
        }
        assert punctuations.size()+1 == texts.size();

        List<List<String>> newTexts = new ArrayList<>();
        for (String text : texts) {
            newTexts.add(doSplit(text));
        }

        ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < newTexts.size(); i++) {
            if (i > 0) {
                result.add(punctuations.get(i - 1));
            }
            result.addAll(newTexts.get(i));
        }

        return result;
    }

    private List<String> doSplit(String s) {
        List<String> out = new ArrayList<>();

        // Find the best match for the i first characters, assuming cost has
        // been built for the i-1 first characters.
        // Returns a pair (match_cost, match_length).

        // Build the cost array.
        List<Double> cost = new ArrayList<>();
        cost.add(0.0);
        for (int i = 1; i <= s.length(); i++) {
            LanguageModel.Pair<Double, Integer> pair = bestMatch(i, s, cost);
            cost.add(pair.getKey());
        }

        // Backtrack to recover the minimal-cost string.
        int i = s.length();
        while (i > 0) {
            LanguageModel.Pair<Double, Integer> pair = bestMatch(i, s, cost);
            double c = pair.getKey();
            int k = pair.getValue();
            assert c == cost.get(i);
            boolean newToken = true;
            String substring = s.substring(i - k, i);
            if (!"'".equals(substring)) { // ignore a lone apostrophe
                if (!out.isEmpty()) {
                    // re-attach split 's and split digits
                    if (out.get(out.size() - 1).equals("'s") || (Character.isDigit(s.charAt(i - 1)) && Character.isDigit(out.get(out.size() - 1).charAt(0)))) { // digit followed by digit
                        out.set(out.size() - 1, substring + out.get(out.size() - 1)); // combine current token with previous token
                        newToken = false;
                    }
                }
            }

            if (newToken) {
                out.add(substring);
            }

            i -= k;
        }
        Collections.reverse(out);
        return out;
    }

    private class Pair<K, V> {
        private K key;
        private V value;

        public Pair(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        public V getValue() {
            return value;
        }

        public void setValue(V value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            LanguageModel.Pair<?, ?> pair = (LanguageModel.Pair<?, ?>) o;

            if (!Objects.equals(key, pair.key)) {
                return false;
            }

            return Objects.equals(value, pair.value);
        }

        @Override
        public int hashCode() {
            int result = key != null ? key.hashCode() : 0;
            result = 31 * result + (value != null ? value.hashCode() : 0);
            return result;
        }
    }

}
