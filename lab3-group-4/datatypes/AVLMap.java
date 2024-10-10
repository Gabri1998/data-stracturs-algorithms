package datatypes;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * A map implemented as an AVL tree.
 * This inherits most functionality from BST, the only difference is
 * that it rebalances the tree when adding new tree nodes.
 */
public class AVLMap<Key extends Comparable<Key>, Value> extends BSTMap<Key, Value> {

    public AVLMap() {
        this(() -> null);
    }

    public AVLMap(Supplier<Value> defaultValueSupplier) {
        super(defaultValueSupplier);
        this.root = null;
    }

    protected Node putHelper(Node node, Key key, Value value) {
        // Call `putHelper` for normal BSTs, to insert the value into the tree,
        // and update the node to be the new parent.
        node = super.putHelper(node, key, value);

        // Calculate the balance for the node.
        int nodeBalance = height(node.left) - height(node.right);

        //---------- TASK 4a: Implement AVL rebalancing ---------------------//
        // Perform balance rotations if needed.
        // You do this by calling `leftRotate` and `rightRotate`.
        // Remember to return the new parent node if you balance the tree.

        // TODO: Replace these lines with your solution!
        if (nodeBalance>1&&key.compareTo(node.left.key)<0){
            return rightRotate(node) ;}
        if (nodeBalance<-1&&key.compareTo(node.right.key)>0){
            return leftRotate(node);}
        if (nodeBalance>1&&key.compareTo(node.left.key)>0){
           node.left=leftRotate(node.left);
            return rightRotate(node);}
        if (nodeBalance<-1&&key.compareTo(node.right.key)<0){
            node.right=rightRotate(node.right);
            return leftRotate(node);}



        //if (true) throw new UnsupportedOperationException();
        //---------- END TASK 4a -------------------------------------------//

        return node;
    }

    private Node rightRotate(Node parent) {
        // The left child will be the new parent.
        Node child = parent.left;
        // This must exist, otherwise we cannot rotate right.
        if (child == null)
            throw new NullPointerException("There must be a left child");

        //---------- TASK 4b: Implement right rotation ----------------------//
        // Don't forget to update the size and height for all nodes that need it.
        // You do this by calling the method `updateSizeAndHeight()`.

        // TODO: Replace these lines with your solution!
        Node temp= child.right;
        child.right=parent;
        parent.left=temp;

        parent.updateSizeAndHeight();
        child.updateSizeAndHeight();




        //if (true) throw new UnsupportedOperationException();
        //---------- END TASK 4b --------------------------------------------//

        // Return the new parent.
        return child;
    }

    private Node leftRotate(Node parent) {
        //---------- TASK 4c: Implement left rotation -----------------------//
        // Don't forget to:
        //  1. Update the size and height for all nodes that need it.
        //     You do this by calling the method 'update_size_and_height()`.
        //  2. Return the new parent node after rotation.
        // The left child will be the new parent.
        // TODO: Replace these lines with your solution!
        Node child = parent.right;
        // This must exist, otherwise we cannot rotate right.
        if (child == null)
            throw new NullPointerException("There must be a right child");




        Node temp= child.left;
        child.left=parent;
        parent.right=temp;

        parent.updateSizeAndHeight();
        child.updateSizeAndHeight();




        //if (true) throw new UnsupportedOperationException();

        // Return the new parent.
        return child;
        //---------- END TASK 4c --------------------------------------------//
    }


    ///////////////////////////////////////////////////////////////////////////
    // Validation

    @Override
    public void validate() {
        super.validate();
        this.validateBalance(this.root);
    }

    private void validateBalance(Node node) {
        if (node == null)
            return;

        int nodeBalance = height(node.left) - height(node.right);
        if (!(-1 <= nodeBalance && nodeBalance <= 1))
            throw new IllegalArgumentException(String.format(
                "Node '%s:%s' not properly balanced: balance is %d, not -1, 0 or 1",
                node.key, node.value, nodeBalance
            ));
        this.validateBalance(node.left);
        this.validateBalance(node.right);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Test the AVL tree: Change this code however you want.

    public static void main(String[] args) throws IOException {
        System.out.println("# A very simple tree.");
        AVLMap<Integer, String> tree = new AVLMap<>();
        tree.put(2, "dog"); tree.validate();
        tree.put(3, "sat"); tree.validate();
        tree.put(5, "the"); tree.validate();
        tree.put(6, "mat"); tree.validate();
        tree.put(4, "on");  tree.validate();
        tree.put(2, "cat"); tree.validate();
        tree.put(1, "the"); tree.validate();
        for (int k = 0; k < 8; k++) {
            System.out.format("%d --> %s\n", k, tree.get(k));
        }
        System.out.println(tree.show(5));
        System.out.println();

        System.out.println("# A tree where the values are mutable lists.");
        AVLMap<Integer, ArrayList<String>> tree2 = new AVLMap<>(() -> new ArrayList<>());
        tree2.get(1).add("the");
        tree2.get(3).add("sat");
        tree2.get(9).add("the");
        tree2.get(9).add("mat");
        tree2.get(3).add("on");
        tree2.get(1).add("cat");
        tree2.validate();
        for (int k : tree2) {
            System.out.format("%d --> %s\n", k, tree2.get(k));
        }
        System.out.println(tree2.show(5));
        if (tree2.size() != 3)
            throw new IllegalArgumentException(String.format("Wrong tree size: %d, but it should be 3", tree2.size()));
        System.out.println();

        /*
        // Wait with this until you're pretty certain that your code works.
        System.out.println("# A larger tree, testing performance.");
        AVLMap<Integer, Integer> tree3 = new AVLMap<>();
        List<Integer> numbers = IntStream.range(0, 5_000).boxed().collect(Collectors.toList());

        // Comment this to get an unbalanced tree:
        Collections.shuffle(numbers);

        for (int k : numbers) {
            tree3.put(k, k*k);  // Map a number to its square
        }
        tree3.validate();
        System.out.println(tree3.show(2));
        for (int k : numbers) {
            if (k*k != tree3.get(k))
                throw new IllegalArgumentException(String.format("%d --> %d", k, tree3.get(k)));
        }
        System.out.println();
        */
    }
}
