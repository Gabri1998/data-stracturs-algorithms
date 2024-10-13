
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiFunction;

import core.Index;
import core.SuffixSorter;
import core.TextFile;
import sorters.BuiltinSort;
import sorters.InsertionSort;
import sorters.MultikeyQuicksort;
import sorters.PivotSelector;
import sorters.Quicksort;
import utilities.CommandParser;
import utilities.Stopwatch;

public class BuildIndex {

    static Map<String, BiFunction<Index, PivotSelector, SuffixSorter>> suffixSorters = Map.of(
        "insertion", ((index, pivotSelector) -> new InsertionSort(index)),
        "quicksort", ((index, pivotSelector) -> pivotSelector == null ? new Quicksort(index) : new Quicksort(index, pivotSelector)),
        "multikey", ((index, pivotSelector) -> pivotSelector == null ? new MultikeyQuicksort(index) : new MultikeyQuicksort(index, pivotSelector)),
        "builtin", ((index, pivotSelector) -> new BuiltinSort(index))
    );

    static Map<String, PivotSelector> pivotSelectors = Map.of(
        "first", PivotSelector.TAKE_FIRST,
        "middle", PivotSelector.TAKE_MIDDLE,
        "random", PivotSelector.TAKE_RANDOM,
        "median", PivotSelector.TAKE_MEDIAN_OF_THREE,
        "adaptive", PivotSelector.ADAPTIVE
    );

    public static void main(String[] args) throws IOException {
        CommandParser parser = new CommandParser("BuildIndex", "Build an inverted search index.");
        parser.addArgument("--textfile", "-f", "text file (utf-8 encoded)")
            .makeRequired();
        parser.addArgument("--algorithm", "-a", "sorting algorithm")
            .makeRequired().setChoices(suffixSorters.keySet());
        parser.addArgument("--pivot", "-p", "pivot selectors (only for quicksort algorithms)")
            .setChoices(pivotSelectors.keySet());

        CommandParser.Namespace options = parser.parseArgs(args);

        // Create stopwatches to time the execution of each phase of the program.
        Stopwatch stopwatchTotal = new Stopwatch();
        Stopwatch stopwatch = new Stopwatch();

        // Read the text file.
        Path textPath = Path.of(options.getString("textfile"));
        TextFile textFile;
        try {
            textFile = new TextFile(textPath);
        } catch (NoSuchFileException e) {
            System.err.format(
                "\nERROR: I cannot find the text file '%s'.\n" +
                    "Make sure you specify both the directory and the filename correctly.\n\n",
                textPath
            );
            System.exit(1);
            throw e;
        }
        stopwatch.finished(String.format("Reading %s chars from '%s'", textFile.size(), textPath));

        // The index.
        Index index = new Index(textFile);
        
        // Select and instantiate the sorting algorithm.
        PivotSelector pivotSelector = null;
        if (options.getString("pivot") != null)
            pivotSelector = pivotSelectors.get(options.getString("pivot"));
        BiFunction<Index, PivotSelector, SuffixSorter> sortingAlgorithm = suffixSorters.get(options.getString("algorithm"));
        SuffixSorter sorter = sortingAlgorithm.apply(index, pivotSelector);

        // Build the index using the selected sorting algorithm.
        sorter.buildIndex();
        stopwatch.finished("Building index");
 
        // Check that the index is sorted.
        index.check();
        stopwatch.finished("Checking index");

        // Save it to an index file.
        index.save();
        stopwatch.finished(String.format("Saving index to '%s'", index.indexPath));

        stopwatchTotal.finished("In total the program");
    }
}
