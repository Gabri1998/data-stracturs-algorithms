package searchers;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

import graphtypes.Edge;
import graphtypes.Graph;

/**
 * The A* algorithm for finding the shortest path.
 */
public class Astar<V> extends UCS<V> {

    public Astar(Graph<V> graph, V start, V goal) {
        super(graph, start, goal);
    }

    /**
     * Entries of the priority queue.
     * This inherits all instance variables from `UCSEntry`, plus the ones you add.
     */
    public class AstarEntry extends UCSEntry {
        // These are inherited from UCSEntry:
        // public final V node;
        // public final Edge<V> lastEdge;      // null for the starting entry
        // public final UCSEntry backPointer;  // null for the starting entry
        // public final double costToHere;

        // TODO: Add new fields here:
        public final double totalCost;

        AstarEntry(V node, Edge<V> lastEdge, AstarEntry backPointer, double costToHere
                   // TODO: Extend the parameter list
                ,double totalCost
        ) {
            super(node, lastEdge, backPointer, costToHere);
            // TODO: Initialise the entry
            this.totalCost=totalCost;
        }
    }

    /** 
     * Runs the A* algorithm to find a path in `graph` from `start` to `goal`.
     * Returns the search result (which includes the path found if successful).
     */
    public Result search() {

        int iterations = 0;
        PriorityQueue<AstarEntry> pqueue = new PriorityQueue<>(Comparator.comparingDouble(entry -> entry.totalCost));
        // TODO: Replace these lines with your solution!
        // Notes:
        // * You can start from your implementation of UCS (but using AstarEntry now).
        // * Don't forget to initialise the priority queue with a comparator.
        // * Increment `iterations` every time you remove an entry from the priority queue.

        HashSet<V> visited = new HashSet<>();
        AstarEntry firstNode= new AstarEntry(start,null,null,pqueue.peek()!=null?pqueue.peek().costToHere:0,0); //change
        pqueue.offer(firstNode);
        while (!pqueue.isEmpty()){
            AstarEntry current = pqueue.remove(); //change
            iterations++;


            if (!visited.contains(current.node)){
                visited.add(current.node);
                if (current.node.equals(goal)){
                    return   success(current.costToHere,extractPath(current),iterations);
                }
                for (Edge<V> edge:graph.outgoingEdges(current.node)){

                    double costUntilHere=current.costToHere+ edge.weight;
                    double currentTotalCost= current.costToHere+ edge.weight+ graph.guessCost(edge.end, goal)  ;
                    AstarEntry next = new AstarEntry(edge.end,edge,current,costUntilHere,currentTotalCost ); // change
                    if(!visited.contains(next.node)){
                        pqueue.add(next);

                    }
                }

            }

        }
        return failure(iterations);
    }

}


