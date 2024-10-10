# Lab: Path finder

In this lab, you will write a program to find the shortest path between two nodes in a graph.
You will try this on several kinds of graphs.

### Important notes

* This lab is part of the examination of the course.
  Therefore, you must not copy code from or show code to other students.
  You are welcome to discuss general ideas with one another, but anything you do must be **your own work**.

* You can solve this lab in either Java or Python, and it's totally up to you.
  - Since Python is an interpreted language, it's slower than Java — about 3–4 times slower on the tasks in this lab.
  - You grading will not be affected by your choice of language!

* This is the **Java version** of the lab.
  To switch to the Python version, [see here](https://chalmers.instructure.com/courses/27979/pages/the-lab-system#switching-language).

### Overview of the lab

The lab repository consist of a number of Java files (explained below), a directory of **graphs**, and a file **answers.txt** where you will answer questions for the lab.

Here is a very general overview of this document and what you have to do to complete the lab:

* The [background](#background) contains descriptions of the source files, the classes, and the four different graph types.
* In [part 1](#part-1-uniform-cost-search), you will implement the UCS (uniform cost search) shortest-path algorithm.
* In [part 2](#part-2-word-ladders), you will complete the implementation of "word ladder" graphs.
* In [part 3](#part-3-the-a-algorithm), you will implement the A* algorithm, which uses a heuristic to guide the search.
* In [part 4](#part-4-guessing-the-cost), you will implement A* heuristics for word ladders and grid graphs.
* Finally, there are some [optional tasks](#optional-tasks) that you can play around with if you think this was a fun lab!


## Background

Your task is to write a program solving one of the most important graph problems: calculating the shortest path between two nodes.
This is similar to what map applications do (e.g. Waze, OpenStreetMap, Google, Apple, Garmin, etc.), although your solution will not quite be able to handle as large road maps as them.
But this is also useful in several other circumstances, and you will see some in this laboration.

There is a main program **PathFinder.java** which you can compile and run right away.
It takes three required arguments:

- the algorithm ("random", "ucs", or "astar"),
- the type of graph to read ("AdjacencyGraph", "SlidingPuzzle", "GridGraph", or "WordLadder"),
- the graph itself (usually a file name into the "graphs" subfolder, but for the sliding puzzle it's the dimensions of the puzzle).

You can specify these arguments directly on the command line, like this:

```
java PathFinder -a random -t AdjacencyGraph -g graphs/AdjacencyGraph/citygraph-SE.txt
```

You can also just run the program, and it will let you enter the arguments (just like the previous labs).

The program first prints some information about the specified graph:

```
Weighted adjacency graph with 729 nodes and 5454 edges.

Random nodes with outgoing edges:
  Stavsnäs ---> Djurö [9], Mörtnäs [17]
  Västervik ---> Ankarsrum [24], Gamleby [24], Gunnebo [13], Kristdala [62], Oskarshamn [69]
  Åkersberga ---> Bålsta [55], Kungsängen [39], Norrtälje [50], Resarö [21], Rimbo [37], Rosersberg [35], Sollentuna [25], Täby [19], Upplands Väsby [27], Vallentuna [18], Vaxholm [22]
  [...]
```

It then allows you to (repeatedly) perform path searches by specifying start and goal:

```
Start: Stockholm
Goal: Kiruna

Searching for a path from Stockholm to Kiruna...
Searching the graph took 0.01 seconds.
Loop iteration count: 5285
Cost of path from Stockholm to Kiruna: 232417
Number of edges: 5284
Stockholm --[16]-> Huddinge --[7]-> Tullinge --[7]-> Huddinge --[16]-> Stockholm --[6]-> ... --[190]-> Töre --[25]-> Rolfs --[25]-> Töre --[100]-> Övertorneå --[288]-> Kiruna
```

The query loop stops if you leave the start node blank or close the input stream:

```
Start:
Bye bye, hope to see you again soon!
```

Alternatively, you can directly specify start and goal nodes as additional program arguments.
You do that by adding `-q Stockholm Kiruna` to the command line above.
In that case, the program directly prints the path found.

For long paths as above, only the beginning and end is printed.
If you want to see the full path, set the constant `showFullPaths` in **PathFinder.java** to `true`.

Note that a random walk will typically not find the shortest path.
In fact, it will usually find a different path every time it runs.
It may even run forever (or it would, if we didn't terminate the random walk after visiting 10,000 nodes).

```
Searching: Stockholm --> Kiruna
Searching the graph took 0.01 seconds.
Loop iteration count: 10000
No path from Stockholm to Kiruna found.
```

### The source files

The path finder program consists of several source files, organized in folders.

Top-level programs:

* **PathFinder.java**: the main program.
  Loads the selected graph and calls the selected search algorithm.
  Only random search is currently implemented.

Interfaces for and classes for different kinds of *graphs* in **graphtypes**:

* **Graph.java**: an interface for weighted directed graphs.
  It is generic in the type `V` of nodes.
  It specifies the main methods that a graph type must implement.
  We have four implementations:
  - **AdjacencyGraph.java**: the "standard" implementation storing a graph as a mapping from nodes to lists of outgoing edges.
  - **GridGraph.java**: nodes are "pixels" in a two-dimensional grid and edges connect neighbouring pixels (*to be completed in part 4*.)
  - **SlidingPuzzle.java**: an implementation of the 15-puzzle (4x4-puzzle), but for arbitrary dimensions.
  - **WordLadder.java**: nodes are words and the edges connect similar words (*to be completed in parts 2 and 4*).
* **Edge.java**: a class for weighted directed edges in a graph.
* **Point.java**: a class for two-dimensional points or coordinates.
  It is used by `GridGraph` and `SlidingPuzzle`.

Interfaces and implementations for *search algorithms* in **searchers**:

* **Query.java**:
  This is the base class for all search algorithms.
  It stores the graph and the start and goal node.
  It exposes a method `search()` for running the search.
  There are three implementations:
  - **Random.java**: an extremely stupid algorithm.
     It just moves around randomly in the graph until it (hopefully) reaches the goal.
  - **UCS.java**: UCS (uniform cost search), also known as Dijkstra's algorithm (*to be developed in part 1*).
  - **Astar.java**: the A* algorithm, a generalization of UCS which uses heuristics to guide the search (*to be developed in part 3*).

Some general **utilities** that you can ignore:

- **CommandParser.java**: command-line argument parser, heavily inspired by Python's builtin [argparse](https://docs.python.org/3/library/argparse.html) module.
- **Stopwatch.java**: very simple class for measuring runtime.

### The search classes

The seacher classes **Random**, **UCS** and **Astar** do the heavy work of path finding.
The context for each search is inherited from **Query**:

```java
    public final Graph<V> graph;
    public final V start, goal;
```

Each searcher has a method `search()` that runs the actual search and returns the result.

```java
    /**
     * Searches for a path in `graph` from `start` to `goal`.
     * Returns the search result (which includes the path found if successful).
     */
    public abstract Result search()
```

The parent class provides methods to construct a search result:

```java
    /**
     * Construct a failure result (no path found).
     */
    public Result failure(int iterations)

    /**
     * Construct a success result (path found).
     */
    public Result success(double cost, List<Edge<V>> path, int iterations)
```

UCS and A* use a priority queue, and the elements stored in the priority queue are objects of the classes **UCSEntry** and **AstarEntry** respectively.
In addition, the class **UCS** defines the method `extractPath`, which is used by both UCS and A*.

* `UCS.search`: this is Part 1a+c
* `UCS.extractPath`: this is Part 1b
* `Astar.search` and `Astar.AstarEntry`: this is Part 3

### Edges and graphs

The type `Edge<V>` represents weighted directed edges.
Each edge has nodes `start` and `end` of type `V` and a non-negative `weight`.
The weight defaults to 1 if it is not specified (such as for **WordLadder** and **SlidingPuzzle**).

The type `Graph<V>` represents weighted directed graphs.
It defines the following two important methods:

```java
interface Graph<V> {
    List<Edge<V>> outgoingEdges(V v);
    double guessCost(V v, V w);
}
```

Note that this interface differs from the graph interface in the course API (it lacks several of the API methods).
But it is enough for the purposes in this lab.

The interface also contains a number of auxiliary methods used for parsing and printing.
You don't have to look at them.

### AdjacencyGraph

The **AdjacencyGraph** reads a generic finite graph, one edge per line, and stores it as an adjacency list as described in the course book and the lectures.
The graph can represent anything.
In the graph repository, there are distance graphs for cities (city graphs) in several regions, including EU, Sweden (SE) and Västra Götaland (VGregion).
There is also a link graph between more than 4500 Wikipedia pages, "wikipedia-graph.txt":

```
$ java PathFinder -a random -t AdjacencyGraph -g graphs/AdjacencyGraph/wikipedia-graph.txt -q Sweden Norway
Searching for a path from Sweden to Norway...
Searching the graph took 0.00 seconds.
Loop iteration count: 82
Cost of path from Sweden to Norway: 81
Number of edges: 81
Sweden -> Lithuania -> Climate -> Global_warming -> Nuclear_power ->  .....  -> 11th_century -> Viking -> British_Isles -> Irish_people -> Norway
```

Graph files in **graphs/AdjacencyGraph**:

* All graphs **citygraph-XX.txt** are extracted from freely available [mileage charts](https://www.mileage-charts.com/).
  The smallest graph has 130 cities and 838 edges (citygraph-VGregion.txt).
  The largest one 996 cities and 28054 edges (citygraph-EU.txt).
  All edge costs are in kilometers.
  - Suggested searches: `Göteborg` to `Götene` (**citygraph-VGregion.txt**); `Lund` to `Kiruna` (**citygraph-SE.txt**); `Porto, Portugal` to `Vorkuta, Russia` (**citygraph-EU.txt**)

* **wikipedia-graph.txt** is converted from [the Wikispeedia dataset](http://snap.stanford.edu/data/wikispeedia.html) in SNAP (Stanford Large Network Dataset Collection).
  It contains 4587 Wikipedia pages and 119882 page links.
  All edges have cost 1.
  - Suggested search: `Superconductivity` to `Anna_Karenina`

**Note:**
All graph files are encoded in UTF-8 (Unicode).
If you experience problems searching for words with special characters (`å`, `ä`, `ö`), your setup may have a character encoding problem.
Try switching to an English or Swedish system locale.

### SlidingPuzzle

**SlidingPuzzle** is generalization of the [15-puzzle](https://en.wikipedia.org/wiki/15_puzzle).
The nodes for this graph are the possible states of the puzzle.
An edge represents a move in the game, swapping the empty tile with an adjacent tile.

We represent each state as a string encoding of an *N* x *M* matrix.
The tiles are represented by characters starting from the letter A (`A`…`H` for *N*=*M*=3, and `A`…`O` for *N*=*M*=4).
The empty tile is represented by `_`.
To make it more readable for humans, every row is separated by `/`:

```
$ java PathFinder -a random -t SlidingPuzzle -g 2x2 -q /_C/BA/ /AB/C_/
Searching for a path from /_C/BA/ to /AB/C_/...
Searching the graph took 0.02 seconds.
Loop iteration count: 41
Cost of path from /_C/BA/ to /AB/C_/: 40
Number of edges: 40
/_C/BA/ -> /C_/BA/ -> /_C/BA/ -> /BC/_A/ -> /_C/BA/ ->  .....  -> /_B/AC/ -> /AB/_C/ -> /_B/AC/ -> /AB/_C/ -> /AB/C_/

$ java PathFinder -a random -t SlidingPuzzle -g 3x3 -q /ABC/DEF/HG_/ /ABC/DEF/GH_/
Searching for a path from /ABC/DEF/HG_/ to /ABC/DEF/GH_/...
Searching the graph took 0.04 seconds.
Loop iteration count: 10000
No path from /ABC/DEF/HG_/ to /ABC/DEF/GH_/ found.
```

Note that **SlidingPuzzle** does not need a file for initializing the graph.
Instead, we give it the dimensions of the puzzle.

It's no use trying the "random" algorithm on **SlidingPuzzle** with dimensions larger than 2: it will almost certainly never find a solution.
In fact, already for *N* = *M* = 4, the number of states is 16! ≈ 2 · 10<sup>13</sup>.
Thus, we cannot even store the set of nodes in memory.

Dimensions larger than 3x4 are usually too difficult even for the other algorithms in this lab.

Suggested searches:
* Dimensions 2x2: `/_C/BA/` to goal `/AB/C_/`
* Dimensions 3x3: any of `/_AB/CDE/FGH/`, `/CBA/DEF/_HG/`, `/FDG/HE_/CBA/` or `/HFG/BED/C_A/`, to the goal `/ABC/DEF/GH_/`
* The following 3x3-puzzle doesn't have a solution (why?): `/ABC/DEF/HG_/` to `/ABC/DEF/GH_/`

#### GridGraph

**GridGraph** is a 2D-map encoded as a bitmap, or an *N* x *M* matrix of characters.
Some characters are passable, others denote obstacles.
A node is a point in the bitmap, consisting of an x- and a y-coordinate.
This is defined by the helper class **Point**.
You can move from each point to the eight points around it.
The edge costs are 1 (for up/down/left/right) and sqrt(2) (for diagonal movement).
A point is written as `x:y`, like this:

```
$ java PathFinder -a random -t GridGraph -g graphs/GridGraph/maze-10x5.txt
Grid graph of dimensions 41 x 11.

+---+---+---+---+---+---+---+---+---+---+
        |   |                       |   |
+---+   +   +   +---+   +---+---+   +   +
|   |   |           |   |   |           |
+   +   +---+---+---+   +   +   +---+---+
|               |       |   |           |
+---+   +---+---+   +---+   +---+   +   +
|       |                   |       |   |
+   +---+   +---+---+---+---+   +---+   +
|           |                   |
+---+---+---+---+---+---+---+---+---+---+

Random example nodes with outgoing edges:
* 12:3 11:2 [1.41], 11:3 [1], 13:2 [1.41], 13:3 [1]
* 30:1 29:1 [1], 31:1 [1]
* 5:2 4:1 [1.41], 5:1 [1], 5:3 [1], 6:1 [1.41], 6:2 [1], 6:3 [1.41]
* 27:1 26:1 [1], 28:1 [1]
* 25:4 25:3 [1], 25:5 [1], 26:3 [1.41], 26:4 [1], 26:5 [1.41]
* 31:7 30:7 [1], 30:8 [1.41], 31:8 [1], 32:7 [1]
* 7:4 6:3 [1.41], 6:4 [1], 6:5 [1.41], 7:3 [1], 7:5 [1], 8:5 [1.41]
* 5:5 4:5 [1], 5:4 [1], 5:6 [1], 6:4 [1.41], 6:5 [1], 6:6 [1.41]

Start: 1:1
Goal: 39:9

Searching: 1:1 --> 39:9
Searching the graph took 0.01 seconds.
Loop iteration count: 10000
No path from 1:1 to 39:9 found.

Start: 1:1
Goal: 39:9

Searching for a path from 1:1 to 39:9...
Searching the graph took 0.02 seconds.
Loop iteration count: 2546
Cost of path from 1:1 to 39:9: 2959.63
Number of edges: 2545
1:1 --[1]-> 2:1 --[1]-> 1:1 --[1]-> 0:1 --[1]-> 1:1 --[1]-> ..... --[1]-> 37:7 --[1.41]-> 38:6 --[1.41]-> 39:7 --[1.41]-> 38:8 --[1.41]-> 39:9

+---+---+---+---+---+---+---+---+---+---+
*S******|   |      *****************|***|
+---+***+   +   +---+***+---+---+***+***+
|***|***|           |***|   |***********|
+***+***+---+---+---+***+   +***+---+---+
|********       |*******|   |***********|
+---+***+---+---+***+---+   +---+***+***+
|*******|************       |* **** |***|
+***+---+***+---+---+---+---+***+---+** +
|***********|               ****|   * *G
+---+---+---+---+---+---+---+---+---+---+
```

The asterisks (`*`) in the final graph make up the path.
A lot of the available space is filled since the algorithm moves around randomly until it finds its way out.

As you can see in the example, often the random search won't find a path even for the smallest maze.

Graph files in **graphs/GridGraph**:

- **AR0011SR.txt** and **AR0012SR.txt** are taken from the [2D Pathfinding Benchmarks](https://www.movingai.com/benchmarks/grids.html) in Nathan Sturtevant's Moving AI Lab.
  The maps are from the collection "Baldurs Gate II Original maps", and are grids of sizes 216 x 224 and 148 x 139, respectively.
  There are also associated PNG files, so that you can see how they look like.
  * Suggested searches: `23:161` to `130:211` (**AR0011SR.txt**); `11:73` to `85:127` (**AR0012SR.txt**)

- **maze-10x5.txt**, **maze-20x10.txt**, and **maze-100x50.txt** are generated by a [random maze generator](http://www.delorie.com/game-room/mazes/genmaze.cgi).
  They are grids of sizes 41 x 11, 81 x 21, and 201 x 101, respectively.
  * Suggested searches: `1:1` to `39:9` (**maze-10x5.txt**); `1:1` to `79:19` (maze-20x10.txt); `1:1` to `199:99` (**maze-100x50.txt**)

#### WordLadder

This class is unfinished.
You will complete it in Part 2.

> Word ladder is a word game invented by Lewis Carroll.
> A word ladder puzzle begins with two words, and to solve the puzzle one must find a chain of other words to link the two, in which two adjacent words (that is, words in successive steps) differ by one letter.
>
> ([Wikipedia](https://en.wikipedia.org/wiki/Word_ladder))

We model this problem as a graph.
The nodes denote words in a dictionary and the edges denote one step in this word ladder.
Note that edges only connect words of the same length.

The class does not store the full graph in memory, just the dictionary of words.
The edges are then computed on demand.
The class already contains code that reads the dictionary, but you must complete the rest of the class.

Graph files in **graphs/WordLadder**:

* **swedish-romaner.txt** and **swedish-saldo.txt** are two Swedish word lists compiled from [Språkbanken Text](https://spraakbanken.gu.se/resurser).
  They contain 75,740 words (**swedish-romaner.txt**) and 888,275 words (**swedish-saldo.txt**), respectively.
  - Suggested searches (after you have completed Part 2 below): `eller` to `glada` (**swedish-romaner.txt**); `njuta` to `övrig` (**swedish-saldo.txt**)
  - Another interesting combination is to try any combination of the following words: `ämnet`, `åmade`, `örter`, `öring` (**swedish-romaner.txt**)

* **english-crossword.txt** comes from the official crossword lists in the [Moby project](https://en.wikipedia.org/wiki/Moby_Project).
  It consists of 117,969 words.
  - Suggested searches: any start and goal of the same length (between 4 and 8 characters)


## Part 1: Uniform-cost search

There is a skeleton method `search` in **searchers/UCS.java**.
Your goal in Part 1 is to implement uniform-cost search (UCS).
This is a variant of Dijkstra's algorithm which can handle infinite and very large graphs.
It is also arguably easier to understand than the usual formulation of Dijkstra's.

### Part 1a: The simple UCS algorithm

The main data structure used in UCS is a priority queue.
It contains graph nodes paired with the cost to reach them.
We store this information in the class `UCSEntry` (which is already implemented):

```java
public class UCS<V> {
    public class UCSEntry {
        public final V node;
        public final Edge<V> lastEdge;
        public final UCSEntry backPointer;
        public final double costToHere;
    }
}
```

The `backPointer` is necessary for recreating the final path from the start node to the goal.
More about this in Part 1b below.
The very first entry will not have any previous entry, so we set `lastEdge` and `backPointer` to `null`.

We use the implementation `PriorityQueue` of a min-priority queue from the Java API.
When we initialise it, we specify a **Comparator** that tells it how to compare entries.
Note that `removeMin` is just called `remove`.

Here is pseudocode of the simplest version of UCS:

```
search (in graph from start to goal):
    create a min-priority queue for entries based on their cost
    add an initial entry to the priority queue
    while there is an entry to remove from the priority queue:
        if the node of the entry is the goal:
            SUCCESS:) extract the path and return it
        for every edge starting at the entry's node:
            add a new entry for the target of the edge to the priority queue
            with cost that of the current entry plus the edge weight
    FAILURE:( there is no path
```

It is important that we return as soon as we reach the goal.
Otherwise, we will continue adding new entries to the queue indefinitely.

Implement this algorithm in the method `UCS.search`.
It should return a **Result** object as described [previously](the-search-classes).
When you return a sucessful result, use `null` for the `path` argument for now.
You should increase the counter `iterations` every time you remove an entry from the priority queue.

When you have done this, you should be able to run queries for nodes not too far apart:

```
$ java PathFinder -a ucs -t AdjacencyGraph -g graphs/AdjacencyGraph/citygraph-VGregion.txt -q Skara Lerum
Searching for a path from Skara to Lerum...
Searching the graph took 0.12 seconds.
Loop iteration count: 66240
Cost of path from Skara to Lerum: 115
WARNING: The path is null. Remember to implement extractPath.
```

But there are two problems with this implementation:

1. the path found is not printed, and
2. it becomes extremely slow on more difficult problems (e.g., try to find the way from Skara to Torslanda).

You will address these problems in Parts 1b and 1c.

### Part 1b: Extracting the solution

Now you should write code to extract the solution, the list of edges forming the shortest path.
For this, implement and make use of the skeleton method `extractPath`:

```python
class UCS(Generic[V]):
    def extractPath(self, entry: UCSEntry[V]) -> List[Edge[V]]
```

```java
public class UCS<V> {
    public List<Edge<V>> extractPath(UCSEntry entry);
}
```

Make sure you get the order of edges right!

After this is completed, your output will change:

```
$ java PathFinder -a ucs -t AdjacencyGraph -g graphs/AdjacencyGraph/citygraph-VGregion.txt -q Skara Lerum
Searching for a path from Skara to Lerum...
Searching the graph took 0.12 seconds.
Loop iteration count: 66240
Cost of path from Skara to Lerum: 115
Number of edges: 6
Skara --[35]-> Vara --[28]-> Vårgårda --[9]-> Jonstorp --[15]-> Alingsås --[23]-> Stenkullen --[5]-> Lerum
```

As you can see, the result is the same as before, but now the path is printed too.
Check that you get the same path as shown here.

### Part 1c: Remembering visited nodes

The reason why the algorithm is slow is that it will revisit the same node every time it is reached.
There are hundreds of ways to get from Skara to Alingsås, and the algorithm visits most of them before it finds its way to Lerum.
But all the subsequent visits to Alingsås are unnecessary because the first visit is already via the shortest path (why is that?).

Therefore, a simple solution is to record the visited nodes in a set and avoid processing visited nodes.
You can choose a data structure for a set from the Java API.

**Note:** a node only counts as visited when we retrieve an entry for it from the priority queue.

When this is done, you should see a drastic improvement:

```
$ java PathFinder -a ucs -t AdjacencyGraph -g graphs/AdjacencyGraph/citygraph-VGregion.txt -q Skara Lerum
Searching for a path from Skara to Lerum...
Searching the graph took 0.01 seconds.
Loop iteration count: 291
Cost of path from Skara to Lerum: 115
Number of edges: 6
Skara --[35]-> Vara --[28]-> Vårgårda --[9]-> Jonstorp --[15]-> Alingsås --[23]-> Stenkullen --[5]-> Lerum
```

The number of loop iterations went down by a factor of 200!
Now you should be able to solve all kinds of problems in adjacency graphs, sliding puzzles, and grid graphs:

```
$ java PathFinder -a ucs -t AdjacencyGraph -g graphs/AdjacencyGraph/citygraph-EU.txt -q "Volos, Greece" "Oulu, Finland"
Searching for a path from Volos, Greece to Oulu, Finland...
Searching the graph took 0.03 seconds.
Loop iteration count: 23515
Cost of path from Volos, Greece to Oulu, Finland: 3488
Number of edges: 12
Volos, Greece --[923]-> Timişoara, Romania --[55]-> Arad, Romania --[114]-> Oradea, Romania --[83]-> Debrecen, Hungary --[50]-> ..... --[169]-> Lublin, Poland --[253]-> Białystok, Poland --[825]-> Tallinn, Estonia --[88]-> Helsinki, Finland --[607]-> Oulu, Finland

$ java PathFinder -a ucs -t SlidingPuzzle -g 3x3 -q /_AB/CDE/FGH/ /ABC/DEF/GH_/
Searching for a path from /_AB/CDE/FGH/ to /ABC/DEF/GH_/...
Searching the graph took 0.18 seconds.
Loop iteration count: 152439
Cost of path from /_AB/CDE/FGH/ to /ABC/DEF/GH_/: 22
Number of edges: 22
/_AB/CDE/FGH/ -> /A_B/CDE/FGH/ -> /ADB/C_E/FGH/ -> /ADB/_CE/FGH/ -> /ADB/FCE/_GH/ -> ..... -> /ABC/GDE/_HF/ -> /ABC/_DE/GHF/ -> /ABC/D_E/GHF/ -> /ABC/DE_/GHF/ -> /ABC/DEF/GH_/

$ java PathFinder -a ucs -t GridGraph -g graphs/GridGraph/maze-100x50.txt -q 1:1 199:99
Searching for a path from 1:1 to 199:99...
Searching the graph took 0.07 seconds.
Loop iteration count: 26478
Cost of path from 1:1 to 199:99: 1216.48
Number of edges: 1016
1:1 --[1]-> 1:2 --[1]-> 1:3 --[1]-> 1:4 --[1.41]-> 2:5 --[1.41]-> ..... --[1]-> 196:97 --[1]-> 197:97 --[1]-> 198:97 --[1.41]-> 199:98 --[1]-> 199:99
```

Go on!
Try the suggestions for the different graphs in the section "About the graphs in the collection" above!

***Important***:
Make sure you get the cost (shortest path length) shown in these examples.
If you got a higher cost, then UCS didn't find the optimal path.
If you got a lower cost, there's an error in how you calculate the path costs (or you take some illegal shortcuts).
Furthermore, it is a good sign (but not required) if your implementation has the same number of loop iterations as shown above (or very close).

### Questions for part 1

Run uniform-cost search on the shortest path problems in **answers.txt**.
List the requested information (number of loop iterations and information about the shortest path found).


## Part 2: Word ladders

The class **WordLadder** is not fully implemented.
This task is to make it work correctly.
What is implemented is the reading of the dictionary, adding of words, and some auxiliary methods.
What is missing is the implementation of `outgoingEdges`:

```java
public List<Edge<String>> outgoingEdges(String w);
```

An edge is one step in the word ladder.
The target word must:

- be in the dictionary,
- be of the same length,
- differ by exactly one letter.

At your disposal are the following two instance variables:

```java
private Set<String> dictionary;
private Set<Character> alphabet;
```

Here, `alphabet` is the set of letters appearing in dictionary words.
Use this instead of iterating over a fixed collection of characters.

**Note**: You should not iterate over all words in the dictionary (that's too expensive).

After you completed your implementation, you should be able to solve the following word ladders:

```
$ java PathFinder -a ucs -t WordLadder -g graphs/WordLadder/swedish-romaner.txt
Word ladder graph with 75740 words.
Alphabet: àaábâcdäeåfægçhèiéjêkëlmínîoïpqñrsótuõvöwxøyzúü

Random example nodes with outgoing edges:
* oförsynthet ---> with no outgoing edges
* utställt ---> utskällt, utställd
* galopperat ---> galopperar
* moralismen ---> moralismer
* basker ---> masker, banker
* kvällstidningarna ---> with no outgoing edges
* renare ---> genare, senare, rånare, redare, renade, renate
* apelsinsaft ---> with no outgoing edges

Start: mamma
Goal: pappa

Searching for a path from mamma to pappa...
Searching the graph took 0.02 seconds.
Loop iteration count: 888
Cost of path from mamma to pappa: 6
Number of edges: 6
mamma -> mumma -> summa -> sumpa -> pumpa -> puppa -> pappa

Start: katter
Goal: hundar

Searching for a path from katter to hundar...
Searching the graph took 0.07 seconds.
Loop iteration count: 9036
Cost of path from katter to hundar: 14
Number of edges: 14
katter -> kanter -> tanter -> tanten -> tanden -> ..... -> randas -> randad -> rundad -> rundar -> hundar

Start: örter
Goal: öring

Searching for a path from örter to öring...
Searching the graph took 0.08 seconds.
Loop iteration count: 20127
Cost of path from örter to öring: 30
Number of edges: 30
örter -> arter -> arten -> armen -> almen -> ..... -> slang -> klang -> kling -> kring -> öring
```

### Question for part 2

Use uniform-cost search to solve the word ladder problem in **answers.txt**.
List the requested information (number of loop iterations and information about the shortest path found).


## Part 3: The A* algorithm

The UCS algorithm finds an optimal path, but there is an optimisation which can help discover it much faster!
This algorithm is called A*.
Your task is to implement it in the method `search` of the class **Astar**.

The basic structure of A* is that of UCS, so you can start by copying your code for UCS to this method.
For each entry in the priority queue, A* doesn't just keep track of the cost so far, as in UCS, but also of the *estimated total cost* from the start, via this node, to the goal.
The latter score is used as the priority.

To be able to do this efficiently, you will need to use the class **AstarEntry**, which is an extension of **UCSEntry**.
You can add attributes to it as you see fit.

To work, A* needs a *distance heuristic*, an educated guess of the distance between two nodes.
This requires some additional insight into the problem, so the heuristics are different for different types of graphs and problems.
Our graph API (the interface **Graph**) provides this heuristic in the form of the method `guessCost`, which takes two nodes as argument and returns a cost estimate:

```java
interface Graph<V> {
    double guessCost(V v, V w);
}
```

The estimated total cost for an entry is then defined as the cost so far *plus* the estimated cost from the current node to the goal.
To tell the priority queue how to compare the entries, you need to pass it a comparator when you create it (see Part 1).

**Important**:
Make sure your implementation doesn't call `guessCost` too many times.
This could slow down your search.
The priority queue comparator should not call `guessCost` directly, but instead use a value stored with the priority queue entry.
Also avoid operations on the priority queue that take linear time such as iterating over it, or testing if it contains an entry.

**Important**:
There are other versions of A* that modify the priority of an existing entry in the priority queue (this is called a `decreaseKey` operation).
Our priority queue class does not support this operation.

When you have implemented A*, try it out for **SlidingPuzzle** problems.
This is the only graph type with a ready-baked heuristic (see the next task for how it works):

```
$ java PathFinder -a ucs -t SlidingPuzzle -g 3x3 -q /CBA/DEF/_HG/ /ABC/DEF/GH_/
Searching for a path from /CBA/DEF/_HG/ to /ABC/DEF/GH_/...
Searching the graph took 0.27 seconds.
Loop iteration count: 292528
Cost of path from /CBA/DEF/_HG/ to /ABC/DEF/GH_/: 24
Number of edges: 24
/CBA/DEF/_HG/ -> /CBA/_EF/DHG/ -> /_BA/CEF/DHG/ -> /B_A/CEF/DHG/ -> /BA_/CEF/DHG/ -> ..... -> /AC_/DBF/GEH/ -> /A_C/DBF/GEH/ -> /ABC/D_F/GEH/ -> /ABC/DEF/G_H/ -> /ABC/DEF/GH_/

$ java PathFinder -a astar -t SlidingPuzzle -g 3x3 -q /CBA/DEF/_HG/ /ABC/DEF/GH_/
Searching for a path from /CBA/DEF/_HG/ to /ABC/DEF/GH_/...
Searching the graph took 0.04 seconds.
Loop iteration count: 4536
Cost of path from /CBA/DEF/_HG/ to /ABC/DEF/GH_/: 24
Number of edges: 24
/CBA/DEF/_HG/ -> /CBA/DEF/H_G/ -> /CBA/D_F/HEG/ -> /C_A/DBF/HEG/ -> /_CA/DBF/HEG/ -> ..... -> /_AB/DEC/GHF/ -> /A_B/DEC/GHF/ -> /AB_/DEC/GHF/ -> /ABC/DE_/GHF/ -> /ABC/DEF/GH_/
```

Note that A* visits much fewer nodes (4,500 compared to 300,000 for UCS!), but finds a path of the same length as UCS.
If your implementation doesn't, then there's probably a bug somewhere.

If we don't have a way of guessing the cost, we should use 0.
That's the default implementation of `guessCost` in **Graph.java**.
In that case, the A* algorithm behaves exactly like UCS (why is that?).
Try that!
If you get different numbers of nodes visited, you might have a bug.

### Questions for part 3

Use A* to solve the sliding puzzles in **answers.txt**.
You only have to state the number of loop iterations and calculated distance.

## Part 4: Guessing the cost

The graph API method `guessCost` should return an *optimistic* guess for the cost (distance) between two nodes.
This is already implemented for **SlidingPuzzle**, but is missing for the other graph types.
The implementation for **SlidingPuzzle** estimates the cost as the sum over each tile of the [Manhattan distance](https://en.wikipedia.org/wiki/Taxicab_geometry) between its positions in the source and target state.
This is the implementation (in pseudocode):

```
guessCost(s, t):
    cost = 0
    for tile in tiles (excluding the empty tile):
        (sx, sy) = coordinates of tile in s
        (tx, ty) = coordinates of tile in t
        cost += |sx - tx| + |sy - ty|
    return cost
```

Your task is to implement the following `guessCost` heuristic for **GridGraph** and **WordLadder**:

- **GridGraph**:
  The [straight-line distance](https://en.wikipedia.org/wiki/Euclidean_distance) between the two points.
  You may find some methods of the **Point** class useful here.
  After implementing it, you should see an improvement in the number of loop iterations.

  ```
  $ java PathFinder -a ucs -t GridGraph  -g graphs/GridGraph/AR0012SR.txt -q 11:73 85:127
  Searching for a path from 11:73 to 85:127...
  Searching the graph took 0.07 seconds.
  Loop iteration count: 40266
  Cost of path from 11:73 to 85:127: 147.68
  Number of edges: 122
  11:73 --[1]-> 12:73 --[1.41]-> 13:74 --[1.41]-> 14:75 --[1]-> 15:75 --[1.41]-> ..... --[1]-> 86:123 --[1.41]-> 85:124 --[1]-> 85:125 --[1]-> 85:126 --[1]-> 85:127

  $ java PathFinder -a astar -t GridGraph  -g graphs/GridGraph/AR0012SR.txt -q 11:73 85:127
  Searching for a path from 11:73 to 85:127...
  Searching the graph took 0.05 seconds.
  Loop iteration count: 16700
  Cost of path from 11:73 to 85:127: 147.68
  Number of edges: 122
  11:73 --[1]-> 12:73 --[1]-> 13:73 --[1]-> 14:73 --[1]-> 15:73 --[1.41]-> ..... --[1]-> 87:123 --[1.41]-> 86:124 --[1]-> 86:125 --[1.41]-> 85:126 --[1]-> 85:127
  ```

  Note the improvement from 40,000 iterations to 17,000.

- **WordLadder**:
  The number of character positions where the letters differ from each other.
  For example, `guessCost("örter", "arten")` should return 2: the first and last characters differ (`ö`/`a` and `r`/`n`), but the middle ones (`rte`) are the same.
  Your method should not fail if it happens to be called on words of different length, but the return value then doesn't matter much (why is that?) – you can, e.g., return a very large number.

  ```
  $ java PathFinder -a ucs -t WordLadder -g graphs/WordLadder/swedish-saldo.txt -q eller glada
  Searching for a path from eller to glada...
  Searching the graph took 0.18 seconds.
  Loop iteration count: 25481
  Cost of path from eller to glada: 7
  Number of edges: 7
  eller -> elles -> ellas -> elias -> glias -> glids -> glads -> glada

  $ java PathFinder -a astar -t WordLadder -g graphs/WordLadder/swedish-saldo.txt -q eller glada
  Searching for a path from eller to glada...
  Searching the graph took 0.04 seconds.
  Loop iteration count: 192
  Cost of path from eller to glada: 7
  Number of edges: 7
  eller -> elles -> ellas -> elias -> glias -> glids -> glads -> glada
  ```

  Note the improvement from 25,000 iterations to only 200!

- You don't have to implement `guessCost` for **AdjacencyGraph**.
  That would need domain-specific information about the graph, which the class does not have.
  But see the optional tasks below.

### Important note

The A* algorithm works correctly only if the heuristic is *admissible*, which means that the heuristic must never over-estimate the cost.
It's fine to under-estimate: it will still find an optimal path, but if the heuristic is too under-estimating, it will take longer.

### Questions for part 4

Use A* to find shortest paths for the queries in **answers.txt**.
You only have to state the number of loop iterations and calculated distance.


## Part 5: Reflections

Answer the final reflection questions in the file **answers.txt**.


## Submission

Double check:
* Have you done all the tasks?
  - **PathFinder.java**: Part 1 and 3
  - **WordLadder.java**: Part 2 and 4
  - **GridGraph.java**: Part 4
* Have you answered the questions in **answers.txt**?
  (Don't forget the ones in the appendix.)
* Have you tested your code with **Robograder**?


## Optional tasks

This lab can be expanded in several ways, here are only some suggestions:

* Try the implementations on more graphs: There are several to download from [Moving AI Lab](https://www.movingai.com/benchmarks/grids.html), or [the SNAP project](http://snap.stanford.edu/data/index.html).
  You can also create more random mazes from several places on the web (search for "random maze generator").

* Show the results nicer, e.g., as an animation (for **SlidingPuzzle**).

* **WordLadder** only connects words of the start and goal have the same length.
  Invent and implement word ladder rules for changing the number of letters in a word.
  Remember that all graph nodes must be words in the dictionary.

* You can assign different costs for different kinds of "terrain" (i.e., different characters) in **GridGraph**.

* Try to come up with an even better admissible heuristic than straight-line distance for **GridGraph**.
  Hint: Modify the [Manhattan distance](https://en.wikipedia.org/wiki/Taxicab_geometry) so that it allows for diagonal moves.

* Experiment with different representation of the state in **SlidingPuzzle**.
  See the comments of the internal class **SlidingPuzzle.State**.

* Implement code for reading other graph formats.
  For example, you could read an image as a graph where where dark pixels are considered obstacles.
  See the PNG files in `graphs/GridGraph` for examples.

* Implement a heuristic for roadmaps (**citygraph-XX.txt**).
  For this you need locations of all cities, and they can be found in the [DSpace-CRIS database](https://dspace-cris.4science.it/handle/123456789/31).
  You have to scrape the information you need from the database, i.e., the latitude-longitude of each city.
  Then you have to filter the database to only include the cities you want.
  You also need to read in the position database in the graph class, and finally you need to [translate latitude-longitude into kilometers](https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula).

- What kind of heuristic could be useful for the link distance between two Wikipedia pages (remember **wikipedia-graph.txt**)?
  Assume e.g. that you know the text content of both pages.
  Or that you know the [categories](https://en.wikipedia.org/wiki/Help:Category) that each Wikipedia page belongs to.
