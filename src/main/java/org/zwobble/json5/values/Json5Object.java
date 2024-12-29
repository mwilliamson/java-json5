package org.zwobble.json5.values;

import java.util.LinkedHashMap;

public final class Json5Object implements Json5Value {
    private final LinkedHashMap<String, Json5Member> members;

    public Json5Object(LinkedHashMap<String, Json5Member> members) {
        this.members = members;
    }

    public Iterable<Json5Member> members() {
        return members.values();
    }
}
