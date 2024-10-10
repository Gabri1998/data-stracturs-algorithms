package datatypes;

import java.util.function.Supplier;

/**
 * An interface for simple maps (dictionaries).
 * Some methods have default implementations.
 *
 * We have modified it in one important way.
 * The user may specify a default value supplier.
 * If given, it is used to create a mapping when looking up a non-existing key.
 * This would normally be a separate "DefaultMap" interface.
 * But we were too lazy to add more files.
 */
public abstract class Map<Key, Value> implements Iterable<Key> {
    Supplier<Value> defaultValueSupplier;

    public Map(Supplier<Value> defaultValueSupplier) {
        this.defaultValueSupplier = defaultValueSupplier;
    }

    /**
     * Returns true if the map contains no key-value mappings.
     */
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * Returns true if the map contains a mapping for the specified key.
     */
    public boolean containsKey(Key key) {
        return this.get(key) != null;
    }

    /**
     * Returns the number of key-value mappings.
     */
    public abstract int size();

    /**
     * Returns the value associated with the given key.
     * If the key doesn't exist, a new default value is generated and associated with the key.
     */
    public abstract Value get(Key key);

    /**
     * Associates the specified value with the specified key.
     */
    public abstract void put(Key key, Value value);

    /**
     * Show the contents of the map, down to a certain level.
     */
    public String show(int maxLevel) {
        return this.toString();
    }

    /**
     * Validates that the map is correctly implemented according to the specification.
     * @throws IllegalArgumentException if there is anything wrong.
     */
    public abstract void validate();
}
