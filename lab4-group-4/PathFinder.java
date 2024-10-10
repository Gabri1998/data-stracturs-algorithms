import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.function.Supplier;

import graphtypes.AdjacencyGraph;
import graphtypes.Graph;
import graphtypes.GridGraph;
import graphtypes.SlidingPuzzle;
import graphtypes.WordLadder;
import searchers.Astar;
import searchers.Query;
import searchers.Random;
import searchers.UCS;
import utilities.CommandParser;
import utilities.Stopwatch;

/**
 * This is the main class for finding paths in graphs.
 *
 * Depending on the command line arguments,
 * it creates different graphs and runs different search algorithms.
 */
public class PathFinder {

    // Settings for showing the solution - you can change these if you want.
    static final boolean showFullPath = false;
    static final boolean showPathWeights = true;
    static final boolean showGridGraph = true;
    static final int maxGridGraphWidth = 100;
    static final int maxGridGraphHeight = 25;

    @FunctionalInterface
    interface Searcher<V> {
        Query<V> build(Graph<V> graph, V start, V goal);
    }
    
    static final Map<String, Searcher<?>> searchers = Map.of(
        "random", Random::new,
        "ucs", UCS::new,
        "astar", Astar::new
    );

    static final Map<String, Supplier<Graph<?>>> graphTypes = Map.of(
        "AdjacencyGraph", AdjacencyGraph::new,
        "GridGraph", GridGraph::new,
        "SlidingPuzzle", SlidingPuzzle::new,
        "WordLadder", WordLadder::new
    );

    public static void main(String[] args) throws IOException {
        CommandParser parser = new CommandParser("PathFinder", "This is the main file for finding paths in graphs.");
        parser.addArgument("--algorithm", "-a", "search algorithm to test")
            .makeRequired().setChoices(searchers.keySet());
        parser.addArgument("--graphtype", "-t", "sorting algorithm")
            .makeRequired().setChoices(graphTypes.keySet());
        parser.addArgument("--graph", "-g", "the graph itself")
            .makeRequired();
        parser.addArgument("--queries", "-q", "list of start and goal nodes (alternating)")
            .makeList();

        CommandParser.Namespace options = parser.parseArgs(args);

        Graph<?> graph = graphTypes.get(options.getString("graphtype")).get();
        graph.init(options.getString("graph"));
        main2(graph, options);
    }

    public static <V> void main2(Graph<V> graph, CommandParser.Namespace options) throws IOException{
        @SuppressWarnings("unchecked")
        Searcher<V> algorithm = (Searcher<V>) searchers.get(options.getString("algorithm"));

        List<String> queries = options.getStringList("queries");
        if (queries == null || queries.isEmpty())
            searchInteractive(graph, algorithm);
        else {
            Iterator<String> it = queries.iterator();
            while (it.hasNext()) {
                String start = it.next();
                if (!it.hasNext())
                    throw new IllegalArgumentException("Start node " + start + " does not have a goal node");
                String goal = it.next();
                searchOnce(graph, algorithm, start, goal);
            }
        }
    }

    public static <V> void searchInteractive(Graph<V> graph, Searcher<V> algorithm) {
        Graph.printGraphWithExamples(graph);
        System.out.println();
        @SuppressWarnings("resource")
        Scanner in = new Scanner(System.in);
        while (true) {
            System.out.print("Start: "); System.out.flush();
            if (!in.hasNextLine()) {
                System.out.println();
                break;
            }
            String start = in.nextLine().strip();
            if (start.isEmpty()) 
                break;
            System.out.print("Goal: "); System.out.flush();
            String goal = in.nextLine().strip();
            System.out.println();
            searchOnce(graph, algorithm, start, goal);
        }
        System.out.println("Bye bye, hope to see you again soon!");
    }

    public static <V> void searchOnce(Graph<V> graph, Searcher<V> algorithm, String start, String goal) {
        V startNode, goalNode;
        try {
            startNode = graph.parseNode(start);
            goalNode = graph.parseNode(goal);
        } catch (IllegalArgumentException e) {
            System.err.println("Parse error!");
            System.err.println(e);
            System.err.println();
            return;
        }
        System.out.println("Searching for a path from " + startNode + " to " + goalNode + "...");
        Stopwatch stopwatch = new Stopwatch();
        Query<V>.Result result = algorithm.build(graph, startNode, goalNode).search();
        stopwatch.finished("Searching the graph");
        System.out.println(result.toString(showFullPath, showPathWeights, showGridGraph, maxGridGraphWidth, maxGridGraphHeight));
    }

}

