package org.zwobble.json5.paths;

/**
 * The JSONPath to a specific value in a JSON5 document.
 */
public final class Json5Path {
    public static final Json5Path ROOT = new Json5Path("$");

    private final String path;

    private Json5Path(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return path;
    }

    /**
     * Create a new {@code JSON5Path} to the value of the member with the given name.
     *
     * @param memberName The name of the member.
     * @return A new {@code JSON5Path}.
     */
    public Json5Path member(String memberName) {
        // TODO: handle member names that require escaping
        return new Json5Path(String.format("%s.%s", this.path, memberName));
    }

    public Json5Path index(int index) {
        return new Json5Path(String.format("%s[%s]", this.path, index));
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (other == null) {
            return false;
        }

        if (!(other instanceof Json5Path otherJson5Path)) {
            return false;
        }

        return this.path.equals(otherJson5Path.path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
