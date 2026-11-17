package owewell.application;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import owewell.domain.EqualSplit;
import owewell.domain.GreedySettlement;
import owewell.domain.GroupId;
import owewell.domain.Money;
import owewell.domain.Payment;
import owewell.domain.UserId;
import owewell.infrastructure.InMemoryGroupRepository;

class GroupDeletionTest {

    private final GroupService service =
        new GroupService(new InMemoryGroupRepository(), new GreedySettlement());

    private final UserId alice = new UserId("alice");
    private final UserId bob = new UserId("bob");
    private final UserId eve = new UserId("eve");

    @Test
    void aMemberCanDeleteASettledGroup() {
        GroupId id = service.createGroup("Trip", Set.of(bob), alice);

        service.deleteGroup(id, alice);

        assertThrows(GroupNotFoundException.class, () -> service.group(id, alice));
        assertTrue(service.groupsFor(alice).isEmpty());
        assertTrue(service.groupsFor(bob).isEmpty());
    }

    @Test
    void refusesWhileSomeoneStillOwesMoney() {
        GroupId id = service.createGroup("Trip", Set.of(bob), alice);
        service.addExpense(id, alice, "Dinner", alice,
            new Money(5000, "AUD"), new EqualSplit(List.of(alice, bob)));

        assertThrows(GroupNotSettledException.class, () -> service.deleteGroup(id, alice));
        assertEquals("Trip", service.group(id, alice).name());   // still there
    }

    @Test
    void canDeleteOnceEveryoneHasPaidBack() {
        GroupId id = service.createGroup("Trip", Set.of(bob), alice);
        service.addExpense(id, alice, "Dinner", alice,
            new Money(5000, "AUD"), new EqualSplit(List.of(alice, bob)));
        service.recordPayment(id, bob, new Payment(bob, alice, new Money(2500, "AUD")));

        service.deleteGroup(id, bob);

        assertTrue(service.groupsFor(alice).isEmpty());
    }

    @Test
    void nonMembersCannotDeleteAGroup() {
        GroupId id = service.createGroup("Trip", Set.of(bob), alice);

        assertThrows(NotAGroupMemberException.class, () -> service.deleteGroup(id, eve));
        assertEquals("Trip", service.group(id, alice).name());   // still there
    }
}
