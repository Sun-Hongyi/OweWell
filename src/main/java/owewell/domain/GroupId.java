package owewell.domain;

public record GroupId(String value) {
    public GroupId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("GroupId cannot be null or blank");
        }
    }
}
