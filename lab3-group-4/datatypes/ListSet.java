package datatypes;

public class ListSet<Elem> extends Set<Elem> {
    public ListSet() {
        super(new ListMap<>());
    }
}
