package org.zwobble.json5.values;

public final class Json5Number implements Json5Value {
    private final String value;

    public Json5Number(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
