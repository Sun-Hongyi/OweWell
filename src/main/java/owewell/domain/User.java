package owewell.domain;

import java.util.Objects;

public record User(UserId id, String name, String email) {
    public User {
        Objects.requireNonNull(id, "id is required");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name cannot be blank");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("User email cannot be blank");
        }
    }
}
