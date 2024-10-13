package sorters;

import core.Index;
import core.SuffixSorter;
import core.TextFile;
import utilities.ProgressBar;

public class InsertionSort extends SuffixSorter {

    public InsertionSort(Index index) {
        super(index);
    }

    @Override
    public void sortIndex() {
        for (int i : ProgressBar.range(0, index.suffixArray.length, "Insertion sorting")) {
            //---------- TASK 3a: Insertion sort ------------------------------//
            int temp = index.suffixArray[i];
            int j = i - 1;
            while (j >= 0 && textFile.compareSuffixes(index.suffixArray[j], temp) > 0) {
                swap(j + 1, j);
                j--;
            }
            index.suffixArray[j + 1] = temp;
            //---------- END TASK 3a ------------------------------------------//

            // When debugging, print an excerpt of the suffix array.
            if (debug)
                index.print("i = " + i, new int[] { 0, i + 1 }, " * ");
        }
    }


    public static void main(String[] args) {
        // Run this for debugging.
        SuffixSorter.debugEnable();
        Index index = new Index("ABRACADABRA");
        new InsertionSort(index).buildIndex();
        index.check();
        index.print("ABRACADABRA");
        SuffixSorter.debugDisable();

        // /*
        // Some example performance tests.
        // Wait with these until you're pretty certain that your code works.
        String alphabet = "ABCD";
        for (int k = 1; k < 6; k++) {
            int size = k * 10_000;
            index = new Index(TextFile.random(size, alphabet));
            new InsertionSort(index).buildIndex();
            index.check();
            index.print(String.format("size: %,d, alphabet: '%s'", size, alphabet));
        }
        // */

        // What happens if you try different alphabet sizes?
        // (E.g., smaller ("AB") or larger ("ABC....XYZ"))

        // What happens if you use only "A" as alphabet?
        // (Hint: try much smaller test sizes)
    }
}
