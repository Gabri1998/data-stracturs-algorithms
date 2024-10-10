package graphtypes;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * A class for weighted directed edges.
 */

public class Edge<V> {

    public final V start;
    public final V end;
    public final double weight;

    public Edge(V start, V end, double weight) {
        this.start = start;
        this.end = end;
        this.weight = weight;
    }

    public Edge(V start, V end) {
        this(start, end, 1.0);
    }

    /**
     * Returns a new edge with the direction reversed.
     */
    public Edge<V> reverse() {
        return new Edge<>(this.end, this.start, this.weight);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (!(other instanceof Edge)) return false;
        Edge<?> o = (Edge<?>) other;
        return this.start.equals(o.start) && this.end.equals(o.end) && this.weight == o.weight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.start, this.end, this.weight);
    }

    // Different ways of formatting edges.

    public static final DecimalFormat COST_FORMAT = new DecimalFormat("0.##");

    public String formatWeight() {
        return COST_FORMAT.format(weight);
    }

    public String toString(boolean includeStart, boolean includeEnd, boolean withWeight) {
        String startStr = includeStart ? this.start.toString() : "";
        String endStr = includeEnd ? this.end.toString() : "";
        String arrow = withWeight ? "--[" + COST_FORMAT.format(weight) + "]->" : "->";
        return startStr + " " + arrow + " " + endStr;
    }

    public String toString(boolean includeStart, boolean includeEnd) {
        return this.toString(includeStart, includeEnd, this.weight != 1.0);
    }

    @Override
    public String toString() {
        return this.toString(true, true);
    }

}

