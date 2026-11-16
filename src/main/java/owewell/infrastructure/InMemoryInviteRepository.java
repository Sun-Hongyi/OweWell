package owewell.infrastructure;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import owewell.application.InviteRepository;
import owewell.domain.Invite;

public class InMemoryInviteRepository implements InviteRepository {

    private final Map<String, Invite> invites = new ConcurrentHashMap<>();

    @Override
    public void save(Invite invite) {
        invites.put(invite.tokenHash(), invite);
    }

    @Override
    public Optional<Invite> findByTokenHash(String tokenHash) {
        return Optional.ofNullable(invites.get(tokenHash));
    }
}
