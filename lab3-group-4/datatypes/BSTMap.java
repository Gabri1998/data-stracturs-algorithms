package datatypes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.function.Supplier;

/**
 * A map implemented as a binary search tree.
 */
public class BSTMap<Key extends Comparable<Key>, Value> extends Map<Key, Value> {
    Node root;

    public BSTMap() {
        this(() -> null);
    }

    public BSTMap(Supplier<Value> defaultValueSupplier) {
        super(defaultValueSupplier);
        this.root = null;
    }


    /**
     * A generic class for BST nodes (and subclasses).
     * We include the instance variables `size` and `height` for efficiency reasons
     * (`height` is needed to calculate the balance in AVL trees).
     *
     * A BST is given by a node.
     * Note that the node can be null, in which case the tree is empty.
     * We provide methods size and height below this inner class that deal with that case.
     */
    public class Node {
        Key key;
        Value value;
        Node left, right;
        int size;
        int height;

        Node(Key key, Value value) {
            this.key = key;
            this.value = value;
            this.left = this.right = null;
            this.updateSizeAndHeight();
        }

        /**
         * Makes sure that the `size` and `height` instance variables are correct.
         * This assumes that the size and height for the children are already correct,
         * so if you're changing several nodes you have to update them bottom-up
         * (first the children, then the parents).
         */
        public void updateSizeAndHeight() {
            this.size = 1 + size(left) + size(right);
            this.height = 1 + Math.max(height(left), height(right));
        }
    }

    /**
     * Returns the size of a tree, possibly empty (null).
     */
    public int size(Node node) {
        if (node == null)
            return 0;

        return node.size;
    }

    /**
     * Returns the height of a tree, possibly empty (null).
     */
    public int height(Node node) {
        if (node == null)
            return 0;

        return node.height;
    }


    @Override
    public int size() {
        return size(this.root);
    }

    public int height() {
        return height(this.root);
    }

    @Override
    public Value get(Key key) {
        if (key == null)
            throw new NullPointerException("argument must not be null");

        Value value = this.getHelper(this.root, key);
        if (value == null) {
            // If the key does not exist, generate a default value and associate with the key:
            value = this.defaultValueSupplier.get();
            if (value != null)
                this.put(key, value);
        }
        return value;
    }

    protected Value getHelper(Node node, Key key) {
        //---------- TASK 3a: Implement BST get -----------------------------//
        // Note 1: you can implement this method using iteration or recursion
        // Note 2: if the key does not exist, you should return null

        if (node == null) return null;

        int keyComp = node.key.compareTo(key);

        if (keyComp == 0) {
            return node.value;
        }

        if (keyComp > 0) {
            return getHelper(node.left, key);
        }

        return getHelper(node.right, key);
        //---------- END TASK 3a --------------------------------------------//
    }

    @Override
    public void put(Key key, Value value) {
        if (key == null)
            throw new NullPointerException("argument must not be null");

        this.root = this.putHelper(this.root, key, value);
    }

    protected Node putHelper(Node node, Key key, Value value) {
        //---------- TASK 3b: Implement BST put -----------------------------//
        // Note 1: you should implement this method using recursion
        // Note 2: if the node is null you should return a new Node

        if (node == null) {
            return new Node(key, value);
        }

        int cmp = key.compareTo(node.key);
        if (cmp == 0) {
            node.value = value;
        } else if (cmp < 0) {
            node.left = putHelper(node.left, key, value);
        } else { // cmp > 0
            node.right = putHelper(node.right, key, value);
        }

        //---------- END TASK 3b --------------------------------------------//

        // We need to make sure that the node `size` and `height` are up-to-date.
        node.updateSizeAndHeight();
        return node;
    }

    @Override
    public Iterator<Key> iterator() {
        return new Iterator<>() {
            Stack<Node> traversal = new Stack<>();
            {
                moveToLeftmostChild(root);
            }
            private void moveToLeftmostChild(Node node) {
                while (node != null) {
                    this.traversal.push(node);
                    node = node.left;
                }
            }
            public boolean hasNext() {
                return !this.traversal.isEmpty();
            }
            public Key next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                Node node = this.traversal.pop();
                if (node.right != null)
                    moveToLeftmostChild(node.right);
                return node.key;
            }
        };
    }


    @Override
    public String toString() {
        String className = this.getClass().getName();
        if (this.isEmpty())
            return className + "(empty)";

        return String.format("%s(size %d, height %d)", className, this.size(), this.height());
    }

    public String show(int maxLevel) {
        return String.format("%s: {%s}", this, this.showNode(this.root, maxLevel));
    }

    protected String showNode(Node node, int maxLevel) {
        if (node == null)
            return "-";
        if (maxLevel <= 0)
            return String.format("[...%d nodes...]", node.size);

        String left = this.showNode(node.left, maxLevel-1);
        String right = this.showNode(node.right, maxLevel-1);
        return String.format("[%s %s:%s %s]", left, node.key, node.value, right);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Validation

    @Override
    public void validate() {
        this.validateBST(this.root, null, null);
        this.validateSize(this.root);
        this.validateHeight(this.root);
    }

    private void validateBST(Node node, Key min, Key max) {
        if (node == null)
            return;

        if (min != null && node.key.compareTo(min) <= 0)
            throw new IllegalArgumentException(String.format(
                    "Node '%s:%s' not in BST order: rightmost left child (%s) >= node (%s)",
                    node.key, node.value, min, node.key
            ));
        if (max != null && max.compareTo(node.key) <= 0)
            throw new IllegalArgumentException(String.format(
                    "Node '%s:%s' not in BST order: leftmost right child (%s) >= node (%s)",
                    node.key, node.value, node.key, max
            ));
        this.validateBST(node.left, min, node.key);
        this.validateBST(node.right, node.key, max);
    }

    private void validateSize(Node node) {
        if (node == null)
            return;

        int calculated = 1 + size(node.left) + size(node.right);
        if (node.size != calculated)
            throw new IllegalArgumentException(String.format(
                    "Subtree size for node '%s:%s' not consistent: stored (%s) != calculated (%s)",
                    node.key, node.value, node.size, calculated
            ));
        this.validateSize(node.left);
        this.validateSize(node.right);
    }

    private void validateHeight(Node node) {
        if (node == null)
            return;

        int calculated = 1 + Math.max(height(node.left), height(node.right));
        if (node.height != calculated)
            throw new IllegalArgumentException(String.format(
                    "Subtree height for node '%s:%s' not consistent: stored (%s) != calculated (%s)",
                    node.key, node.value, node.height, calculated
            ));
        this.validateHeight(node.left);
        this.validateHeight(node.right);
    }


    ///////////////////////////////////////////////////////////////////////////
    // Test the BST: Change this code however you want.

    public static void main(String[] args) throws IOException {
        System.out.println("# A very simple tree.");
        BSTMap<Integer, String> tree = new BSTMap<>();
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
        BSTMap<Integer, ArrayList<String>> tree2 = new BSTMap<>(() -> new ArrayList<>());
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
        BSTMap<Integer, Integer> tree3 = new BSTMap<>();
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
