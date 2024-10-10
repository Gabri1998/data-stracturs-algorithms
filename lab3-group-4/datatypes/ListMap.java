package datatypes;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * A map implemented as an unsorted list.
 * This is a very stupid idea and should not be used in practice!
 */
public class ListMap<Key, Value> extends Map<Key, Value> {
    ArrayList<KVPair> elements;

    public ListMap() {
        this(() -> null);
    }

    public ListMap(Supplier<Value> defaultValueSupplier) {
        super(defaultValueSupplier);
        this.elements = new ArrayList<>();
    }

    @Override
    public int size() {
        return this.elements.size();
    }

    @Override
    public Value get(Key key) {
        if (key == null)
            throw new NullPointerException("argument must not be null");

        for (KVPair pair : this.elements)
            if (pair.key.equals(key))
                return pair.value;

        // If the key does not exist, generate a default value and associate with the key:
        Value value = this.defaultValueSupplier.get();
        if (value != null)
            this.elements.add(new KVPair(key, value));
        return value;
    }

    @Override
    public void put(Key key, Value value) {
        if (key == null) throw new NullPointerException("argument must not be null");
        for (KVPair pair : this.elements) {
            if (pair.key.equals(key)) {
                pair.value = value;
                return;
            }
        }
        this.elements.add(new KVPair(key, value));
    }

    @Override
    public Iterator<Key> iterator() {
        return this.elements.stream().map((p) -> p.key).iterator();
    }

    @Override
    public String toString() {
        if (this.isEmpty()) return "ListMap (empty)";
        return String.format("ListMap (size %d)", this.size());
    }

    ///////////////////////////////////////////////////////////////////////////
    // Validation

    @Override
    public void validate() {
        boolean duplicateKeys = this.size() != this.elements.stream().distinct().count();
        if (duplicateKeys)
            throw new IllegalArgumentException(String.format("List map contains duplicate keys: %s", this));
    };

    ///////////////////////////////////////////////////////////////////////////
    // Simple key-value pairs, only for internal use

    private class KVPair {
        Key key;
        Value value;
        KVPair(Key key, Value value) {
            this.key = key;
            this.value = value;
        }
    }
}
