package core;

import utilities.ProgressBar;

//Abstract class for suffix sorting algorithms.
public abstract class SuffixSorter {
    protected TextFile textFile;
    protected Index index;

    public SuffixSorter(Index index) {
        this.textFile = index.textFile;
        this.index = index;
    }

    public void buildIndex() {
        this.initIndex();
        this.sortIndex();
    }

    void initIndex() {
        index.suffixArray = new int[textFile.size()];
        for (int i = 0; i < index.suffixArray.length; i++)
            index.suffixArray[i] = i;
    }

    abstract protected void sortIndex();

    /*
     * Swap two indices in the suffix array.
     */
    protected void swap(int i, int j) {
        int tmp = index.suffixArray[i];
        index.suffixArray[i] = index.suffixArray[j];
        index.suffixArray[j] = tmp;
    }

    // Class methods for debugging.

    protected static boolean debug = false;
    private static boolean oldProgressBarVisibility;

    public static void debugEnable() {
        debug = true;
        oldProgressBarVisibility = ProgressBar.visible;
    }

    public static void debugDisable() {
        debug = false;
        ProgressBar.visible = oldProgressBarVisibility;
    }
}
