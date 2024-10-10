import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import core.FasterSimilarityCalculator;
import core.PathPair;
import core.SimilarityCalculator;
import datatypes.AVLMap;
import datatypes.AVLSet;
import datatypes.BSTMap;
import datatypes.BSTSet;
import datatypes.ListMap;
import datatypes.ListSet;
import utilities.CommandParser;
import utilities.Stopwatch;

/**
 * Command-line tool for detecting similar files.
 */
public class PlagiarismDetector {
    static String[] mapImplementations = new String[] {"list", "bst", "avl"};

    public static void main(String[] args) throws IOException {
        CommandParser parser = new CommandParser("PlagiarismDetector", "Tool for detecting similar files.");
        parser.addArgument("--documents", "-d", "path to directory of documents")
            .makeRequired();
        parser.addArgument("--index", "-i", "use the optimised file index (default: use the slow version)")
            .makeTrueOption();
        parser.addArgument("--map", "-m", "map implementation (default: use key-value list)")
            .setChoices(mapImplementations).setDefault("list");
        parser.addArgument("--ngram", "-n", "ngram size (default: 5)")
            .makeInteger().setDefault(5);
        parser.addArgument("--limit", "-l", "limit the number of similar file pairs (default: 10)")
            .makeInteger().setDefault(10);
        parser.addArgument("--similarity", "-s", "similarity measure to sort by (default: 'absolute')")
            .setChoices(SimilarityCalculator.similarityMeasures).setDefault("absolute");

        CommandParser.Namespace options = parser.parseArgs(args);
        String docPath = options.getString("documents");
        boolean useIndex = options.getBoolean("index");
        String useMap = options.getString("map");
        int ngramSize = options.getInteger("ngram");
        int limitResults = options.getInteger("limit");
        String similarityMeasure = options.getString("similarity");

        SimilarityCalculator calculator;
        if (useIndex)
            calculator = new FasterSimilarityCalculator();
        else
            calculator = new SimilarityCalculator();

        // Initialise the maps.
        if (useMap.equals("avl")) {
            calculator.fileNgrams = new AVLMap<>(() -> new AVLSet<>());
            calculator.ngramIndex = new AVLMap<>(() -> new AVLSet<>());
            calculator.intersections = new AVLMap<>(() -> new AVLSet<>());
        } else if (useMap.equals("bst")) {
            calculator.fileNgrams = new BSTMap<>(() -> new BSTSet<>());
            calculator.ngramIndex = new BSTMap<>(() -> new BSTSet<>());
            calculator.intersections = new BSTMap<>(() -> new BSTSet<>());
        } else {
            calculator.fileNgrams = new ListMap<>(() -> new ListSet<>());
            calculator.ngramIndex = new ListMap<>(() -> new ListSet<>());
            calculator.intersections = new ListMap<>(() -> new ListSet<>());
        }

        // Find all .txt files in the directory and sort the filenames.
        // Uses the Java 8 streams API - very handy Java feature which we don't cover in the course.
        // Read more about streams in 'SimilarityCalculator.findMostSimilar'
        Path[] paths = Files.list(Paths.get(docPath))
            .filter((Path p) -> p.toString().endsWith(".txt"))
            .sorted()
            .toArray(Path[]::new);
        if (paths.length == 0)
            throw new NoSuchFileException("Empty directory");

        // Create stopwatches to time the execution of each phase of the program.
        Stopwatch stopwatchTotal = new Stopwatch();
        Stopwatch stopwatch = new Stopwatch();

        // Phase 1: Read n-grams from all input files.
        calculator.readNgramsFromFiles(paths, ngramSize);
        calculator.fileNgrams.validate();
        stopwatch.finished("Reading all input files");

        // Phase 2: Build index of n-grams.
        if (useIndex) {
            calculator.buildNgramIndex();
            calculator.ngramIndex.validate();
            stopwatch.finished("Building n-gram index");
        }

        // Phase 3: Compute the n-gram intersections of all file pairs.
        calculator.computeIntersections();
        calculator.intersections.validate();
        stopwatch.finished("Computing intersections");

        // Phase 4: Find the L most similar file pairs, arranged in decreasing order of similarity.
        PathPair[] mostSimilar = calculator.findMostSimilar(limitResults, similarityMeasure);
        stopwatch.finished("Finding the most similar files");

        stopwatchTotal.finished("In total the program");

        // Print out some statistics.
        System.out.println();
        System.out.println("Balance statistics:");
        System.out.println("  fileNgrams: " + calculator.fileNgrams);
        if (useIndex)
            System.out.println("  ngramIndex: " + calculator.ngramIndex);
        System.out.println("  intersections: " + calculator.intersections);

        // Print out the plagiarism report!
        System.out.println();
        System.out.println("Plagiarism report:");
        for (String measure : SimilarityCalculator.similarityMeasures)
            System.out.format("%10s", measure.contains("-") ? measure.replaceAll("-.*", "") : "");
        System.out.println();
        for (String measure : SimilarityCalculator.similarityMeasures)
            System.out.format("%10s", measure.replaceAll(".*-", ""));
        System.out.println();
        int maxFilenameSize = Arrays.stream(mostSimilar)
            .mapToInt((PathPair p) -> p.path1.getFileName().toString().length())
            .max().getAsInt();
        for (PathPair pair : mostSimilar) {
            for (String measure : SimilarityCalculator.similarityMeasures) {
                int decimals = measure.equals("absolute") ? 0 : measure.contains("weighted") ? 2 : 3;
                System.out.format("%10." + decimals + "f", calculator.similarity(pair, measure));
            }
            System.out.format("  %-" + maxFilenameSize + "s %s%n", pair.path1.getFileName(), pair.path2.getFileName());
      }
    }
}
