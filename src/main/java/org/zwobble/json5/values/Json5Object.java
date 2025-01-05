package org.zwobble.json5.values;

import org.zwobble.json5.paths.Json5Path;
import org.zwobble.sourcetext.SourceRange;

import java.util.LinkedHashMap;
import java.util.Optional;

public final class Json5Object implements Json5Value {
    private final LinkedHashMap<String, Json5Member> members;
    private final Json5Path path;
    private final SourceRange sourceRange;

    private Json5Object(
        LinkedHashMap<String, Json5Member> members,
        Json5Path path,
        SourceRange sourceRange
    ) {
        this.path = path;
        this.members = members;
        this.sourceRange = sourceRange;
    }

    public Iterable<Json5Member> members() {
        return members.values();
    }

    /**
     * Get the value of the member with the given name, if any.
     *
     * @param memberName The member of the name to find.
     * @return If there is a member with the given name, the value of that
     * member, otherwise an empty {@code Optional}.
     */
    public Optional<Json5Value> getValue(String memberName) {
        return Optional.ofNullable(members.get(memberName)).map(Json5Member::value);
    }

    @Override
    public Json5Path path() {
        return this.path;
    }

    @Override
    public SourceRange sourceRange() {
        return this.sourceRange;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final LinkedHashMap<String, Json5Member> members = new LinkedHashMap<>();

        public Builder addMember(Json5Member member) {
            members.put(member.name().value(), member);
            return this;
        }

        public Json5Object build(Json5Path path, SourceRange sourceRange) {
            return new Json5Object(this.members, path, sourceRange);
        }
    }
}
