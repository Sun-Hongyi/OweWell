package owewell.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import owewell.domain.AutoSettlement;
import owewell.domain.GroupId;
import owewell.domain.UserId;
import owewell.infrastructure.InMemoryGroupRepository;
import owewell.infrastructure.InMemoryInviteRepository;

class InviteServiceTest {

    private final UserId alex = new UserId("alex");
    private final UserId jordan = new UserId("jordan");
    private final UserId stranger = new UserId("stranger");

    private final Instant now = Instant.parse("2026-11-16T10:00:00Z");

    private InMemoryGroupRepository groupRepo;
    private InMemoryInviteRepository inviteRepo;
    private GroupService groups;
    private InviteService invites;

    @BeforeEach
    void setUp() {
        groupRepo = new InMemoryGroupRepository();
        inviteRepo = new InMemoryInviteRepository();
        groups = new GroupService(groupRepo, new AutoSettlement());
        invites = invitesAt(now);
    }

    // An InviteService whose clock is frozen at the given time
    private InviteService invitesAt(Instant time) {
        return new InviteService(groupRepo, inviteRepo, Clock.fixed(time, ZoneOffset.UTC));
    }

    @Test
    void memberCanInviteAndFriendCanJoin() {
        GroupId bali = groups.createGroup("Bali Trip", Set.of(), alex);
        String token = invites.createInvite(bali, alex).token();

        GroupId joined = invites.acceptInvite(token, jordan);

        assertEquals(bali, joined);
        assertDoesNotThrow(() -> groups.balances(bali, jordan));   // jordan is now a member
    }

    @Test
    void nonMemberCannotCreateAnInvite() {
        GroupId bali = groups.createGroup("Bali Trip", Set.of(), alex);

        assertThrows(NotAGroupMemberException.class, () -> invites.createInvite(bali, stranger));
    }

    @Test
    void madeUpTokenIsRejected() {
        assertThrows(InviteNotFoundException.class, () -> invites.acceptInvite("made-up-token", jordan));
    }

    @Test
    void inviteStillWorksJustBeforeItExpires() {
        GroupId bali = groups.createGroup("Bali Trip", Set.of(), alex);
        String token = invites.createInvite(bali, alex).token();

        assertDoesNotThrow(() -> invitesAt(now.plus(Duration.ofDays(6))).acceptInvite(token, jordan));
    }

    @Test
    void expiredInviteIsRejected() {
        GroupId bali = groups.createGroup("Bali Trip", Set.of(), alex);
        String token = invites.createInvite(bali, alex).token();

        assertThrows(InviteExpiredException.class,
            () -> invitesAt(now.plus(Duration.ofDays(8))).acceptInvite(token, jordan));
    }

    @Test
    void tokenIsOnlyStoredAsAHash() {
        GroupId bali = groups.createGroup("Bali Trip", Set.of(), alex);
        String token = invites.createInvite(bali, alex).token();

        assertTrue(inviteRepo.findByTokenHash(token).isEmpty());                       // raw token isn't stored
        assertTrue(inviteRepo.findByTokenHash(InviteTokens.hash(token)).isPresent());  // only its hash is
    }

    @Test
    void joiningTwiceIsHarmless() {
        GroupId bali = groups.createGroup("Bali Trip", Set.of(), alex);
        String token = invites.createInvite(bali, alex).token();

        invites.acceptInvite(token, jordan);
        invites.acceptInvite(token, jordan);

        assertEquals(2, groupRepo.findById(bali).orElseThrow().members().size());   // alex and jordan
    }

    @Test
    void tokensAreLongAndUnique() {
        String a = InviteTokens.newToken();
        String b = InviteTokens.newToken();

        assertNotEquals(a, b);
        assertEquals(43, a.length());
    }
}
