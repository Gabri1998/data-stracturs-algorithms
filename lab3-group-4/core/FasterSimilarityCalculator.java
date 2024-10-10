package core;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import datatypes.Set;
import utilities.ProgressBar;

public class FasterSimilarityCalculator extends SimilarityCalculator {

    /**
     * Phase 2: Build index of n-grams.
     * Uses the instance varable `fileNgrams`.
     * Stores the result in the instance variable `ngramIndex`.
     */
    @Override
    public void buildNgramIndex() {
        //---------- TASK 2a: Build n-gram index ----------------------------//
        // Note: You can use a ProgressBar in your outermost loop if you want.
        // See in `similarityCalculator.java` how it is used.

        // TODO: Replace these lines with your solution!

        for (Path path : fileNgrams) {
            Set<Ngram> ngrams = fileNgrams.get(path);
            for (Ngram ngram : ngrams) {

                this.ngramIndex.get(ngram).add(path);
            }
        }








        //throw new UnsupportedOperationException();
        //---------- END TASK 2a --------------------------------------------//
    }

    /**
     * Phase 3: Count how many n-grams each pair of files has in common.
     * This version should use the `ngramIndex` to make this function much more efficient.
     * Stores the result in the instance variable `intersections`.
     */
    @Override
    public void computeIntersections() {
        //---------- TASK 2b: Compute n-gram intersections ------------------//
        // Note 1: Intersection is a commutative operation, i.e., A ∩ B == B ∩ A.
        // This means that you restrict yourself to only compute the intersection
        // for path pairs (p,q) where p < q (in Java: p.compareTo(q) < 0).

        // Note 2: You can use a ProgressBar in your outermost loop if you want.
        // See in `similarityCalculator.java` how it is used.

        // TODO: Replace these lines with your solution!
        for (Ngram ngram:ngramIndex) {
            Set<Path> paths = ngramIndex.get(ngram);
            for (Path path1 : paths) {

                for (Path path2 : paths) {
                    if (path1.compareTo(path2) < 0){
                        PathPair pathPair= new PathPair(path1,path2);
                        this.intersections.get(pathPair).add(ngram);
                }
                }
            }
        }



        //throw new UnsupportedOperationException();
        //---------- END TASK 2b --------------------------------------------//
    }
}
