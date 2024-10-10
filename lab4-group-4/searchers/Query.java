package searchers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import graphtypes.Edge;
import graphtypes.Graph;

/**
 * This is an abstract class for a search query in a given graph.
 * The graph as well as start and goal nodes are instance variables.
 * Call `search` to perform a search and obtain the search result.
 */
public abstract class Query<V> {
    public final Graph<V> graph;
    public final V start, goal;

    public Query(Graph<V> graph, V start, V goal) {
        this.graph = graph;
        this.start = start;
        this.goal = goal;
    }

    /**
     * Searches for a path in `graph` from `start` to `goal`.
     * Returns the search result (which includes the path found if successful).
     */
    public abstract Result search();

    /**
     * Construct a failure result (no path found).
     */
    public Result failure(int iterations) {
        return new Result(false, -1, null, iterations);
    }

    /**
     * Construct a success result (path found).
     */
    public Result success(double cost, List<Edge<V>> path, int iterations) {
        return new Result(true, cost, path, iterations);
    }

    /**
     * Class for search results.
     * You don't have to look into this.
     */
    public class Result {
        public final boolean success;
        public final double cost;
        public final List<Edge<V>> path;
        public final int iterations;

        public Result(boolean success, double cost, List<Edge<V>> path, int iterations) {
            this.success = success;
            this.cost = cost;
            this.path = path;
            this.iterations = iterations;
        }

        private String formatPathPart(boolean suffix, boolean withWeight, int i, int j) {
            return path.subList(i, j).stream()
                .map(e -> e.toString(!suffix, suffix, withWeight && graph.isWeighted()))
                .collect(Collectors.joining());
        }

        public void validatePath() {
            if (this.success) {
                if (this.path == null)
                    throw new IllegalArgumentException("The path is null. Remember to implement extractPath.");

                // We sum using left association order to mimic the algorithm.
                // Then we can use exact comparison of doubles.
                double actualCost = path.stream()
                    .mapToDouble((e) -> e.weight)
                    .reduce(0, Double::sum);
                if (cost != actualCost)
                    throw new IllegalArgumentException("The actual path cost " + actualCost + " differs from the reported cost " + cost + ".");
            } else {
                if (this.path != null)
                    throw new IllegalArgumentException("Failure reported, but path is null.");
            }
        }

        public void validateIterations() {
            if (this.iterations <= 0)
                throw new IllegalArgumentException("The number of iterations should be > 0.");
        }

        public void validate() {
            validateIterations();
            validatePath();
        }

        public String toString(boolean showFullPath, boolean withWeight, boolean drawGraph, int maxGraphWidth, int maxGraphHeight) {
            StringWriter buffer = new StringWriter();
            PrintWriter w = new PrintWriter(buffer);

            try {
                validateIterations();
                w.println("Loop iteration count: " + iterations);
            } catch (IllegalArgumentException e) {
                w.println("WARNING: " + e.getMessage());
            }

            String endpointsStr = "from " + start + " to " + goal;
            if (success)
                w.println("Cost of path " + endpointsStr + ": " + Edge.COST_FORMAT.format(cost));
            else
                w.println("No path " + endpointsStr + " found.");

            try {
                validatePath();
            } catch (IllegalArgumentException e) {
                w.println("WARNING: " + e.getMessage());
            }

            if (path != null) {
                int n = path.size();
                w.println("Number of edges: " + n);
                if (showFullPath || path.size() <= 10)
                    w.println(start + formatPathPart(true, withWeight, 0, n));
                else
                    w.println(formatPathPart(false, withWeight, 0, 5) + "....." + formatPathPart(true, withWeight, n - 5, n));
                if (drawGraph) {
                    String graphStr = graph.drawGraph(maxGraphWidth, maxGraphHeight, start, goal, this.path);
                    if (graphStr != null) {
                        w.println();
                        w.print(graphStr);
                    }
                }
            }

            return buffer.toString();
        }
    }

}

