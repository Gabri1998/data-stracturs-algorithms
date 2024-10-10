package graphtypes;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A generalization of the 15-puzzle.
 *
 * The nodes are string encodings of an N x M matrix of tiles.
 * The tiles are represented by characters starting from the letter A
 * (for example, A...H for N=M=3, and A...O for N=M=4).
 * The empty tile is represented by "_", and
 * to make it more readable for humans every row is separated by "/".
 */
public class SlidingPuzzle implements Graph<SlidingPuzzle.State> {
    private int N, M;

    private static final String SEPARATOR = "/";

    // The characters '_', 'A', ..., 'Z', '0', ..., '9', 'a', ..., 'z'.
    // A fixed NPuzzle uses only an initial prefix of these characters.
    private static final String ALL_TILE_NAMES =
        "_" +
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ" +
        "0123456789" +
        "abcdefghijklmnopqrstuvwxyz";

    private static final Point[] MOVES = {
        new Point(-1, 0),
        new Point(1, 0),
        new Point(0, -1),
        new Point(0, 1)
    };

    public SlidingPuzzle() {}
    public SlidingPuzzle(int N) {
        this(N, N);
    }
    public SlidingPuzzle(int N, int M) {
        init(N, M);
    }
    public SlidingPuzzle(String graph) {
        init(graph);
    }

    /**
     * Creates a new sliding puzzle of dimensions N x M.
     */
    public void init(int N, int M) {
        if (!(N >= 1 && M >= 1))
            throw new IllegalArgumentException("The dimensions must be positive.");
        if (!(M * N <= 40))
            throw new IllegalArgumentException("We only support up to 40 tiles.");
        this.N = N;
        this.M = M;
    }

    @Override
    public void init(String graph) {
        int N, M;
        try {
            String[] dimensions = graph.split("\\s*x\\s*", 2);
            N = Integer.parseInt(dimensions[0]);
            M = Integer.parseInt(dimensions[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid dimensions (e.g., 3x3): " + graph);
        }
        init(N, M);
    }

    /**
     * A possible state of the sliding puzzle.
     *
     * We represent the tiles as numbers from 0 to N * M.
     * The empty tile is represented by 0.
     *
     * The array `positions` stores the position of each tile.
     *
     * Optional task: try out different representations of states:
     *  - coding the positions as indices (sending a point p to p.y * M + p.x)
     *  - using an array `tiles` that stores the tile at each point
     *  - combinations (more space usage, but better runtime?)
     */
    public class State {
        public final Point[] positions;

        State(Point[] positions) {
            this.positions = positions;
        }

        /**
         * Parse a state from its string representation.
         * For example, a valid string representation for N = M = 3 is "/FDA/CEH/GB_/".
         */
        public State(String s) {
            String[] rows = Arrays.stream(s.split(Pattern.quote(SEPARATOR)))
                .filter(row -> !row.isEmpty())
                .toArray(String[]::new);
            int N = rows.length;
            this.positions = new Point[N * M];
            for (int y = 0; y < N; y++) {
                String row = rows[y];
                if (row.length() != M)
                    throw new IllegalArgumentException("Row " + row + " does not have " + M + " columns.");
                for (int x = 0; x < M; x++) {
                    char tileName = row.charAt(x);
                    int i = ALL_TILE_NAMES.indexOf(tileName);
                    if (this.positions[i] != null)
                        throw new IllegalArgumentException("Duplicate tiles: " + tileName);
                    this.positions[i] = new Point(x, y);
                }
            }
        }

        /**
         * Returns the state given by swapping the tiles `i` and `j`.
         */
        public State swap(int i, int j) {
            Point[] positionsNew = this.positions.clone();
            positionsNew[i] = this.positions[j];
            positionsNew[j] = this.positions[i];
            return new State(positionsNew);
        }

        /**
         * Returns a randomly shuffled state.
         */
        public State shuffled() {
            Point[] positionsNew = this.positions.clone();
            Collections.shuffle(Arrays.asList(positionsNew));
            return new State(positionsNew);
        }

        /**
         * Returns the NxN-matrix of tiles of this state.
         */
        public int[][] tiles() {
            int[][] tiles = new int[N][M];
            for (int i = 0; i != this.positions.length; i++) {
                Point p = this.positions[i];
                tiles[p.y][p.x] = i;
            }
            return tiles;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) // equality of references
                return true;
            if (!(o instanceof State))
                return false;
            return Arrays.deepEquals(this.positions, ((State) o).positions);
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(this.positions);
        }

        @Override
        public String toString() {
            return Arrays.stream(this.tiles())
                .map(rowTiles -> Arrays.stream(rowTiles)
                    .mapToObj(ALL_TILE_NAMES::charAt)
                    .map(String::valueOf)
                    .collect(Collectors.joining())
                ).collect(Collectors.joining(SEPARATOR, SEPARATOR, SEPARATOR));
        }
    }

    // Helper methods for formatting and parsing tiles.

    public char formatTile(int tile) {
        return ALL_TILE_NAMES.charAt(tile);
    }

    /**
     * All states are nodes of this graph.
     * However, the set of such nodes is typically too large to enumerate.
     * So we do not implement those operations.
     */
    @Override
    public Set<State> nodes() {
        throw new UnsupportedOperationException("too expensive!");
    }

    public List<Edge<State>> outgoingEdges(State v) {
        Point emptyPos = v.positions[0];
        ArrayList<Edge<State>> edges = new ArrayList<>();
        for (Point move : MOVES) {
            Point p = emptyPos.subtract(move);
            if (isValidPoint(p)) {
                int i = Arrays.asList(v.positions).indexOf(p);
                State newState = v.swap(0, i);
                edges.add(new Edge<>(v, newState));
            }
        }
        return edges;
    }

    public boolean isWeighted() {
        return false;
    }

    /**
     * Checks if the point is valid (lies inside the matrix).
     */
    public boolean isValidPoint(Point p) {
        return p.x >= 0 && p.y >= 0 && p.x < M && p.y < N;
    }

    /**
     * We guess the minimal cost for getting from one puzzle state to another,
     * as the sum of the Manhattan displacement for each tile.
     * The Manhattan displacement is the Manhattan distance from where
     * the tile is currently to its desired location.
     */
    @Override
    public double guessCost(State v, State w) {
        int cost = 0;
        for (int i = 1; i < N * M; i++) {
            cost += v.positions[i].subtract(w.positions[i]).manhattanNorm();
        }
        return cost;
    }

    /**
     * Return the traditional goal state.
     * The empty tile is in the bottom right corner.
     */
    public State goalState() {
        return new State(IntStream.range(0, N * M)
                .map(i -> Math.floorMod(i - 1, N * M))
                .mapToObj(i -> new Point(i % M, i / M))
                .toArray(Point[]::new));
    }

    public State parseNode(String str) {
        return new State(str);
    }

    @Override public Supplier<State> randomNodes() {
        return () -> this.goalState().shuffled();
    }

    @Override
    public String toString() {
        StringWriter buffer = new StringWriter();
        PrintWriter w = new PrintWriter(buffer);
        w.println("NPuzzle graph of dimensions " + N + " x " + M + ".");
        w.println("Nodes are puzzle states, " + N + " x " + M + " matrices of tiles.");
        w.println("Such a matrix is written as a string of character tiles, with rows separated by '\" + SEPARATOR + \"'");
        w.println("Tiles are represented by unique characters '" + formatTile(1) + "'...'" + formatTile(N*M-1) + "' and '" + formatTile(0) + "' (for the empty tile).");
        w.println("The traditional goal state is: " + goalState());
        return buffer.toString();
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1)
            throw new IllegalArgumentException("missing argument: dimensions for n-puzzle");
        SlidingPuzzle graph = new SlidingPuzzle(args[0]);
        Graph.printGraphWithExamples(graph);
    }
}

