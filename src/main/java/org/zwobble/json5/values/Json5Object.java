package org.zwobble.json5.values;

import org.zwobble.json5.sources.Json5SourceRange;

import java.util.LinkedHashMap;

public final class Json5Object implements Json5Value {
    private final LinkedHashMap<String, Json5Member> members;
    private final Json5SourceRange sourceRange;

    private Json5Object(
        LinkedHashMap<String, Json5Member> members,
        Json5SourceRange sourceRange
    ) {
        this.members = members;
        this.sourceRange = sourceRange;
    }

    public Iterable<Json5Member> members() {
        return members.values();
    }

    @Override
    public Json5SourceRange sourceRange() {
        return this.sourceRange;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final LinkedHashMap<String, Json5Member> members = new LinkedHashMap<>();

        private Builder() {}

        public Builder addMember(Json5Member member) {
            members.put(member.name().value(), member);
            return this;
        }

        public Json5Object build(Json5SourceRange sourceRange) {
            return new Json5Object(members, sourceRange);
        }
    }
}
