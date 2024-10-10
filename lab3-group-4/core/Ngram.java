package core;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * An n-gram is conceptually a sequence of tokens (strings) of length n.
 */
public class Ngram implements Comparable<Ngram> {
    private final String[] tokens;

    private Ngram(String[] tokens) {
        // Create a new array because the caller could modify the initial array later.
        this.tokens = tokens.clone();
    }

    public int size() {
        return tokens.length;
    }

    public int compareTo(Ngram other) {
        return Arrays.compare(this.tokens, other.tokens);
    }

    public boolean equals(Object other) {
        return other != null && other instanceof Ngram &&
            Arrays.equals(this.tokens, ((Ngram) other).tokens);
    }

    public int hashCode() {
        return Arrays.hashCode(this.tokens);
    }

    public String toString() {
        return Arrays.stream(this.tokens).collect(Collectors.joining("/"));
    }

    /**
     * Return all n-grams of a given list of tokens.
     */
    public static Ngram[] ngrams(String[] input, int n) {
        int count = Math.max(0, input.length - n + 1);
        Ngram[] ngrams = new Ngram[count];
        for (int i = 0; i < count; i++) {
            ngrams[i] = new Ngram(Arrays.copyOfRange(input, i, i + n));
        }
        return ngrams;
    }
}
