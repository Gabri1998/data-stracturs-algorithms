package core;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import sorters.Quicksort;
import utilities.ProgressBar;

// This class is executable for the purpose of testing binarySearchFirst.
public class Index {
    // Internal constant.
    static final String INDEX_PATH_SUFFIX = ".jix";

    public final TextFile textFile;
    public final Path indexPath;
    public int[] suffixArray;
    
    public Index(String text) {
        this(new TextFile(text));
    }
    
    public Index(Path textPath) throws IOException {
        this(new TextFile(textPath));
    }

    public Index(TextFile textFile) {
        this.textFile = textFile;
        this.indexPath = this.textFile.path == null
            ? null
            : Path.of(this.textFile.path + INDEX_PATH_SUFFIX);
    }
    
    public int size() {
        return textFile.size();
    }

    public void load() throws IOException {
        try (ObjectInputStream stream = new ObjectInputStream(
            Files.newInputStream(indexPath)
        )) {
            try {
                this.suffixArray = (int[]) stream.readObject();
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }
    }

    public void save() throws IOException {
        try (ObjectOutputStream stream = new ObjectOutputStream(
            Files.newOutputStream(indexPath)
        )) {
            stream.writeObject(suffixArray);
        }
    }

    /**
     * Search for the *first* index in the suffix array that matches the given value.
     * @return the smallest index in the suffix array whose suffix starts with `value`, or -1 if no such index exists.
     * 
     * Precondition: `suffixArray` is an alphabetically sorted array of positions representing suffixes in `textFile.text`.
     * 
     * See below main method for testing.
     */

   public int binarySearchFirst(String value) {
        String text = textFile.text;
        //---------- TASK 4: Binary search returning the first index ----------//
        int result = -1;

        int right = suffixArray.length - 1;
        int left = 0;
        int mid;

        while (left <= right) {
            mid = (right + left) / 2;
            String suffix = text.substring(suffixArray[mid]);
            if (suffix.startsWith(value)) {
                result = mid;
                right = mid - 1;
            }
            else if (suffix.compareTo(value) > 0) {
                right = mid - 1;
            }
            else {
                left = mid + 1;
            }
        }
        return result;

         //throw new UnsupportedOperationException("not implemented");

        //---------- END TASK 4 -----------------------------------------------//
    }

    // You can use this main method to test `binarySearchFirst`.
    public static void main(String[] args) throws IOException {
        Index index = new Index("ABRACADABRA");
        new Quicksort(index).buildIndex();
        index.check();
        index.print("Suffix array", new int[] { 0, index.textFile.size() }, "   ");

        // Search for some strings, e.g.: "ABRA", "RAC", "RAD", "AA"
        String value = "ABRA";
        System.out.format("Searching for: '%s'%n", value);
        int i = index.binarySearchFirst(value);
        if (i < 0) {
            System.out.format("--> String not found%n");
        } else {
            int pos = index.suffixArray[i];
            System.out.format("--> String found at index: %d --> text position: %d%n", i, pos);
        }

        /*
        // Next step is to search in a slightly larger text file, such as:
        Index index = new Index(Path.of("texts/bnc-tiny.txt"));
        ...
        // Try, e.g., to search for the following strings:
        // "and": String found at index: 7921 --> text position: 20165
        "ands" :  String found at index: 8065 --> text position: 10959
        "\n\n": string not found
         "zz":  String found at index: 28516 --> text position: 28516
         "zzzzz": String not found
        */
    }

    /*
     * Check correctness of the index.
     */
    public void check() {
        if (suffixArray == null)
            throw new AssertionError("Index has not been built or loaded.");

        if (suffixArray.length != textFile.size())
            throw new AssertionError("Index has incorrect size.");

        // Check that the suffix array contains each position exactly once.
        boolean[] included = new boolean[suffixArray.length];
        for (int position : suffixArray) {
            if (included[position])
                throw new AssertionError("Index contains position " + position + " multiple times.");
            included[position] = true;
        }
        for (int i = 0; i != included.length; ++i)
            if (!included[i])
                throw new AssertionError("Index does not contain position " + i);

        int left = suffixArray[0];
        int size = suffixArray.length;
        ProgressBar<?> progressBar = new ProgressBar<>(suffixArray.length, "Checking suffix array");
        int progressBarInterval = size / 10_000 + 1;
        for (int i = 1; i < size; i++) {
            if (i % progressBarInterval == 0)
                progressBar.setValue(i);
            int right = suffixArray[i];
            if (!(textFile.compareSuffixes(left, right) < 0)) {
                throw new AssertionError(
                    String.format(
                        "Ordering error in positions %d-%d:'%s...' > %d'%s...'",
                        i, left, textFile.text.substring(left, Math.min(left + 10, size)),
                        right, textFile.text.substring(right, Math.min(right + 10, size))
                    )
                );
            }
            left = right;
        }
        progressBar.setValue(size);
        progressBar.close();
    }

    // The below functions are only used for debugging.
    // You can ignore them.
    
    public void print(String header) {
        this.print(header, new int[] { 0, textFile.size() }, "  ");
    }

    public void print(String header, int[] breakpoints, String indicators) {
        this.print(header, breakpoints, indicators, 3, 40);
    }

    public void print(String header, int[] breakpoints, String indicators, int context, int maxSuffix) {
        int digits = Math.max(3, String.valueOf(textFile.size()).length());

        System.out.println("--- " + header + " " + "-".repeat(75 - header.length()));
        System.out.format("%" + (digits + 3) + "s%" + (digits + 6) + "s      suffix%n", "index", "textpos");
        String dotdotdot = String.format("%" + (digits + 2) + "s%" + (digits + 6) + "s", "...", "...");
        int endRange = 0;
        for (int k : breakpoints) {
            int startRange = k - context;
            if (endRange < startRange - 1) {
                System.out.println(dotdotdot);
            } else {
                startRange = endRange;
            }
            endRange = k + context;
            for (int i = startRange; i < endRange; i++) {
                if (0 <= i && i < textFile.size()) {
                    char ind = indicators.charAt(0);
                    for (int bp = 0; bp < breakpoints.length; bp++) {
                        if (i >= breakpoints[bp])
                            ind = indicators.charAt(bp + 1);
                    }
                    int suffixPos = suffixArray[i];
                    String suffixString = (suffixPos + maxSuffix <= textFile.size()
                        ? textFile.text.substring(suffixPos, suffixPos + maxSuffix) + "..."
                        : textFile.text.substring(suffixPos));
                    suffixString = suffixString.replaceAll("\n", "\\\\n").replaceAll("\r", "\\\\n");
                    System.out.format(
                        "%c %" + digits + "d  --> %" + digits + "d  -->  %s%n",
                        ind, i, suffixPos, suffixString
                    );
                }
            }
        }
        if (endRange < textFile.size()) {
            System.out.println(dotdotdot);
        }
        System.out.println("-".repeat(80));
        System.out.println();
    }
}
