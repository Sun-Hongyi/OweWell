package owewell.application;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import owewell.domain.AutoSettlement;
import owewell.domain.EqualSplit;
import owewell.domain.ExpenseId;
import owewell.domain.GroupId;
import owewell.domain.Money;
import owewell.domain.Payment;
import owewell.domain.UserId;
import owewell.infrastructure.InMemoryGroupRepository;

class GroupServiceTest {

    private final UserId alex = new UserId("alex");
    private final UserId sam = new UserId("sam");
    private final UserId priya = new UserId("priya");
    private final UserId stranger = new UserId("stranger");

    private GroupService service;

    @BeforeEach
    void setUp() {
        service = new GroupService(new InMemoryGroupRepository(), new AutoSettlement());
    }

    private static Money aud(long cents) {
        return new Money(cents, "AUD");
    }

    @Test
    void creatorIsAlwaysAMember() {
        GroupId bali = service.createGroup("Bali Trip", Set.of(), alex);

        service.addExpense(bali, alex, "Snacks", alex, aud(1000), new EqualSplit(List.of(alex)));

        assertTrue(service.balances(bali, alex).containsKey(alex));
    }

    @Test
    void createGroupAndAddAnExpense() {
        GroupId bali = service.createGroup("Bali Trip", Set.of(sam, priya), alex);

        service.addExpense(bali, alex, "Dinner", alex, aud(9000), new EqualSplit(List.of(alex, sam, priya)));

        assertEquals(aud(6000), service.balances(bali, alex).get(alex));
        assertEquals(aud(-3000), service.balances(bali, sam).get(sam));
    }

    @Test
    void nonMembersCannotSeeOrChangeAGroup() {
        GroupId bali = service.createGroup("Bali Trip", Set.of(sam), alex);

        assertThrows(NotAGroupMemberException.class, () -> service.balances(bali, stranger));
        assertThrows(NotAGroupMemberException.class,
            () -> service.addExpense(bali, stranger, "Sneaky", alex, aud(100), new EqualSplit(List.of(alex))));
    }

    @Test
    void groupsAreKeptSeparate() {
        GroupId bali = service.createGroup("Bali Trip", Set.of(sam), alex);
        GroupId house = service.createGroup("Share House", Set.of(sam), alex);

        service.addExpense(bali, alex, "Dinner", alex, aud(2000), new EqualSplit(List.of(alex, sam)));

        assertEquals(aud(1000), service.balances(bali, alex).get(alex));
        assertTrue(service.balances(house, alex).isEmpty());
    }

    @Test
    void unknownGroupThrowsGroupNotFound() {
        assertThrows(GroupNotFoundException.class,
            () -> service.balances(new GroupId("does-not-exist"), alex));
    }

    @Test
    void voidingAnExpenseRemovesItsEffect() {
        GroupId bali = service.createGroup("Bali Trip", Set.of(sam), alex);
        ExpenseId dinner = service.addExpense(bali, alex, "Dinner", alex, aud(2000),
            new EqualSplit(List.of(alex, sam)));

        service.voidExpense(bali, alex, dinner);

        assertTrue(service.balances(bali, alex).isEmpty());
    }

    @Test
    void settleUpEndToEnd() {
        GroupId bali = service.createGroup("Bali Trip", Set.of(sam, priya), alex);
        service.addExpense(bali, alex, "Dinner", alex, aud(9000), new EqualSplit(List.of(alex, sam, priya)));
        service.addExpense(bali, sam, "Taxi", sam, aud(3000), new EqualSplit(List.of(alex, sam, priya)));

        for (Payment p : service.suggestSettlement(bali, alex)) {
            service.recordPayment(bali, alex, p);
        }

        for (Money balance : service.balances(bali, alex).values()) {
            assertTrue(balance.isZero());
        }
    }
}
