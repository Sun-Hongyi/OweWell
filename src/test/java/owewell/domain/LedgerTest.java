package owewell.domain;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class LedgerTest {

    private final UserId alex = new UserId("alex");
    private final UserId sam = new UserId("sam");
    private final UserId priya = new UserId("priya");

    private static Money aud(long cents) {
        return new Money(cents, "AUD");
    }

    private Expense dinner() {
        return Expense.splitEqually("Dinner", alex, aud(9000), List.of(alex, sam, priya));
    }

    @Test
    void addingAnExpenseUpdatesBalances() {
        Ledger ledger = new Ledger();
        ledger.addExpense(dinner());

        Map<UserId, Money> balances = ledger.balances();

        assertEquals(aud(6000), balances.get(alex));
        assertEquals(aud(-3000), balances.get(sam));
        assertEquals(aud(-3000), balances.get(priya));
    }

    @Test
    void recordingAPaymentMovesBalancesTowardZero() {
        Ledger ledger = new Ledger();
        ledger.addExpense(dinner());
        ledger.recordPayment(new Payment(sam, alex, aud(3000)));   // Sam pays Alex back

        Map<UserId, Money> balances = ledger.balances();

        assertEquals(aud(3000), balances.get(alex));
        assertTrue(balances.get(sam).isZero());
        assertEquals(aud(-3000), balances.get(priya));
    }

    @Test
    void voidingAnExpenseRemovesItsEffect() {
        Ledger ledger = new Ledger();
        ExpenseId id = ledger.addExpense(dinner());
        ledger.voidExpense(id);

        assertTrue(ledger.balances().isEmpty());
    }

    @Test
    void voidedExpenseStaysInTheHistory() {
        Ledger ledger = new Ledger();
        ExpenseId id = ledger.addExpense(dinner());
        ledger.voidExpense(id);

        List<LedgerEvent> events = ledger.events();
        assertEquals(2, events.size());
        assertInstanceOf(LedgerEvent.ExpenseAdded.class, events.get(0));
        assertInstanceOf(LedgerEvent.ExpenseVoided.class, events.get(1));
    }

    @Test
    void cannotVoidAnUnknownExpense() {
        Ledger ledger = new Ledger();
        assertThrows(IllegalArgumentException.class,
            () -> ledger.voidExpense(new ExpenseId("e99")));
    }

    @Test
    void cannotVoidTheSameExpenseTwice() {
        Ledger ledger = new Ledger();
        ExpenseId id = ledger.addExpense(dinner());
        ledger.voidExpense(id);

        assertThrows(IllegalArgumentException.class, () -> ledger.voidExpense(id));
    }

    @Test
    void recordingTheSuggestedPaymentsSettlesEveryone() {
        Ledger ledger = new Ledger();
        ledger.addExpense(dinner());
        ledger.addExpense(Expense.splitEqually("Taxi", sam, aud(3000), List.of(alex, sam, priya)));

        List<Payment> suggested = new AutoSettlement().settle(ledger.balances());
        for (Payment p : suggested) {
            ledger.recordPayment(p);
        }

        for (Money balance : ledger.balances().values()) {
            assertTrue(balance.isZero());
        }
    }
}
