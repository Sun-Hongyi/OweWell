package owewell.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

import owewell.domain.Group;
import owewell.domain.GroupId;
import owewell.domain.Invite;
import owewell.domain.UserId;

public class InviteService {

    public static final Duration VALID_FOR = Duration.ofDays(7);

    private final GroupRepository groups;
    private final InviteRepository invites;
    private final Clock clock;

    public InviteService(GroupRepository groups, InviteRepository invites, Clock clock) {
        this.groups = Objects.requireNonNull(groups);
        this.invites = Objects.requireNonNull(invites);
        this.clock = Objects.requireNonNull(clock);
    }

    public record CreatedInvite(String token, Instant expiresAt) {}

    public CreatedInvite createInvite(GroupId groupId, UserId actor) {
        Group group = groups.findById(groupId).orElseThrow(() -> new GroupNotFoundException(groupId));
        if (!group.members().contains(actor)) {
            throw new NotAGroupMemberException(groupId, actor);
        }

        String token = InviteTokens.newToken();
        Instant expiresAt = clock.instant().plus(VALID_FOR);
        invites.save(new Invite(InviteTokens.hash(token), groupId, actor, expiresAt));

        return new CreatedInvite(token, expiresAt);   // the raw token is only ever returned here, once
    }

    public GroupId acceptInvite(String token, UserId actor) {
        Invite invite = invites.findByTokenHash(InviteTokens.hash(token))
            .orElseThrow(InviteNotFoundException::new);

        if (invite.isExpiredAt(clock.instant())) {
            throw new InviteExpiredException();
        }

        Group group = groups.findById(invite.groupId())
            .orElseThrow(() -> new GroupNotFoundException(invite.groupId()));
        group.addMember(actor);   // joining twice does nothing, since members are a Set
        groups.save(group);
        return group.id();
    }
}
