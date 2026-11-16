package owewell.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Ledger {

    private final List<LedgerEvent> events = new ArrayList<>();
    private int nextExpenseNumber = 1;
    private int loadedEventCount = 0;

    public ExpenseId addExpense(Expense expense) {
        ExpenseId id = new ExpenseId("e" + nextExpenseNumber++);
        events.add(new LedgerEvent.ExpenseAdded(id, expense));
        return id;
    }

    public void voidExpense(ExpenseId id) {
        if (!activeExpenses().containsKey(id)) {
            throw new IllegalArgumentException("No active expense with id " + id.value());
        }
        events.add(new LedgerEvent.ExpenseVoided(id));
    }

    public void recordPayment(Payment payment) {
        events.add(new LedgerEvent.PaymentRecorded(payment));
    }

    public List<LedgerEvent> events() {
        return List.copyOf(events);
    }

    public Map<UserId, Money> balances() {
        Map<UserId, Money> balances = Balances.calculate(new ArrayList<>(activeExpenses().values()));

        for (LedgerEvent event : events) {
            if (event instanceof LedgerEvent.PaymentRecorded recorded) {
                Payment p = recorded.payment();
                balances.merge(p.from(), p.amount(), Money::add);           // payer owes less
                balances.merge(p.to(), p.amount().negate(), Money::add);    // receiver is owed less
            }
        }
        return balances;
    }

    // Replays the events to find which expenses are still active (added and not voided)
    private Map<ExpenseId, Expense> activeExpenses() {
        Map<ExpenseId, Expense> active = new LinkedHashMap<>();
        for (LedgerEvent event : events) {
            switch (event) {
                case LedgerEvent.ExpenseAdded added -> active.put(added.id(), added.expense());
                case LedgerEvent.ExpenseVoided voided -> active.remove(voided.id());
                case LedgerEvent.PaymentRecorded recorded -> { }   // payments don't change expenses
            }
        }
        return active;
    }

    public static Ledger fromEvents(List<LedgerEvent> history) {
        Ledger ledger = new Ledger();
        for (LedgerEvent event : history) {
            ledger.events.add(event);
            if (event instanceof LedgerEvent.ExpenseAdded) {
                ledger.nextExpenseNumber++;
            }
        }
        ledger.loadedEventCount = history.size();
        return ledger;
    }

    public int loadedEventCount() {
        return loadedEventCount;
    }
}
