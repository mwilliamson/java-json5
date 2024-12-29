package org.zwobble.json5.values;

public final class Json5Boolean implements Json5Value {
    private final boolean value;

    public Json5Boolean(boolean value) {
        this.value = value;
    }

    public boolean value() {
        return value;
    }
}
