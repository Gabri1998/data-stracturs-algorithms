package core;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.Objects;

/**
 * A simple class representing a pair of paths.
 */
public class PathPair implements Comparable<PathPair> {
    public final Path path1, path2;

    public PathPair(Path path1, Path path2) {
        this.path1 = path1;
        this.path2 = path2;
    }

    public String toString() {
        return path1.getFileName() + " and " + path2.getFileName();
    }

    public boolean equals(Object obj) {
        if (!(obj != null && this.getClass() == obj.getClass()))
          return false;

        PathPair pair = (PathPair) obj;
        return this.path1.equals(pair.path1) && this.path2.equals(pair.path2);
    }

    public int hashCode() {
        return Objects.hash(path1, path2);
    }

    public int compareTo(PathPair other) {
        return Comparator
            .comparing((PathPair pair) -> pair.path1)
            .thenComparing(pair -> pair.path2)
            .compare(this, other);
    }
}
