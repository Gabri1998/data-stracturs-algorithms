package datatypes;

public class AVLSet<Elem extends Comparable<Elem>> extends Set<Elem> {
    public AVLSet() {
        super(new AVLMap<>());
    }
}
