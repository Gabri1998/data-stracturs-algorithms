package graphtypes;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

/**
 * This is a class for a generic finite graph, with string nodes.
 *  - The edges are stored as an adjacency list as described in the course book and the lectures.
 *  - The graphs can be anything, such as a road map or a web link graph.
 *  - The graph can be read from a simple text file with one edge per line.
 */

public class AdjacencyGraph implements Graph<String> {
    private final Map<String, List<Edge<String>>> adjacencyList;
    private boolean weighted;

    public AdjacencyGraph() {
        adjacencyList = new HashMap<>();
        weighted = false;
    }

    public AdjacencyGraph(String graph) throws IOException {
        this();
        init(graph);
    }

    /**
     * Populates the graph with edges from a text file.
     * The file should contain one edge per line, each on the form
     * "from \\t to \\t weight" or "from \\t to"(where \\t == TAB).
     */
    @Override
    public void init(String graph) throws IOException {
        Files.lines(Paths.get(graph))
            .map(String::strip)
            .filter(line -> !line.startsWith("#"))
            .map(line -> line.split("\t"))
            .map(parts -> ( parts.length == 2
                          ? new Edge<>(parts[0], parts[1])
                          : new Edge<>(parts[0], parts[1], Double.parseDouble(parts[2]))
                          ))
            .forEach(this::addEdge);
    }

    /**
     * Adds a node to this graph.
     */
    public void addNode(String v) {
        adjacencyList.putIfAbsent(v, new LinkedList<>());
    }

    /**
     * Adds a directed edge (and its source and target nodes) to this edge-weighted graph.
     * Note: This does not test if the edge is already in the graph!
     */
    public void addEdge(Edge<String> e) {
        addNode(e.start);
        addNode(e.end);
        adjacencyList.get(e.start).add(e);
        if (e.weight != 1)
            weighted = true;
    }

    @Override
    public Set<String> nodes() {
        return Collections.unmodifiableSet(adjacencyList.keySet());
    }

    @Override
    public List<Edge<String>> outgoingEdges(String v) {
        return Collections.unmodifiableList(adjacencyList.get(v));
    }

    public boolean isWeighted() {
        return weighted;
    }

    @Override
    public String parseNode(String s) {
        if (!this.adjacencyList.containsKey(s))
            throw new IllegalArgumentException("Unknown node: " + s);
        return s;
    }

    @Override
    public String toString() {
        StringWriter buffer = new StringWriter();
        PrintWriter w = new PrintWriter(buffer);
        String weightedStr = weighted ? "Weighted" : "Unweighted";
        w.println(weightedStr + " adjacency graph with " + numNodes() + " nodes and " + numEdges() + " edges.");
        return buffer.toString();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1)
            throw new IllegalArgumentException("missing argument: path to adjacency graph file");
        AdjacencyGraph graph = new AdjacencyGraph(args[0]);
        Graph.printGraphWithExamples(graph);
    }
}

