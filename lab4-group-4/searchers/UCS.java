package searchers;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

import graphtypes.Edge;
import graphtypes.Graph;

/**
 * Uniform-cost search for finding the shortest path.
 */
public class UCS<V> extends Query<V> {

    public UCS(Graph<V> graph, V start, V goal) {
        super(graph, start, goal);
    }

    /**
     * Entries of the priority queue.
     */
    public class UCSEntry {
        public final V node;
        public final Edge<V> lastEdge;      // null for the starting entry
        public final UCSEntry backPointer;  // null for the starting entry
        public final double costToHere;

        UCSEntry(V node, Edge<V> lastEdge, UCSEntry backPointer, double costToHere) {
            this.node = node;
            this.lastEdge = lastEdge;
            this.backPointer = backPointer;
            this.costToHere = costToHere;
        }
    }

    /**
     * Uniform cost search for a path in `graph` from `start` to `goal`.
     * Returns the search result (which includes the path found if successful).
     */
    @Override
    public Result search() {
        int iterations = 0;
        PriorityQueue<UCSEntry> pqueue = new PriorityQueue<>(Comparator.comparingDouble(entry -> entry.costToHere));

        // TODO: Replace these lines with your solution!
        // Notes:
        // * Use the attributes `graph`, `start`, `goal`.
        // * Increment `iterations` every time you remove an entry from the priority queue.
        // * Return one of the following search results:
        //   - `success(cost, path, iterations)`,
        //   - `failure(iterations)` if no path found.
        //   See the parent class SearchQuery for these methods.

        HashSet<V> visited = new HashSet<>();
        UCSEntry firstNode= new UCSEntry(start,null,null,pqueue.peek()!=null?pqueue.peek().costToHere:0);
        pqueue.offer(firstNode);
        while (!pqueue.isEmpty()){
            UCSEntry current = pqueue.poll();
            iterations++;

                 if (!visited.contains(current.node)){
                      visited.add(current.node);
                     if (current.node.equals(goal)){
                         return   success(current.costToHere,extractPath(current),iterations);
                     }
                      for (Edge<V> edge:graph.outgoingEdges(current.node)){

                          double costUntilHere=current.costToHere+ edge.weight;
                           UCSEntry next = new UCSEntry(edge.end,edge,current,costUntilHere);
                      if(!visited.contains(next.node)){
                          pqueue.add(next);

                         }
                     }

                }

        }
        return failure(iterations);
    }

    /**
     * Extracts the path from the start to the current priority queue entry.
     */
    public List<Edge<V>> extractPath(UCSEntry entry) {
        // TODO: Replace these lines with your solution!
        List<Edge<V>> edges =new ArrayList<>();
        while (entry!=null){
            if (entry.lastEdge!=null){
                edges.add(entry.lastEdge);
            }
            entry=entry.backPointer;
        }
        Collections.reverse(edges);

        return edges;
    }

}

