package core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.StreamSupport;

import datatypes.Map;
import datatypes.Set;
import utilities.ProgressBar;

public class SimilarityCalculator {
    public Map<Path, Set<Ngram>> fileNgrams;
    public Map<Ngram, Set<Path>> ngramIndex;
    public Map<PathPair, Set<Ngram>> intersections;

    /**
     * Phase 1: Read in each file and chop it into n-grams.
     * Stores the result in the instance variable `fileNgrams`.
     */
    public void readNgramsFromFiles(Path[] paths, int N) throws IOException {
        for (Path path : paths) {
            String[] contents = Files.readString(path, StandardCharsets.UTF_8)
                // Split the input into lower-case words.
                // A "word" is here a sequence of letters,
                // which is completely wrong but an ok approximation.
                .toLowerCase().split("\\W+");
            Set<Ngram> ngrams = fileNgrams.get(path);
            for (Ngram ngram : Ngram.ngrams(contents, N)) {
                ngrams.add(ngram);
            }
        }
    }

    /**
     * Phase 2: Build index of n-grams.
     * The naive version doesn't do anything,
     * you should implement it in `FasterSimilarityCalculation.java`.
     */
    //
    public void buildNgramIndex() {}

    /**
     * Phase 3: Count which n-grams each pair of files has in common.
     * This is the naive, slow version,
     * to be improved in `FasterSimilarityCalculation.java`.
     * Uses the instance varable `fileNgrams`.
     * Stores the result in the instance variable `intersections`.
     */
    public void computeIntersections() {
        for (Path path1 : new ProgressBar<>(this.fileNgrams, this.fileNgrams.size(), "Computing intersections")) {
            for (Path path2 : this.fileNgrams) {
                // Since intersection is a commutative operation (i.e., A ∩ B == B ∩ A),
                // it's enough to only compute the intersection for path pairs (p,q) where p < q:
                if (path1.compareTo(path2) < 0) {
                    for (Ngram ngram1 : this.fileNgrams.get(path1)) {
                        for (Ngram ngram2 : this.fileNgrams.get(path2)) {
                            if (ngram1.equals(ngram2)) {
                                PathPair pair = new PathPair(path1, path2);
                                Set<Ngram> intersection = this.intersections.get(pair);
                                intersection.add(ngram1);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Phase 4: find all pairs, sorted in descending order of similarity.
     * Returns an array of the M path pairs that are most similar.
     * Uses the instance variables `fileNgrams` and `intersections`.
     */
    public PathPair[] findMostSimilar(int M, String measure) {
        // Uses the Java 8 streams API - very handy Java feature which we don't cover in the course.
        // If you want to learn about it, see e.g.:
        // * https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html#package.description
        // * https://stackify.com/streams-guide-java-8/

        // Convert `intersections` into a stream.
        // This is a bit more complicated than it should be because Map doesn't implement the streaming API.
        // If our maps came from the Java standard library, we could just write `intersections.keys().stream()`.
        return StreamSupport.stream(this.intersections.spliterator(), false)
            // Sort to have the most similar pairs first.
            .sorted(Comparator.comparing((PathPair p) -> this.similarity(p, measure)).reversed())
            // Limit to the M first results.
            .limit(M)
            // Convert the result to an array.
            .toArray(PathPair[]::new);
    }

    public static String[] similarityMeasures = new String[] {
        "absolute", "jaccard", "cosine", "average",
        "weighted-jaccard", "weighted-cosine", "weighted-average"
    };

    /**
     * Calculates a similarity score between the ngrams in A and B. The following are implemented:
     *
     * Absolute number of common ngrams:
     *      absolute(A, B)  =  |A ∩ B|
     *
     * Jaccard index: https://en.wikipedia.org/wiki/Jaccard_index
     * This is the proportion of ngrams from A or B that are in both.
     *      jaccard(A, B)  =  |A ∩ B| / |A ∪ B|
     *                     =  |A ∩ B| / (|A| + |B| - |A ∩ B|)
     *
     * Cosine similarity, or Otsuka–Ochiai coefficient.
     * This is the same as the geometric mean of p(B|A) and p(A|B)
     * https://en.wikipedia.org/wiki/Cosine_similarity#Otsuka–Ochiai_coefficient
     *      cosine(A, B)  =  |A ∩ B| / √(|A|·|B|)
     *                    =  p(A,B) / √(p(A)·p(B))
     *                    =  √( |A∩B|/|A| · |A∩B|/|B| )
     *                    =  √( p(B|A) · p(A|B) )
     *
     * Arithmetic mean of p(B|A) and p(A|B):
     *      average(A, B)  =  ( |A∩B|/|A| + |A∩B|/|B| ) / 2
     *                     =  ( p(B|A) + p(A|B) ) / 2
     *
     * Weighted variants of all the above (except absolute):
     *      weighted-X(A, B)  =  |A∩B| · X(A, B)
     */
    public float similarity(PathPair pair, String measure) {
        if (!Arrays.asList(SimilarityCalculator.similarityMeasures).contains(measure))
            throw new IllegalArgumentException("Unknown similarity measure: " + measure);

        float nAB = this.intersections.get(pair).size();

        float score = nAB;
        if (measure.equals("absolute"))
            return score;

        float nA = this.fileNgrams.get(pair.path1).size();
        float nB = this.fileNgrams.get(pair.path2).size();

        if (measure.contains("jaccard")) {
            float nAorB = nA + nB - nAB;
            score = nAB / nAorB;
        } else if (measure.contains("cosine")) {
            score = (float) Math.sqrt((nAB / nA) * (nAB / nB));
        } else if (measure.contains("average")) {
            score = ((nAB / nA) + (nAB / nB)) / 2;
        }

        if (measure.contains("weighted")) {
            return nAB * score;
        } else {
            return score;
        }
    }
}
