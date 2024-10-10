package graphtypes;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A graph that encodes word ladders.
 *
 * The class does not store the full graph in memory, just a dictionary of words.
 * The edges are then computed on demand.
 */
public class WordLadder implements Graph<String> {
    private Set<String> dictionary;
    private Set<Character> alphabet;

    public WordLadder() {
        dictionary = new HashSet<>();
        alphabet = new HashSet<>();
    }

    public WordLadder(String graph) throws IOException {
        this();
        init(graph);
    }

    /**
     * Populates the word ladder graph from the given dictionary file.
     * The file should contain one word per line, except lines starting with "#".
     */
    public void init(String graph) throws IOException {
        Files.lines(Paths.get(graph))
            .filter(line -> !line.startsWith("#"))
            .map(String::trim)
            .forEach(this::addWord);
    }

    /**
     * Adds a word to the dictionary if it only contains letters.
     * The word is converted to lower case.
     */
    public void addWord(String word) {
        if (word.matches("\\p{L}+")) {
            word = word.toLowerCase();
            dictionary.add(word);
            for (char c : word.toCharArray())
                alphabet.add(c);
        }
    }

    @Override
    public Set<String> nodes() {
        return Collections.unmodifiableSet(dictionary);
    }

    /**
     * Returns a list of the graph edges that originate from `word`.
     */
    @Override
    public List<Edge<String>> outgoingEdges(String w) {
        // TODO: Replace these lines with your solution!
        List<Edge<String>> outEdges= new ArrayList<>();
        for (int i=0;i<w.length();i++){
            char[] box= w.toCharArray();
            for(Character character:alphabet){
                box[i]= character;
                String temp = new String(box);
                if (dictionary.contains(temp)&&!temp.equals(w)){
                    outEdges.add(new Edge<>(w,temp));

                }

            }

        }


        return outEdges ;
    }

    /**
     * Returns the guessed best cost for getting from a word to another.
     * (the number of differing character positions)
     */
    @Override

    public double guessCost(String w, String u) {
        // TODO: Replace these lines with your solution!
        // Don't forget to handle the case where the lengths differ.
        double cost = 0;
        if (w.length()==u.length()){
            for (int i=0;i<w.length();i++){
                if (w.charAt(i)!=u.charAt(i)){
                    cost+=1;
                }
            }
        }
        return cost;
    }

    public boolean isWeighted() {
        return false;
    }

    @Override
    public String parseNode(String s) {
        String word = s.toLowerCase();
        if (!dictionary.contains(word))
            throw new IllegalArgumentException("Unknown word: " + word);
        return s;
    }

    public String toString() {
        StringWriter buffer = new StringWriter();
        PrintWriter w = new PrintWriter(buffer);
        w.println("Word ladder graph with " + numNodes() + " words.");
        String alphabetStr = alphabet.stream()
            .map(Object::toString)
            .collect(Collectors.joining());
        w.println("Alphabet: " + alphabetStr);
        return buffer.toString();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1)
            throw new IllegalArgumentException("missing argument: path to words file");
        WordLadder graph = new WordLadder(args[0]);
        Graph.printGraphWithExamples(graph);
    }
}

