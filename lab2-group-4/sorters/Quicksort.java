package sorters;
import java.io.IOException;

import core.Index;
import core.SuffixSorter;
import core.TextFile;
import utilities.ProgressBar;

public class Quicksort extends SuffixSorter {
    PivotSelector pivotSelector;
    ProgressBar<Void> progressBar;
    int progressBarSpanSize;

    public Quicksort(Index index, PivotSelector pivotSelector) {
        super(index);
        this.pivotSelector = pivotSelector;
    }
    
    public Quicksort(Index index) {
        this(index, PivotSelector.TAKE_MIDDLE);
    }
    
    @Override
    public void sortIndex() {
        int size = textFile.size();
        this.progressBar = new ProgressBar<>(size, "Quicksorting");
        this.progressBarSpanSize = size / 10_000;
        this.quicksort(0, size);
        this.progressBar.setValue(size);
        this.progressBar.close();
    }

    /* 
     * Sort the range of `index.suffixArray` from `start` to `end`.
     * Note that `end` points to *after* the  last element in the range.
     */
    public void quicksort(int start, int end) {
        int size = end - start;

        // Don't update the progress bar unnecessarily often.
        if (size >= progressBarSpanSize)
            this.progressBar.setValue(start);

        //---------- TASK 3b: Quicksort ---------------------------------------//
        // * Don't forget about the base case!
        if (size <= 1) {
            return;
        }

        // * Make use of the method `partition` below.
        int partitionPivot = partition(start, end);

        quicksort(start, partitionPivot);
        quicksort(partitionPivot + 1, end);

        //---------- END TASK 3b ----------------------------------------------//
    }

    /* 
     * Partition the range of `index.suffixArray` from `start` to `end`.
     * Returns the final position of the pivot element.
     *
     * Note that `end` points to *after* the last element in the range.
     *
     * Precondition: all suffixes in the range start with the same prefix of length `offset`.
     */
    public int partition(int start, int end) {
        // Select the pivot, and find the pivot suffix.
        int pivotIndex = pivotSelector.pivotIndex(this.index, start, end);
        int pivotSuffix = index.suffixArray[pivotIndex];

        // Swap the pivot so that it is the first element.
        this.swap(start, pivotIndex);

        // This is the Hoare partition scheme, where the pointers move in opposite direction.
        int lo = start + 1;
        int hi = end - 1;
        
        // This is the value that will be returned in the end - the final position of the pivot.
        int newPivotIndex = -1;

        //---------- TASK 3b: Quicksort ---------------------------------------//
        // The following methods may prove convenient: swap, textFile.compareSuffixes
        while (true) {
            while (lo <= hi && textFile.compareSuffixes(index.suffixArray[lo], pivotSuffix) < 0) {
                lo++;
            }
            while (lo <= hi && textFile.compareSuffixes(index.suffixArray[hi], pivotSuffix) > 0) {
                hi--;
            }
            if (lo > hi) break;
            swap(lo, hi);
            lo++;
            hi--;
        }
        swap(start, hi);
        newPivotIndex = hi;

        //---------- END TASK 3b ----------------------------------------------//

        // When debugging, print an excerpt of the suffix array.
        if (debug) {
            String pivotValue = (pivotSuffix + 20 < textFile.size()
                ? textFile.text.substring(pivotSuffix, pivotSuffix + 20) + "..."
                : textFile.text.substring(pivotSuffix)
            );
            String header = String.format("start: %d, end: %d, pivot: %s", start, end, pivotValue);
            index.print(header, new int[] { start, newPivotIndex, newPivotIndex + 1, end }, " <=> ");
        }

        return newPivotIndex;
    }


    public static void main(String[] args) throws IOException {
        // Run this for debugging.
        SuffixSorter.debugEnable();
        Index index = new Index("ABRACADABRA");
        new Quicksort(index).buildIndex();
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
            new Quicksort(index).buildIndex();
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
