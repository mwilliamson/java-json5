package org.zwobble.json5.values;

public final class Json5String implements Json5Value {
    private final String value;

    public Json5String(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
