package owewell.domain;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Group {

    private final GroupId id;
    private final String name;
    private final Set<UserId> members;
    private final Ledger ledger;

    public Group(GroupId id, String name, Set<UserId> members) {
        this(id, name, members, new Ledger());
    }

    private Group(GroupId id, String name, Set<UserId> members, Ledger ledger) {
        this.id = Objects.requireNonNull(id, "id is required");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Group name cannot be blank");
        }
        if (members == null || members.isEmpty()) {
            throw new IllegalArgumentException("A group needs at least one member");
        }
        this.name = name;
        this.members = new LinkedHashSet<>(members);
        this.ledger = ledger;
    }

    public static Group restore(GroupId id, String name, Set<UserId> members,
                                List<LedgerEvent> history) {
        return new Group(id, name, members, Ledger.fromEvents(history));
    }

    public GroupId id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Set<UserId> members() {
        return Collections.unmodifiableSet(members);
    }

    public void addMember(UserId user) {
        members.add(Objects.requireNonNull(user, "user is required"));
    }

    public ExpenseId addExpense(Expense expense) {
        requireMember(expense.paidBy());
        for (UserId person : expense.shares().keySet()) {
            requireMember(person);
        }
        return ledger.addExpense(expense);
    }

    public void voidExpense(ExpenseId id) {
        ledger.voidExpense(id);
    }

    public void recordPayment(Payment payment) {
        requireMember(payment.from());
        requireMember(payment.to());
        ledger.recordPayment(payment);
    }

    public Map<UserId, Money> balances() {
        return ledger.balances();
    }

    public List<Payment> suggestSettlement(SettlementAlgorithm algorithm) {
        return algorithm.settle(balances());
    }

    public List<LedgerEvent> history() {
        return ledger.events();
    }

    public int version() {
        return ledger.loadedEventCount();
    }

    private void requireMember(UserId user) {
        if (!members.contains(user)) {
            throw new IllegalArgumentException(user.value() + " is not a member of " + name);
        }
    }
}
