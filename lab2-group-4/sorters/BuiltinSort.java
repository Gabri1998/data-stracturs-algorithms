package sorters;

import java.util.Arrays;
import java.util.Comparator;

import core.Index;
import core.SuffixSorter;

public class BuiltinSort extends SuffixSorter {

    public BuiltinSort(Index index) {
        super(index);
    }

    @Override
    public void sortIndex() {
        Comparator<Integer> compare = (a, b) -> textFile.compareSuffixes(a, b);
        // Java cannot sort an int[] using a custom comparator,
        // so we have to box the elements to Integer instead. 
        // We use streams for that:
        int[] sorted = Arrays
            .stream(index.suffixArray)  // now we have an IntStream
            .boxed()                    // now it's a Stream<Integer>
            .sorted(compare)            // now it's sorted
            .mapToInt(i -> i)           // now back to an IntStream
            .toArray();                 // and finally an int[]
        // And finally we copy the result back into the original array:
        System.arraycopy(sorted, 0, index.suffixArray, 0, index.suffixArray.length);
    }

}
