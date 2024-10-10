package searchers;

import graphtypes.Edge;
import graphtypes.Graph;

import java.util.List;
import java.util.LinkedList;

/**
 * Perform a random walk in the graph, hoping to reach the goal.
 * Warning: this class will give up of the random walk
 * reaches a dead end or after 10,000 iterations.
 * So a negative result does not mean there is no path.
 */
public class Random<V> extends Query<V> {

    public Random(Graph<V> graph, V start, V goal) {
        super(graph, start, goal);
    }

    @Override
    public Result search() {
        java.util.Random random = new java.util.Random();
        int iterations = 0;
        double cost = 0;
        LinkedList<Edge<V>> path = new LinkedList<>();

        V current = start;
        while (iterations < 10_000) {
            iterations++;
            if (current.equals(goal))
                return success(cost, path, iterations);

            List<Edge<V>> neighbours = this.graph.outgoingEdges(current);
            if (neighbours.isEmpty())
                break;

            Edge<V> edge = neighbours.get(random.nextInt(neighbours.size()));
            path.add(edge);
            cost += edge.weight;
            current = edge.end;
        }
        return failure(iterations);
    }

}


