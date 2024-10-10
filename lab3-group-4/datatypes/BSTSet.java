package datatypes;

public class BSTSet<Elem extends Comparable<Elem>> extends Set<Elem> {
    public BSTSet() {
        super(new BSTMap<>());
    }
}
