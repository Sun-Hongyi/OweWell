package owewell.domain;

import java.time.Instant;
import java.util.Objects;

public record Invite(String tokenHash, GroupId groupId, UserId createdBy, Instant expiresAt) {

    public Invite {
        if (tokenHash == null || tokenHash.isBlank()) {
            throw new IllegalArgumentException("tokenHash is required");
        }
        Objects.requireNonNull(groupId, "groupId is required");
        Objects.requireNonNull(createdBy, "createdBy is required");
        Objects.requireNonNull(expiresAt, "expiresAt is required");
    }

    public boolean isExpiredAt(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
