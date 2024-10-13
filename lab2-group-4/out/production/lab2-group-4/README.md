# Lab: Text indexing

In this lab, you will implement a very fast search engine for large text files.
It has the following purposes:

* to teach sorting algorithms, both standard and tailor-made ones,
* how to tailor-make algorithms for special circumstances,
* more binary search training,
* and to experiment with different complexity classes.

### Important notes

* This lab is part of the examination of the course.
  Therefore, you must not copy code from or show code to other students.
  You are welcome to discuss general ideas with one another, but anything you do must be **your own work**.

* You can solve this lab in either Java or Python, and it's totally up to you.
  - Since Python is an interpreted language, it's much slower than Java — about 20–40 times slower on the tasks in this lab.
    So if you want something that's blazingly fast you should choose Java.
  - A faster alternative to standard Python is [PyPy](https://www.pypy.org).
    It is 5–8 times faster than the standard Python interpreter.
  - You grading will not be affected by your choice of language!
    But if you choose Java, you can experiment with larger texts than if you choose Python.

* This is the **Java version** of the lab.
  To switch to the Python version, [see here](https://chalmers.instructure.com/courses/27979/pages/the-lab-system#switching-language).

* Further info on Canvas:
  - [general information](https://chalmers.instructure.com/courses/27979/pages/labs-general-information)
  - [the lab system](https://chalmers.instructure.com/courses/27979/pages/the-lab-system)
  - [running Java labs](https://chalmers.instructure.com/courses/27979/pages/running-java-labs)

### Overview of the lab

The lab repository consist of a number of source (explained below), a directory of **texts**, and a file **answers.txt** where you will answer questions for the lab.

Here is a very general overview of this document and what you have to do to complete the lab:

* The [background](#background) contains descriptions of the source files.
* In [part 1](#part-1-testing-naive-text-search), you will test a naive linear search algorithm and see that it is really slow.
* [Part 2](#part-2-suffix-arrays-background-information) tries to explain the theory behind suffix arrays.
* In [part 3](#part-3-insertion-sort), you will implement insertion sort, which can be used to build search indexes for text files.
* In [part 4](#part-4-quicksort), you will implement quicksort, which makes it possible to build indexes for much larger text files.
* In [part 5](#part-5-searching-using-the-suffix-array), you will use the search index to find strings in the text file quickly.
* In [part 6](#part-6-multi-key-quicksort), you will implement multi-key quicksort, a specialized sorting algorithm for strings.
* In [part 7](#part-7-empirical-complexity-analysis), you will make an empirical complexity analysis of your implementations.
* Finally, there are some [optional tasks](#optional-tasks) that you can play around with if you think this was a fun lab!

## Background

If we want to search for a string in a text file, we usually iterate through the file from the start until we find an occurrence of the string.
This works fine for small-to-medium sized texts, but when the files contain millions of words this simple idea becomes too slow.

To solve this, we can calculate a *search index* in advance.
Much like an index in a book, a search index is a data structure that allows us to quickly find all the places where a given string appears.

Search indexes are used in many applications — for example, database engines use them to be able to search quickly.

In this lab, you will build a search index using a data structure called a *suffix array*.
Suffix arrays build on ideas from *sorting algorithms* and *binary search* to search efficiently in large texts.
By the end, you will be able to search through large texts in a millisecond!
(Building the index will take some time, but the search will be almost instantaneous.)

Later in the course, you will learn about two more data structures, called *hash tables* and *search trees*, which can be used to implement a search index that can be updated quickly as well.

### The source files

The lab directory contains several source files, organized in packages.

The **core** infrastructure:
- **TextFile.java**: Represents the text that is to be indexed.
- **Index.java**: The search index, with a binary search method (*to be completed in task 5*).
- **SuffixSorter.java**: Abstract base class for sorting algorithms operating on the index to create a sorted suffix array.

Sorting algorithms in **sorters** for sorting the index:

- **BuiltinSort.java**: This uses Java's built-in sorting algorithm.
- **InsertionSort.java**: Insertion sort (*to be completed in task 3*).
- **Quicksort.java**: Quicksort (*to be completed in task 4*).
- **MultikeyQuicksort.java**: Multi-key quicksort (*to be completed in task 7*).
- **PivotSelector.java**: Several different pivot selection algorithms, used by (multi-key) quicksort.

Some general **utilities** that you can ignore:

- **ProgressBar.java**: Command-line progress bar, heavily inspired by the [tqdm](https://tqdm.github.io) library.
- **CommandParser.java**: Command-line argument parser, heavily inspired by Python's builtin [argparse](https://docs.python.org/3/library/argparse.html) module.
- **Stopwatch.java**: Very simple class for measuring runtime.

Top-level programs:

- **BuildIndex.java**: Command-line program that you invoke to build a suffix array and save it to disk.
- **SearchIndex.java**: Command-line program with which you can search in texts.

And non-source code files:

- **answers.txt**: Here you will write down answers to questions in this lab.
- **texts**: A directory containing several differently-sized text files for experimentation.

## Part 1: Testing naive text search

In this part, you will experiment with a very stupid and slow baseline for searching for strings in texts.

It is already implemented in the function `linearSearch` in **SearchIndex.java** — take some time to read and understand it.
Since Java does not have generators (in contrast to Python), the function returns an *iterator* of search results:

```java
/*
 * Generate all positions in the text file matching the given value.
 * This is done by naive linear search through the text.
 */
public static Iterator<Integer> linearSearch(TextFile textFile, String value) {
    return new Iterator<>() {
        int position = 0;

        @Override
        public boolean hasNext() {
            while (true) {
                int end = position + value.length();
                if (end > textFile.size())
                    return false;
                if (value.equals(textFile.text.substring(position, end)))
                    return true;
                position++;
            }
        }

        @Override
        public Integer next() {
            if (!hasNext())
                throw new NoSuchElementException();
            return position++;
        }
    };
}
```

This allows us to consume the stream of search results *on demand*.

### How to search in a text

You can already now use the program to search in text files, however only using linear search.
Simply run the main class **SearchIndex** without arguments and answer the questions like this:

<pre>
$ java SearchIndex
(...)
Enter values:
 * text file (utf-8 encoded): <b>texts/bnc-larger.txt.gz</b>
 * use linear search (much slower than binary search)? (yes/+/true for true) (no/-/ENTER for false): <b>yes</b>
 * number of matches to show (default: 10 matches) (ENTER for 10): 
 * context to show to the left and right (default: 40 characters) (ENTER for 40): 
 * trim each search result to the matching line? (yes/+/true for true) (no/-/ENTER for false): 
Reading 26836050 chars 'texts/bnc-larger.txt.gz' took 0.20 seconds.

Search key (ENTER to quit): 
</pre>

Note that you have to answer the first two questions.
The second one about linear search you have to answer "yes", because the other kind of search isn't implemented yet.

A more compact way of running the program is to provide the arguments directly on the command line:

```
$ java SearchIndex --textfile texts/bnc-larger.txt.gz --linear-search
Reading 26836050 chars from 'texts/bnc-larger.txt.gz' took 0.30 seconds.
Search key (ENTER to quit): 
```

If you use the command history of your terminal (usually up-arrow), this allows you to save a lot of repetitive typing.

### Task for part 1: Perform some linear searches

Search for the following strings in the largest text file you have (such as **bnc-larger.txt.gz**): "and", "20th-century", and "this-does-not-exist".
For each search, write down how many matches you get and how long time each query takes.

Write down your answers in **answers.txt**.

## Part 2: Suffix arrays (background information)

As you hopefully noted, linear search becomes quite slow when we search through large amounts of text.
But there are much faster ways to search in text.
In this lab you will experiment with a very nice data structure called a *suffix array* that can be used as a search index for this problem.

In this section, imagine that we want to build a search index for the text "ABRACADABRA".
A *suffix* is a substring of the text that starts at some position and goes all the way to the end of the text.
For example, "ADABRA" is a suffix of the text above.

Conceptually, a suffix array consists of *all suffixes* of the text, sorted alphabetically.
Here are the suffixes of our example text, together with the position (in characters) where each one starts, written as an array of pairs of the form `(position, suffix)`:

```
[ ( 0, "ABRACADABRA"),
  ( 1, "BRACADABRA"),
  ( 2, "RACADABRA"),
  ( 3, "ACADABRA"),
  ( 4, "CADABRA"),
  ( 5, "ADABRA"),
  ( 6, "DABRA"),
  ( 7, "ABRA"),
  ( 8, "BRA"),
  ( 9, "RA"),
  (10, "A"),
]
```

The suffix array for the text is conceptually just this list of suffixes sorted alphabetically:

```
[ (10, "A"),
  ( 7, "ABRA"),
  ( 0, "ABRACADABRA"),
  ( 3, "ACADABRA"),
  ( 5, "ADABRA"),
  ( 8, "BRA"),
  ( 1, "BRACADABRA"),
  ( 4, "CADABRA"),
  ( 6, "DABRA"),
  ( 9, "RA"),
  ( 2, "RACADABRA"),
]
```

Now, how can we find a specific string in the text?
It turns out that *we can use binary search on the suffix array*!
For example, suppose we want to find all occurrences of "BRA".
Can you see how to do this?

Here is the idea:

* There are two occurrences of "BRA" ("A«**BRA**»CADABRA" and "ABRACADA«**BRA**»").
Put another way, there are two suffixes that *start with* "BRA" (positions 1 and 8).
* In alphabetical order, these suffixes must all be ≥ "BRA" and < "BRB".
So they must appear together in the suffix array (as you can see above), in one "block": `(8,"BRA")` followed by `(1,"BRACADABRA")`.
* In fact, the suffix array remains sorted when we restrict each suffix to just the first three characters (same length as "BRA").
* We can use binary search in this restricted suffix array to find the "block" of suffixes starting with "BRA"!
  Everything in this "block" is a match.

Make sure you understand this before going on!

### We don't need to store the suffixes

If the text consists of N characters, the suffix array will consist of N strings, ranging from the whole text to a single character at the end.
If we would store every single suffix in this array, it would use up an enormous amount of memory (quadratic in N) — but we don't have to store it like that.
Instead, the suffix array is an array of integers, which are the *positions* of the suffixes in the text.
This array is then sorted — not numerically by position, but alphabetically by the substring starting at that position.
So, for the text above, we start with the array of positions «0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10» , and sort it to get «10, 7, 0, 3, 5, 8, 1, 4, 6, 9, 2».

The text itself is stored in the immutable class `TextFile` (in **core/TextFile.java**).
This is the only place where text data is stored.
You can access it using the string attribute `text`.

For the search index, we have the class `Index` (in **core/Index.java**).
It contains attribute `textFile` as above and `suffixArray` for the array of suffix positions:

```java
public class Index {
    TextFile textFile;
    int[] suffixArray;
    ...
}
```

### Comparing suffixes

To sort the suffix array, we have to be able to compare the suffixes.
For this purpose we provide the method `compareSuffixes` in `TextFile`, which takes as input two integers, representing the two suffixes in the text.
It returns a negative number if the first suffix is smaller than the second, and a positive number if it is larger.
If they are equal, the method returns 0.

(Exercise for you: when are two suffixes in a text equal?)

One very common mistake is to try to compare the substrings using the substring method:

```java
// Don't do this!
public int compareSuffixes(int positionA, int positionB) {
    String suffixA = this.text.substring(positionA);
    String suffixB = this.text.substring(positionB);
    return suffixA.compareTo(suffixB);
}
```

The problem with this approach is that taking the substring creates a brand new string.
And if the text consists of a million characters, this will take a lot of time.
So don't try this at home!

Instead, we implement suffix comparison by iterating through the characters in the two substrings in parallel.
When two characters are unequal, we know that one suffix is smaller than the other:

```java
public int compareSuffixes(int positionA, int positionB) {
    if (positionA == positionB)
        return 0;

    int end = text.length();
    while (true) {
        if (positionA == end)
            return -1;
        if (positionB == end)
            return 1;

        char char1 = text.charAt(positionA);
        char char2 = text.charAt(positionB);
        if (char1 != char2)
            return char1 < char2 ? -1 : 1;

        positionA++;
        positionB++;
    }
}
```

Take your time to really understand how the method works!\
Try it manually on some suffix examples (e.g., from the ABRACADABRA text).

Why do we choose -1 and 1 as the return values in the inner if-statements?
What would happen if these return values are exchanged?

### Example texts

The **texts** directory has example text files for you to index.
They are generated from the British National Corpus (BNC), a large collection of English-language text collected from all kinds of spoken and written sources.

| Text file                |   Sentences |        Words |    Characters | File size |
|:-------------------------|------------:|-------------:|--------------:|----------:|
| **bnc-tinyest.txt**      |          30 |        ≈ 500 |       ≈ 2,500 |     3k    |
| **bnc-tinyer.txt**       |         100 |      ≈ 2,000 |      ≈ 10,000 |    10k    |
| **bnc-tiny.txt**         |         300 |      ≈ 5,000 |      ≈ 30,000 |    30k    |
| **bnc-smallest.txt**     |       1,000 |     ≈ 17,000 |      ≈ 90,000 |    90k    |
| **bnc-smaller.txt**      |       3,000 |     ≈ 58,000 |     ≈ 320,000 |   320k    |
| **bnc-small.txt**        |      10,000 |    ≈ 190,000 |   ≈ 1,100,000 |     1M    |
| **bnc-medium.txt.gz**    |      30,000 |    ≈ 450,000 |   ≈ 2,500,000 |     1M    |
| **bnc-large.txt.gz**     |     100,000 |  ≈ 1,500,000 |   ≈ 8,800,000 |     3M    |
| **bnc-larger.txt.gz**    |     300,000 |  ≈ 4,700,000 |  ≈ 27,000,000 |     9M    |
| **bnc-largest.txt.gz**\* |   1,000,000 | ≈ 17,000,000 | ≈ 100,000,000 |    34M    |
| **bnc-huge.txt.gz**\*    |   2,000,000 | ≈ 36,000,000 | ≈ 210,000,000 |    73M    |
| **bnc-full.txt.gz**\*    | ≈ 6,000,000 | ≈ 97,000,000 | ≈ 570,000,000 |   195M    |

The texts marked with \* are too large to fit in the git repository, but you can download them from Canvas if you want: just go to the Files section and navigate to "lab-data/text-indexing".
(Note that this is not necessary to complete the lab. And please don't add them to the repository.)

The larger files are stored compressed in [gzip format](https://en.wikipedia.org/wiki/Gzip).
But you don't have to do anything with them — the class **TextFile** can read both plain and compressed text files.

#### About the BNC corpus (you can skip this if you want)

The original BNC corpus can be downloaded from here: <http://www.natcorp.ox.ac.uk>

To create the example texts, we first converted the corpus to a UTF-8 encoded text file, with one sentence per line.
Then we converted the file to pure ASCII using the [Unidecode](https://pypi.org/project/Unidecode/) library.
The reason for this is so you won't have any encoding problems when searching for texts.

After that, we selected 2,000,000 lines (=sentences) from the BNC, starting in line 2,895,763.
This is the first sentence in Shakespeare's *The merchant of Venice*.
The merchant is followed by a scientific text about text recognition (starting in line 2,899,626), and then by several other texts.

So, the file **bnc-huge.txt.gz** starts with *The merchant of Venice*.
All the other files (except **bnc-full.txt.gz**) consist of the N first lines of that file, so all example files start with The merchant of Venice:

```
CHAPTER ONE
'No,' said Sally-Anne McAllister dazedly.
'No, please, no,' and she struggled fiercely against the arms which held her – a man's, she noted, and that was enough to start her struggling even harder.
She would not be held by a man ever again.
No, not at all, and then, even in her confused state, her mind shied away from the reasons for her distaste, and she found herself saying even through her pain and shock, 'I will not think about that, I will not,' and so saying she stopped struggling and sank back into oblivion once more .
The next time she returned to consciousness she discovered that the whole right side of her face was numb, and that was all she registered.
The memory of being held in a man's hard arms had disappeared.
Her eyes opened; she was on her back.
Above her she saw a ceiling, grey and white, a plaster rose from which depended a gas-light inside a glass globe, engraved with roses.
She heard voices which at first made little sense, could not, for the moment, think where she might be or even who she was.
...
```

### The class `TextFile`

The class `TextFile` represents the text that is to be indexed.
Its constructor takes either a text string or a path (`Path`) to a text file.
You can also use the static method `random` to generate a random text with a fixed alphabet.

Core methods:

* `size`: returns the size of the text (the number of characters).
* `getChar`: gets the character at the specified position (or the null character if the position is out of bounds).
* `compareSuffixes`: helper method for comparing suffixes, described earlier.
  (You will use this in your sorting implementations.)

### The class `Index`

The class `Index` represents the search index.
Apart from the attribute `textFile` for the text to search, it has an array of integers `suffixArray` for the suffix array.

The constructor takes a `TextFile`.
For convenience, you can also directly pass a text string.
Note that the constructor does not create the sorted suffix array, so `suffixArray` will initially be `null`.
This is the job of the suffix sorters below.

Core methods:

* `save`: Writes a search index to disk.
* `load`: Reads a built search index from disk.
  This is much faster than building it from scratch.
* `binarySearchFirst`: To be implemented in part 5.
* `check`: Check that the search index is correct.
* `print`: A helper method that prints (a portion of) the suffix array.
  Used for debugging (skip this).

### Tasks for part 2: Create a suffix array manually

Manually create suffix arrays:
- from the string "SIRAPIPARIS"
- from the string "AAAAAAAAAA"

How do the resulting arrays look like? Write down your answers in **answers.txt**.

## Parts 3, 4 and 6: Implementing some sorting algorithms

We represent each sorting algorithm as extensions of the abstract class `SuffixSorter` (defined in **core/SuffixSorter.java**).
This abstract class defines the following methods for building the search index:

* `buildIndex`: builds the sorted search index by calling `initIndex` and then `sortIndex`.
* `initIndex`: initialises the index by creating the array [0, 1, ..., N–1].
* `sortIndex`: sorts the index according to the suffixes as described earlier.
  (This method is ***abstract***. It is implemented by the different sorting algorithms below).
* `swap`: a helper method that swaps two indices in the suffix array.
  (You can use this in your sorting implementations).

There are the following implementations available:

- **sorters/BuiltinSort.java**: this is already implemented.
- **sorters/InsertionSort.java**: to be completed in part 3.
- **sorters/Quicksort.java**: to be completed in part 4.
- **sorters/MultikeyQuicksort.java**: to be completed in part 6.

There is also a helper file **sorters/PivotSelector.java**, which implements several different pivot selection strategies for (multi-key) quicksort (see part 4 below).

### The progress bar

There is a helper class `ProgressBar` (defined **utilities/ProgressBar.java**), which shows a progress bar so that you can see that the sorting is progressing.
This is heavily inspired from the Python [tqdm](https://tqdm.github.io) library, but you don't have to install anything to use the `ProgressBar`.

All uses of the progress bar are already in the skeleton code, so you shouldn't have to think about it — hopefully it works out of the box.

### Running the program

Run the program **BuildIndex.java** to create a suffix array for a given text.
You have to provide the text file and specify which algorithm you want to use.
E.g., like this:

```
$ java BuildIndex --textfile texts/bnc-medium.txt.gz --algorithm builtin
Reading 2503569 chars from 'texts/bnc-medium.txt.gz' took 0.09 seconds.
Building index took 2.41 seconds.
Checking index took 0.16 seconds.
Saving index to 'texts/bnc-medium.txt.gz.jix' took 0.14 seconds.
In total the program took 2.83 seconds.
```

If you want to know which alternatives you have, you can give the argument `--help`:

```
$ java BuildIndex --help
Usage: java BuildIndex [-h] --textfile TEXTFILE --algorithm {insertion,quicksort,multikey,builtin} [--pivot {first,middle,random,median,adaptive}]

Build an inverted search index.

Options:
  -h, --help                  show this help message and exit
  --textfile TEXTFILE, -f TEXTFILE
                              text file (utf-8 encoded)
  --algorithm {insertion,quicksort,multikey,builtin}, -a {insertion,quicksort,multikey,builtin}
                              sorting algorithm
  --pivot {first,middle,random,median,adaptive}, -p {first,middle,random,median,adaptive}
                              pivot selectors (only for quicksort algorithms)
```

## Part 3: Insertion sort

Your first sorting task is to complete the `sort` method in the class `sorters.InsertionSort`.
You should implement the in-place version of insertion sort, not allocating any additional memory.

<details>
<summary>
Spoiler 1
</summary>

To perform swaps, you can use the method `swap` of `SuffixSorter`.
To compare array values, use the method `compareSuffixes` of `TextFile`.
</details>

<details>
<summary>
Spoiler 2
</summary>

Don't use recursion.
Two nested loops will do.
</details>

<details>
<summary>
Spoiler 3
</summary>

The outer loop is already implemented with an index `i` ranging from 0 to the size of the array.
The inner loop should move the new element (at index `i`) backwards to its correct place according to the ordering.
For example, this can be done by repeatedly swapping with the element before it.
</details>

<details>
<summary>
Spoiler 4
</summary>

The course book has pseudocode for insertion sort.
</details>

### Task for part 3: Testing insertion sort

The class `InsertionSort` has a main method with some basic tests.
Feel free to add your own tests!

Now you should be able to build suffix arrays for the tiny BNC texts.
Compile and run **BuildIndex.java** with the algorithm "insertion", and the text file of your choice.
Then answer the following question in **answers.txt**:

- *How long time does it take to insertion sort the suffix array for each of the tiny files?*

If it takes less than 10 seconds to sort **bnc-tiny.txt** you should try **bnc-smallest.txt** too.
And if that goes like a charm you might have some luck with **bnc-smaller.txt** too.

## Part 4: Quicksort

Your second sorting task is to complete the implementation of quicksort in **sorters/Quicksort.java**.
To help you structure your code, we have created a skeleton with two methods:

```java
public void quicksort(int start, int end)
public int partition(int start, int end)
```

Both methods are already started with some initialisations, but you have to finish them both.

You can choose which partitioning scheme to use in `partition`.
We recommend the Hoare scheme taught in the course (see the course material and Spoiler 2 below).

Note the call to `pivotIndex` in `partition`.
The class `Quicksort` uses a *pivot selection strategy* (see the interface `PivotSelector`) that can be specified by the method `setPivotSelector`.

We have already implemented a variety of pivot selection strategies — see **sorters/PivotSelector.java** for more information.
* `TAKE_FIRST`: always pick the first element as pivot,
* `TAKE_MIDDLE`: always pick the middle element as pivot (this is the default),
* `TAKE_RANDOM`: pick a random pivot,
* `TAKE_MEDIAN_OF_THREE`: take the median of the first, middle, and last element,
* `ADAPTIVE`: adaptive strategy that takes the size of the range into account.

The same reminders as for insertion sort apply:
* To perform swaps, you can use the method `swap` of `SuffixSorter`.
* To compare array values, use the method `compareSuffixes` of `SuffixArray`.

Plus an important note:
* The first index in the given range is `start` and the last index is `end - 1`.
  This is different from old version of the course book, where quicksort and partition uses inclusive intervals.

<details>
<summary>
Spoiler 1
</summary>

Use recursion to sort the left and right parts of the partition in `quicksort`.
</details>

<details>
<summary>
Spoiler 2
</summary>

The partition scheme of the course book works as follows.
- First swap the pivot with the first element.
- Initialize `lo = from + 1` and `hi = to - 1`.
- Advance `lo` forward and `hi` backward while their elements are in the correct position.
- Once we reach a conflict on both sides, we swap and advance.
- Eventually, `lo` and `hi` cross.
- Finally, where should the pivot go?
</details>

<details>
<summary>
Spoiler 3
</summary>

The course book has pseudocode for quicksort.
</details>

### Task for part 4: Testing quicksort

Just like `InsertionSort`, the class `Quicksort` has a main method with some basic tests.
Feel free to add your own tests!

Now you should be able to build suffix arrays for medium-to-large BNC texts.
Run **BuildIndex.java** with the algorithm "quicksort", and the text file of your choice.
For quicksort, you can also specify a pivot selector (one of "first", "middle", "random", "median", "adaptive").
If you don't, then "middle" will be used by default.

You can e.g. start with the file **bnc-smallest.txt**, and if that goes smoothly continue with larger and larger files.
You can stop when building the index takes longer than 20–30 seconds.
Finally you can answer the following question in **answers.txt**:

- *How long does it take to quicksort the suffix array for each of the three largest BNC files that you tried?*

## Part 5: Searching using the suffix array

Here, you will implement the following function in the file **core/Index.java**:

```java
public int binarySearchFirst(String value)
```

This function finds the *first* occurrence of `value` in the suffix array.
That is, not the first occurrence in the text by position, but the alphabetically smallest suffix that starts with `value`.

You have already implemented a similar method in the binary search lab.
Choose of which of your implementations you like best and copy it over, modifying the comparisons to the new setting.

Once we have the above method, it's an easy task to iterate through all occurrences of `value`, as explained in the explanation about suffix arrays above.
That part is already implemented as the function `binarySearch` in the file **SearchIndex.java**.
Can you follow what it does?

```java
/*
 * Generate all positions in the text file matching the given value.
 * This is done by binary search in the suffix array index.
 */
public static Iterator<Integer> binarySearch(Index index, String value) {
    if (index.suffixArray == null)
        throw new AssertionError("Index is not initialised!");

    int first = index.binarySearchFirst(value);
    if (first == -1)
        return Collections.emptyIterator();

    return new Iterator<>() {
        int i = first;

        @Override
        public boolean hasNext() {
            if (!(i < index.suffixArray.length))
                return false;
            int start = index.suffixArray[i];
            int end = start + value.length();
            return value.equals(index.textFile.text.substring(start, end));
        }

        @Override
        public Integer next() {
            if (!hasNext())
                throw new NoSuchElementException();

            return index.suffixArray[i++];
        }
    };
}
```

### Testing your binary search implementation

The main class **core.Index** has some basic tests for **binarySearchFirst**.
Feel free to add your own tests!

Finally you can reap the fruits of your labor.
Run the file **SearchIndex.java** with a text file for which you have previously built an index.

```
$ java SearchIndex --textfile texts/bnc-medium.txt.gz
Reading 2503569 chars from 'texts/bnc-medium.txt.gz' took 0.11 seconds.
Loading the index took 0.06 seconds.
Search key (ENTER to quit): 
```

If everything went well, you should now have a blazingly fast text search interface to the corpus you selected.
If it takes longer than a hundredth of a second, you probably have some bug in your code.

Here are some concrete test cases for **bnc-medium.txt.gz**.

There should be exactly 7 matches for the string "University" (note the capital U):
```
Search key (ENTER to quit): University
Searching for 'University':
  746607:   quality of Rembrandts, and the private |University| College at Buckingham certifies educati
  563816:  l British dictionary publishers (Oxford |University| Press, Longmans and Chambers) are curre
 1916509:   he?' 'He is the Vice Chancellor of the |University| of Bridport,' said Ellen. There was a s
 1955291:   in 1991, written by Gene Lerner of the |University| of California at Santa Barbara, on what
  763008:  y Professor David Newberry of Cambridge |University| of the efficient way to achieve a 30 pe
  936252:  rent countries, George Yarrow of Oxford |University| reached the following conclusion. Priva
 1959662:   paper by Deborah Tannen, of Georgetown |University|, whose study of repetition in conversat
Finding 7 matches took 0.00 seconds.
```

You can also search for multiple-word strings:
```
Search key (ENTER to quit): speech recognition system
Searching for 'speech recognition system':
  458415:  uction of a large vocabulary continuous |speech recognition system| in 1972. The group pioneered the use of
  421823:  baiks/). Therefore training a connected |speech recognition system| with isolated words may not be satisfac
  423996:  ognition of speech. Currently available |speech recognition system|s impose a selection of constraints on t
  424198:  r these constraints do not apply to all |speech recognition system|s): Limited vocabulary The vocabulary si
Finding 4 matches took 0.00 seconds.
```

Or for parts of words:
```
Search key (ENTER to quit): abra
Searching for 'abra':
 1225241:  ly saw its waxy flowers bold as a candel|abra|. A horse chestnut. The trunk was studde
 1335083:  nd she lit a seven branched olive candel|abra|. There was a silver goblet in the centr
 1841942:  k and white mixture between collie and l|abra|dor, trotted happily towards me. It look
 1414916:  ared publication, dealing in hard fact, |abra|sive as Maggie herself was abrasive. She
 1414947:  rd fact, abrasive as Maggie herself was |abra|sive. She had been a feature writer here
Finding 5 matches took 0.00 seconds.
```

Wonderful, isn't it?
Note how fast the query is!
You could do almost a thousand queries per second.
And most of the query time is probably just used for printing the results.

### Questions for part 5

In the example searches above, the first number that is printed on each line is the position of each search result.
As you can see these numbers are not ordered, but they seem to be random.

- *Why do you think the results are not shown in increasing order of position?*

Now, search for a non-existing string (e.g., "this-does-not-exist") in the largest text file for which you have built a suffix array.
(This should be at least **bnc-large.txt.gz**.)

Do this search in two ways — one using naive *linear search* (using the command line option `-l`), and one using your own *binary search*.

- *How long time does it take to search for a non-existing string using linear search, and using binary search, respectively?*
  (Don't include the time it takes to read the text and the index)

Write your answers to these questions in **answers.txt**.

## Part 6: Multi-key quicksort

Using quicksort to build the search index is already quite fast, but we are going to make it even faster.
You will implement a version of quicksort that is particularly fast at sorting a list of sequences *in lexicographic order*.
And a suffix array is exactly that — a list of sequences — so this new quicksort variant should be a perfect match for us.

In the end your implementation should be around twice as fast as your quicksort from part 3.
If you don't see a drastic improvement you probably have a bug somewhere.

### Lexicographic order

Suppose we have a type T with an ordering (e.g., numbers or characters).
This induces an ordering on the sequences of type T: given sequences x and y, we just look for the first difference between them.
For example, if x is [4, 1, 7, 6] and y is [4, 1, 6, 8], then x > y because the first position where the sequences differ is index 2, and x[2] = 7 is bigger than y[2] = 6.
We call this position within the lists x and y, an *offset*.

In this lab we sort strings, but strings are really just sequences of characters.
When comparing two strings we can find the first offset where the characters differ and compare that character.
This is how all programming languages implement string comparison, and it is how the method `compareSuffixes` is implemented (see the explanation about suffix arrays above).

### Multi-key quicksort

Multi-key quicksort is a version of quicksort optimized for types with a lexicographic ordering.
You will implement it in **sorters/MultikeyQuicksort.java**.

There are two main differences over plain quicksort:

* We never compare strings fully.
  Instead, we only ever compare them at some offset.
  Initially, we compare strings only by their first character.
  But as the algorithm goes on, we will start comparing strings also by their second character offset, third offset, and so on.

* Instead of partitioning into a left part and a right part, we partition into *three parts*.
  The middle part will contain all the elements that compare equal to the pivot (for the chosen offset).

```java
public void multikeyQuicksort(int start, int end, int offset)
public IndexPair partition(int start, int end, int offset)
```

Since `partition` divides the array into three parts, it has to return two indices.
For this, we have a helper class `IndexPair`.

### Partitioning

The pivot selection step works the same as in quicksort.
But now we won't use the whole suffix string as the pivot — instead we will use a single *pivot character* from the given offset of the pivot string.

Initially, we partition the array by comparing only the first characters of the strings (offset 0).
This gives us three parts:
* the left part has elements where the first character is smaller than the pivot character,
* the middle part has elements where the first character is the same as the pivot character,
* the right part has elements where the first character is larger than the pivot character.

In the general case, `partition` is given the offset in addition to the start and end indices.
All suffixes in the given range are guaranteed to start with the same prefix of length `offset`.
We use that offset to find the pivot character and compare it to the respective characters from the suffixes in the range.

Now, how do we actually partition into three parts?
Just as for normal quicksort, this should be in-place — i.e., not use a helper array.
Feel free to look at some of the below spoilers if you are out of ideas.

<details>
<summary>
Spoiler 1
</summary>

Start as usual by swapping the pivot with the first element of the range.
</details>

<details>
<summary>
Spoiler 2
</summary>

Let's call the current range `start` and `end`.
Eventually, we want a range `middleStart` and `middleEnd` for the middle part of the partition.
This should include the pivot.
The range from `start` and to `middleStart` will be the left part and the range from `middleEnd` to `end` will be the right part.
</details>

<details>
<summary>
Spoiler 3
</summary>

Initialize `middleStart = start + 1` and `middleEnd = end`.
You need to traverse all the elements from `middleStart` and `middleEnd`, compare each to the pivot in the position under consideration, and depending on the result swap it into its part of the partition.
You will need to update some of the variables such as `middleStart` and `middleEnd` to account for changes in the partition sizes.
</details>

<details>
<summary>
Spoiler 4
</summary>

Say we use `i` as the index to start at `middleStart + 1`.
We process the element at `i` until we reach `i == middleEnd`.
Inside the loop, the ranges have to following meaning:
* from `start` to `middleStart`: the left part so far
* from `middleStart` to `i`: the middle part so far
* from `i` to `middleEnd`: still to be processed
* from `middleEnd` to `end`: the right part so far
</details>

<details>
<summary>
Spoiler 5
</summary>

Suppose we process the element at `i`.
Let `chr` be the character at offset `offset` of the suffix starting at `index.suffixArray[i]`
What we should do depends on `chr`:
* If `chr < pivotChar`, the element belongs in the left part.
* If `chr == pivotChar`, the element belongs to the middle part.
* If `chr > pivotChar`, the element belongs to the right part.

In each case, we may use a swap to update the three parts of the partition so far with the new element.
How do the variables `middleStart`, `middleEnd`, `i` change?
</details>

### Quicksorting

Just like in quicksort, we handle the left and right part recursively, calling the same method with a smaller range.
But for the middle part, we can do something more efficient.
Since we already know that all elements in the middle part have the same first character (offset = 0), we can move on to comparing their *second characters* (offset = 1).

To be able to recurse also in this case, we need to add a *comparison offset* parameter (called `offset` in `MultikeyQuicksort`) to the sorting method.

<details>
<summary>
Spoiler 1
</summary>

The general pattern is:
* For the left and right part, we keep the same comparison offset in the recursive call.
* For the middle part, we increase the comparison offset by one.
</details>

### Task for part 6: Testing your implementation

Just like `Quicksort`, the main class `sorters.MultikeyQuicksort` has some basic tests.
Feel free to add your own tests!

Now you should be able to build suffix arrays for medium-to-large BNC texts.
Run **BuildIndex.java** with the algorithm "multikey", and the text file of your choice.
Remember that you can also specify a pivot selector (as for quicksort).

Comparing with normal Quicksort, your Multikey implementation should be around twice as fast.
If you don't see an improvement, please talk to a TA.
(It should even be faster than using Java's builtin sorting algorithm — you can try this by giving "builtin" as the algorithm.)

Answer the following question in **answers.txt**:

- *How long time does it take for Quicksort and Multikey Quicksort, respectively, to sort the suffix array for the largest BNC files that you tried?*

## Part 7: Empirical complexity analysis

In this final part, you will do some empirical experiments to see if the actual asymptotic runtime complexity (linear, linearithmic, quadratic, cubic, etc.) of the algorithms are as predicted.

The sorting classes (**InsertionSort**, **Quicksort** and **MultikeyQuicksort**) all have main functions where you can write tests to run.

To be able to test the complexity of your implementations empirically, you have to experiment with different text sizes.
The static method `random` in the class **TextFile** lets you create a random text of a specified size, which you can use for this purpose.
You can e.g. modify the code commented as "example performance tests" however you like (in the main function of the classes **InsertionSort**, **Quicksort** and **MultikeyQuicksort**).

- *Deduce the empirical asymptotic complexity of each of the sorting implementations.*

Run at least 10 experiments with different text sizes and note the time it takes to sort the suffix array for each size.
Then try to deduce what asymptotic runtime complexity your implementation has.
Do this for **InsertionSort**, **Quicksort**, and **MultikeyQuicksort**.

<details>
<summary>
Spoiler 1
</summary>

You can use a curve fitting tool.
It can e.g. be the `scipy` package in Python, or some online tool such as <http://curve.fit/>, or even Excel or Google Spreadsheet.
</details>

<details>
<summary>
Spoiler 2
</summary>

Try to fit the data to a linear curve, to a quadratic curve, and perhaps to a linearithmic curve (i.e., `y = A + B * x * log(x)`), and measure which has the least error.

Note that Excel and Google Spreadsheets cannot fit a linearithmic curve, only logarithmic, linear, polynomial and exponential curves.
</details>

<details>
<summary>
Spoiler 3
</summary>

It will probably be extremely difficult to see a difference between linear complexity (*O*(*n*)) and linearithmic (*O*(*n* log(*n*))).
So don't be alarmed if the curve fitter gets confused between them.
</details>

Did your experimental results match with your expectations?

- *Vary the size of the alphabet.*

It's enough if you run this experiment on only one sorting algorithm, so let's use quicksort.
The experiment code that you used above used an alphabet "ABCD".
Generate a random text of some size and sort it.
Now vary the size of the alphabet and run the same experiment (with the same text sizes).
How does the sorting time vary when you vary the alphabet size? Try to come up with an explanation of the differences.

**Note**:
You will notice that a one-letter alphabet is a special case!
Why do you think this so much different from other alphabets?

<details>
<summary>
Spoiler
</summary>

It's probably enough to experiment with only two larger alphabet sizes and two smaller.
</details>

## Submission

Double check:
* Have you answered the questions in **answers.txt**?
  - don't forget the ones in the appendix
  - and don't forget to specify your programming language
* Have you tested your code with **Robograder**?

Read in Canvas how to submit your lab.

## Optional tasks

* When you run multi-key quicksort on **bnc-large.txt.gz** or larger, you may experience a stack overflow error.
  Which code path do you think a high level of nested calls comes from?
  How can you fix this?

* Run more experiments with random texts on different-size alphabets.
  Can you deduce a runtime complexity in terms of the two variables N (the size of the text) and V (the size of the alphabet)?

* When you run multi-key quicksort on a list that contains duplicates, your code will run forever or cause a stack overflow error.
  Fortunately, this can never happen for a suffix array (why?).
  How can you fix your implementation so that it also works with duplicates?

## Literature

- Wikipedia explains [suffix arrays](https://en.wikipedia.org/wiki/Suffix_array).

- Sedgewick & Wayne (2011) has a full chapter on string algorithms (chapter 5), including text searching.
  They even have a section about suffix arrays (in chapter 6 "Context").

- Wikipedia explains [multi-key quicksort](https://en.wikipedia.org/wiki/Multi-key_quicksort).
  But it gives away the pseudocode — try to first build your own version.

- [Bentley & Sedgewick (1994)](http://akira.ruc.dk/~keld/teaching/algoritmedesign_f04/Artikler/04/Bentley99.pdf) is the main research article about multi-key quicksort.
  Among other things is shows that multi-key quicksort it is isomorphic to ternary search trees, in the same way as quicksort is isomorphic to tries.

- There are plenty of research on how to make even faster suffix sorting algorithms, and [Larsson & Sadakane (2007)](https://blogs.asarkar.com/assets/docs/algorithms-curated/Faster%20Suffix%20Sorting%20-%20Larsson+Sadakane.pdf) present one of them.
