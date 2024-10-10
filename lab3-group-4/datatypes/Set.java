package datatypes;

import java.util.Iterator;

/**
 * A set implemented as a map from keys to nothing at all.
 * (It's only the keys that are used.)
 *
 * This should really be split into:
 * - an interface Set for sets,
 * - an implementation MapBasedSet.
 * But we were too lazy to add more files.
 */
public class Set<Key> implements Iterable<Key> {
    // There are no good built-in singleton classes in Java,
    // so we use a boolean instead:
    private Map<Key, Boolean> map;

    public Set(Map<Key, Boolean> emptyMap) {
        this.map = emptyMap;
    }

    /**
     * Returns true if there are no elements.
     */
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    /**
     * Returns true if the element is in the set.
     */
    public boolean contains(Key key) {
        return this.map.containsKey(key);
    }

    /**
     * Returns the number of elements.
     */
    public int size() {
        return this.map.size();
    }

    /**
     * Adds the given element, does nothing if it already exists.
     */
    public void add(Key key) {
        this.map.put(key, true);
    }

    /**
     * Returns an iterator over the elements in the set.
     */
    public Iterator<Key> iterator() {
        return this.map.iterator();
    }

    /**
     * Returns a string representation of the set.
     */
    public String toString() {
        return "Set(" + this.map + ")";
    }

    /**
     * Validates that the set is correctly implemented according to the specification.
     * @throws IllegalArgumentException if there is anything wrong.
     */
    public void validate() {
        this.map.validate();
    };
}
