package owewell.domain;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class GroupTest {

    private final UserId alex = new UserId("alex");
    private final UserId sam = new UserId("sam");
    private final UserId priya = new UserId("priya");
    private final UserId stranger = new UserId("stranger");

    private static Money aud(long cents) {
        return new Money(cents, "AUD");
    }

    private Group baliTrip() {
        return new Group(new GroupId("g1"), "Bali Trip", Set.of(alex, sam, priya));
    }

    @Test
    void createsAGroupWithMembers() {
        Group group = baliTrip();

        assertEquals("Bali Trip", group.name());
        assertEquals(Set.of(alex, sam, priya), group.members());
    }

    @Test
    void rejectsABlankName() {
        assertThrows(IllegalArgumentException.class,
            () -> new Group(new GroupId("g1"), "  ", Set.of(alex)));
    }

    @Test
    void rejectsAGroupWithNoMembers() {
        assertThrows(IllegalArgumentException.class,
            () -> new Group(new GroupId("g1"), "Empty", Set.of()));
    }

    @Test
    void addingAnExpenseUpdatesBalances() {
        Group group = baliTrip();
        group.addExpense(Expense.splitEqually("Dinner", alex, aud(9000), List.of(alex, sam, priya)));

        assertEquals(aud(6000), group.balances().get(alex));
        assertEquals(aud(-3000), group.balances().get(sam));
    }

    @Test
    void rejectsAnExpensePaidByANonMember() {
        Group group = baliTrip();

        assertThrows(IllegalArgumentException.class,
            () -> group.addExpense(Expense.splitEqually("Dinner", stranger, aud(9000), List.of(alex, sam))));
    }

    @Test
    void rejectsAnExpenseSharedWithANonMember() {
        Group group = baliTrip();

        assertThrows(IllegalArgumentException.class,
            () -> group.addExpense(Expense.splitEqually("Dinner", alex, aud(9000), List.of(alex, stranger))));
    }

    @Test
    void rejectsAPaymentInvolvingANonMember() {
        Group group = baliTrip();

        assertThrows(IllegalArgumentException.class,
            () -> group.recordPayment(new Payment(stranger, alex, aud(1000))));
    }

    @Test
    void newMembersCanJoinExpenses() {
        Group group = baliTrip();
        UserId jordan = new UserId("jordan");
        group.addMember(jordan);

        group.addExpense(Expense.splitEqually("Taxi", jordan, aud(2000), List.of(jordan, alex)));

        assertEquals(aud(1000), group.balances().get(jordan));
    }

    @Test
    void membersCannotBeChangedFromOutside() {
        Group group = baliTrip();

        assertThrows(UnsupportedOperationException.class,
            () -> group.members().add(stranger));
    }

    @Test
    void suggestedSettlementSettlesEveryone() {
        Group group = baliTrip();
        group.addExpense(Expense.splitEqually("Dinner", alex, aud(9000), List.of(alex, sam, priya)));
        group.addExpense(Expense.splitEqually("Taxi", sam, aud(3000), List.of(alex, sam, priya)));

        for (Payment p : group.suggestSettlement(new AutoSettlement())) {
            group.recordPayment(p);
        }

        for (Money balance : group.balances().values()) {
            assertTrue(balance.isZero());
        }
    }
}
