package graphtypes;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * GridGraph is a 2D-map encoded as a bitmap, or an N x M matrix of characters.
 *
 * Some characters are passable, others denote obstacles.
 * A node is a point in the bitmap, consisting of an x- and a y-coordinate.
 * This is defined by the helper class `Point`.
 * You can move from each point to the eight point around it.
 * The edge costs are 1.0 (for up/down/left/right) and sqrt(2) (for diagonal movement).
 * The graph can be read from a simple ASCII art text file.
 */

public class GridGraph implements Graph<Point> {
    private char[][] grid;

    // Characters from Moving AI Lab:
    //   . - passable terrain
    //   G - passable terrain
    //   @ - out of bounds
    //   O - out of bounds
    //   T - trees (unpassable)
    //   S - swamp (passable from regular terrain)
    //   W - water (traversable, but not passable from terrain)
    // Characters from http://www.delorie.com/game-room/mazes/genmaze.cgi
    //   | - +  walls
    //   space  passable
    // Note: "-" must come last in allowedChars, because we use it unescaped in a regular expression.

    private static final String allowedChars = ".G@OTSW +|-";
    private static final String passableChars = ".G ";

    // The eight directions, as points.
    private static final Point[] directions =
        IntStream.rangeClosed(-1, 1).boxed().flatMap(x ->
            IntStream.rangeClosed(-1, 1).boxed().flatMap(y ->
                Stream.of(new Point(x, y)).filter(p -> !p.equals(Point.ORIGIN))
            )
        ).toArray(Point[]::new);

    public int width() {
        return this.grid[0].length;
    }

    public int height() {
        return this.grid.length;
    }

    public GridGraph() {}
    public GridGraph(String graph) throws IOException {
        this.init(graph);
    }
    public GridGraph(char[][] graph) {
        this.init(graph);
    }

    /**
     * Initialises the graph with edges from a text file.
     * The file describes the graph as ASCII art,
     * in the format of the graph files from the Moving AI Lab.
     */
    @Override
    public void init(String graph) throws IOException {
        init(Files.lines(Paths.get(graph))
            .filter(line -> line.matches("^[" + allowedChars + "]+$"))
            .map(String::toCharArray)
            .toArray(char[][]::new)
        );
    }

    /**
     * Initialises the graph from a grid of characters.
     */
    public void init(char[][] graph) {
        this.grid = graph;
        for (char[] row : this.grid) {
            if (row.length != this.width())
                throw new IllegalArgumentException("Malformed grid, row widths don't match.");
        }
    }

    /**
     * Returns true if you're allowed to pass through the given point.
     */
    private boolean passable(Point p) {
        return (
            0 <= p.x && p.x < this.width() &&
            0 <= p.y && p.y < this.height() &&
            GridGraph.passableChars.indexOf(this.grid[p.y][p.x]) >= 0
        );
    }

    @Override
    public Set<Point> nodes() {
        // Note: this is inefficient because it calculates the set each time.
        HashSet<Point> nodes = new HashSet<>();
        for (int y = 0; y < this.height(); y++) {
            for (int x = 0; x < this.width(); x++) {
                Point p = new Point(x, y);
                if (this.passable(p))
                    nodes.add(p);
            }
        }
        return Collections.unmodifiableSet(nodes);
    }

    @Override
    public List<Edge<Point>> outgoingEdges(Point p) {
        return
            // We consider all directions...
            Arrays.stream(GridGraph.directions)
            // ...compute the edge in that direction...
            .map(dir -> new Edge<>(p, p.add(dir), dir.euclideanNorm()))
            // ...keep the ones with passable target...
            .filter(edge -> this.passable(edge.end))
            // ...and return them as a list.
            .collect(Collectors.toList());
    }

    public boolean isWeighted() {
        return true;
    }

    /**
     * Returns the guessed best cost for getting from one point to another.
     * (the Euclidean distance between the points)
     */
    @Override
    public double guessCost(Point v, Point w) {
        // TODO: Replace these lines with your solution!
        //throw new UnsupportedOperationException();

        /*cost = 0
    for tile in tiles (excluding the empty tile):
        (sx, sy) = coordinates of tile in s
        (tx, ty) = coordinates of tile in t
        cost += |sx - tx| + |sy - ty|*/
        double cost=0;

        cost+= Math.pow((v.x-w.x),2)+Math.pow((v.y-w.y),2);

        return Math.sqrt(cost);
    }

    /**
     * Parse a point from a string representation.
     * For example, a valid string representation is "39:18".
     */
    @Override
    public Point parseNode(String s) {
        return Point.parse(s);
    }

    /**
     * Returns a graphical string representation of the grid graph.
     * If provided, start and end nodes and a path are marked.
     */
    @Override
    public String drawGraph(int maxWidth, int maxHeight, Point start, Point goal, List<Edge<Point>> path) {
        Set<Point> pathPoints = new HashSet<>();
        if (path != null) {
            path.stream().map(e -> e.start).forEach(pathPoints::add);
            path.stream().map(e -> e.end).forEach(pathPoints::add);
        }

        StringWriter buffer = new StringWriter();
        PrintWriter w = new PrintWriter(buffer);
        for (int y = 0; y < this.height(); y++) {
            if (y >= maxHeight) {
                w.print("(truncated)");
                break;
            }
            for (int x = 0; x < this.width(); x++) {
                if (y == 0 && x >= maxWidth - 10) {
                    w.print(" (truncated)");
                    break;
                }
                if (x >= maxWidth)
                    break;
                char c = this.grid[y][x];
                Point point = new Point(x, y);
                if (point.equals(start))
                    c = 'S';
                else if (point.equals(goal))
                    c = 'G';
                else if (pathPoints.contains(point))
                    c = '*';
                w.print(c);
            }
            w.println();
        }
        return buffer.toString();
    }

    /**
     * Returns a string representation of this graph, including some random points and edges.
     */
    @Override
    public String toString() {
        StringWriter buffer = new StringWriter();
        PrintWriter w = new PrintWriter(buffer);
        w.println("Grid graph of dimensions " + width() + " x " + height() + ".");
        w.println();
        w.print(drawGraph(100, 25));
        return buffer.toString();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1)
            throw new IllegalArgumentException("missing argument: path to grid graph data file");
        GridGraph graph = new GridGraph(args[0]);
        Graph.printGraphWithExamples(graph);
    }
}

