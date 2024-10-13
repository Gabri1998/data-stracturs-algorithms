package sorters;

import java.util.Random;

import core.Index;

/* This interface abstracts over the pivot selection strategy in quicksort.
 * We provide several implementations:
 *  * TAKE_FIRST
 *  * TAKE_MIDDLE
 *  * TAKE_RANDOM
 *  * TAKE_MEDIAN_OF_THREE
 *  * ADAPTIVE
 */
public interface PivotSelector {
    public int pivotIndex(Index index, int start, int end);

    // A pivot selector that chooses the first element.
    static final PivotSelector TAKE_FIRST = new PivotSelector() {
        public int pivotIndex(Index index, int start, int end) {
            return start;
        }
    };

    // A pivot selector that chooses the middle element.
    static final PivotSelector TAKE_MIDDLE = new PivotSelector() {
        public int pivotIndex(Index index, int start, int end) {
            return (start + end) / 2;
        }
    };

    // A pivot selector that chooses a random index.
    static final PivotSelector TAKE_RANDOM = new PivotSelector() {
        private Random random = new Random();
        public int pivotIndex(Index index, int start, int end) {
            return start + random.nextInt(end - start);
        }
    };

    public static int medianOfThreeIndex(Index index, int i, int j, int k) {
        int pos_i = index.suffixArray[i];
        int pos_j = index.suffixArray[j];
        int pos_k = index.suffixArray[k];

        // Out of the below three Booleans, two must be equal. (Why?)
        boolean less_or_equal_ij = index.textFile.compareSuffixes(pos_i, pos_j) <= 0;
        boolean less_or_equal_jk = index.textFile.compareSuffixes(pos_j, pos_k) <= 0;
        boolean less_or_equal_ki = index.textFile.compareSuffixes(pos_k, pos_i) <= 0;

        if (less_or_equal_ij == less_or_equal_jk) {
            // The sequence [i], [j], [k] is ascending or descending.
            return j;
        }
        else if (less_or_equal_jk == less_or_equal_ki) {
            // The sequence [j], [k], [i] is ascending or descending.
            return k;
        }
        else { // less_or_equal_ki == less_or_equal_ij
            // The sequence [k], [i], [j] is ascending or descending.
            return i;
        }
    }

    // A pivot selector that uses median of three.
    static final PivotSelector TAKE_MEDIAN_OF_THREE = new PivotSelector() {
        public int pivotIndex(Index index, int start, int end) {
            // We choose the median between the first, middle, last index.
            int mid = (start + end - 1) / 2;
            return medianOfThreeIndex(index, start, mid, end - 1);
        }
    };

    // A pivot selector that adapts to the range size.
    static final PivotSelector ADAPTIVE = new PivotSelector() {
        public int pivotIndex(Index index, int start, int end) {
            int size = end - start;

            // For small arrays, just pick the first element.
            if (size < 10)
                return TAKE_FIRST.pivotIndex(index, start, end);

            // For medium arrays, pick median-of-three.
            if (size < 100)
                return TAKE_MEDIAN_OF_THREE.pivotIndex(index, start, end);

            // For large arrays, pick median-of-three of median-of-three.
            int lo = start;
            int hi = end - 1;
            int mid = (lo + hi) / 2;

            int d = size / 8;
            int i = medianOfThreeIndex(index, lo, lo + d, lo + 2 * d);
            int j = medianOfThreeIndex(index, hi, hi - d, hi - 2 * d);
            int k = medianOfThreeIndex(index, mid - d, mid, mid + d);
            return medianOfThreeIndex(index, i, j, k);
        }
    };
}
