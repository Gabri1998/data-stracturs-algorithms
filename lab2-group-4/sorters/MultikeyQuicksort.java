package sorters;
import core.Index;
import core.SuffixSorter;
import core.TextFile;
import utilities.ProgressBar;

public class MultikeyQuicksort extends SuffixSorter {
    PivotSelector pivotSelector;
    ProgressBar<Void> progressBar;
    int progressBarSpanSize;

    public MultikeyQuicksort(Index index, PivotSelector pivotSelector) {
        super(index);
        this.pivotSelector = pivotSelector;
    }

    public MultikeyQuicksort(Index index) {
        this(index, PivotSelector.TAKE_MIDDLE);
    }
    
    @Override
    public void sortIndex() {
        int size = textFile.size();
        this.progressBar = new ProgressBar<>(size, "Multikey quicksorting");
        this.progressBarSpanSize = size / 10_000;
        this.multikeyQuicksort(0, size, 0);
        this.progressBar.setValue(size);
        this.progressBar.close();
    }

    /* 
     * Sort the range of `index.suffixArray` from `start` to `end`.
     * Note that `end` points to *after* the  last element in the range.
     *
     * Precondition: all suffixes in the range start with the same prefix of length `offset`.
     */
    public void multikeyQuicksort(int start, int end, int offset) {
        int size = end - start;

        // Don't update the progress bar unnecessarily often.
        if (size >= progressBarSpanSize)
            this.progressBar.setValue(start);

        //---------- TASK 5: Multikey quicksort -------------------------------//
        // TODO: Replace these lines with your solution!
        // Hints:
        // - Make use of the `partition` method below.
        // - Don't forget about the base case.
        if (size <= 1) {
            return;
        }
        IndexPair pivots = partition(start, end, offset);
        multikeyQuicksort(start, pivots.start, offset);
        multikeyQuicksort(pivots.start, pivots.end, offset + 1);
        multikeyQuicksort(pivots.end, end, offset);

     //   if (true) throw new UnsupportedOperationException();
        //---------- END TASK 5 -----------------------------------------------//
    }

    private class IndexPair {
        final int start;
        final int end;
        IndexPair(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    /* 
     * Partition the range of `index.suffixArray` from `start` to `end` into three parts.
     * - All suffixes in the middle part share the same character at position `offset`.
     * - All suffixes in the left part have a smaller character at position `offset`.
     * - All suffixes in the right part have a larger character at position `offset`.
     * 
     * Returns the range of the middle part.
     *
     * Note that `end` points to *after* the last element in the range.
     *
     * Precondition: all suffixes in the range start with the same prefix of length `offset`.
     */
    public IndexPair partition(int start, int end, int offset) {
        // Select the pivot, and find the pivot character.
        int pivotIndex = pivotSelector.pivotIndex(index, start, end);
        char pivotChar = textFile.getChar(index.suffixArray[pivotIndex] + offset);

        // Swap the pivot so that it is the first element.
        this.swap(start, pivotIndex);

        // Initialise the middle pointers.
        int middleStart = start;
        int middleEnd = end;

        //---------- TASK 5: Multikey quicksort -------------------------------//
        // TODO: Replace these lines with your solution!
        // Hint: the methods `swap` and `textFile.getChar` are at your disposal.


        int lo = start + 1;
        int hi = end - 1;
        pivotIndex = start;
        while (lo <= hi) {
            char chr = textFile.getChar(index.suffixArray[lo]+offset);
            if (chr < pivotChar) {
                swap(lo, pivotIndex);
                lo++;
                pivotIndex++;
            }
            else if (chr > pivotChar) {
                swap(lo, hi);
                hi--;
            }
            else {
                lo++;
            }
        }
        middleEnd = lo;
        middleStart = pivotIndex;

     //   if (true) throw new UnsupportedOperationException();
        //---------- END TASK 5 -----------------------------------------------//

        // When debugging, print an excerpt of the suffix array.
        if (debug) {
            String pivotValue = ".".repeat(offset) + String.valueOf(pivotChar);
            String header = String.format("start: %d, end: %d, pivot: %s", start, end, pivotValue);
            index.print(header, new int[] { start, middleStart, middleEnd, end }, " <=> ");
        }

        // Return the new range containing all elements selected by the pivot char.
        // Note that `middleEnd` must point to *after* the element in the range.
        return new IndexPair(middleStart, middleEnd);
    }


    public static void main(String[] args) {
        // Run this for debugging.
        SuffixSorter.debugEnable();
        Index index = new Index("ABRACADABRA");
        new MultikeyQuicksort(index).buildIndex();
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
            new MultikeyQuicksort(index).buildIndex();
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
