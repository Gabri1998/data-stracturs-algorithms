package graphtypes;

import java.io.IOException;
import java.util.Random;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A simplistic interface for directed graphs.
 *
 * Note that this interface differs from the graph interface in the course API:
 * - it lacks several of the methods in the course API,
 * - it has an additional method `guessCost`.
 */
public interface Graph<V> {

    /**
     * Initialises a graph from a file or a name or other description.
     */
    public void init(String graph) throws IOException;

    /**
     * Returns an unmodifiable set of nodes of this graph.
     */
    Set<V> nodes();

    /**
     * Returns a list of the graph edges that originate from the given node.
     */
    List<Edge<V>> outgoingEdges(V v);

    /**
     * Returns if the graph edges are weighted.
     */
    public boolean isWeighted();

    /**
     * Returns the guessed best cost for getting from v to w.
     * The default guessed cost is 0, which is always admissible.
     */
    default double guessCost(V v, V w) {
        return 0.0;
    }

    // Below are some auxiliary methods.
    // You don't have to look at them.
    // They are used for parsing and printing.

    /**
     * Returns the number of nodes in this graph.
     */
    default int numNodes() {
        return this.nodes().size();
    }

    /**
     * Returns the number of edges in this graph.
     * (Warning: the default implementation is inefficient).
     */
    default int numEdges() {
        return this.nodes().stream()
            .mapToInt((v) -> this.outgoingEdges(v).size())
            .sum();
    }

    /**
     * Returns a node parsed from the given string.
     *
     * This is really an operation associated with the node type V, not Graph,
     * but there's no easy way to do that in Java.
     * So the result is not related to the nodes currently contained in the graph.
     */
    V parseNode(String s);

    /**
     * Returns a graphical string representation of the graph.
     * If provided, start and end nodes and a path are marked.
     *
     * Only implemented for grid graphs.
     */
    default String drawGraph(int maxWidth, int maxHeight, V start, V goal, List<Edge<V>> path) {
        return null;
    }

    default String drawGraph(int maxWidth, int maxHeight) {
        return drawGraph(maxWidth, maxHeight, null, null, null);
    }

    /**
     * Generate an infinite stream of random nodes.
     */
    default Supplier<V> randomNodes() {
        ArrayList<V> nodeList = new ArrayList<>(this.nodes());
        Random random = new Random();
        return () -> nodeList.get(random.nextInt(nodeList.size()));
    }

    /**
     * Print some graph information.
     */
    public static <V> void printExampleOutgoingEdges(Graph<V> graph, int limit) {
        System.out.println("Random example nodes with outgoing edges:");
        Stream.generate(graph.randomNodes()).<String>map(start -> {
            List<Edge<V>> outgoing = graph.outgoingEdges(start);
            String targets;
            if (outgoing.isEmpty())
                targets = "with no outgoing edges";
            else
                targets = outgoing.stream().map(e -> {
                    String annotation = graph.isWeighted() ? " [" + e.formatWeight() + "]" : "";
                    return e.end + annotation;
                }).collect(Collectors.joining(", "));
            return "* " + start + " ---> " + targets;
        }).limit(limit).forEach(System.out::println);
    }

    public static <V> void printGraphWithExamples(Graph<V> graph) {
        System.out.println(graph);
        printExampleOutgoingEdges(graph, 8);
    }
}

