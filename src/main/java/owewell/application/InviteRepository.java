package owewell.application;

import java.util.Optional;

import owewell.domain.Invite;

public interface InviteRepository {
    void save(Invite invite);
    Optional<Invite> findByTokenHash(String tokenHash);
}
