package owewell.application;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import owewell.domain.Expense;
import owewell.domain.ExpenseId;
import owewell.domain.Group;
import owewell.domain.GroupId;
import owewell.domain.Money;
import owewell.domain.Payment;
import owewell.domain.SettlementAlgorithm;
import owewell.domain.SplitStrategy;
import owewell.domain.UserId;

public class GroupService {

    private final GroupRepository groups;
    private final SettlementAlgorithm settlement;

    public GroupService(GroupRepository groups, SettlementAlgorithm settlement) {
        this.groups = Objects.requireNonNull(groups);
        this.settlement = Objects.requireNonNull(settlement);
    }

    public GroupId createGroup(String name, Set<UserId> members) {
        GroupId id = new GroupId(UUID.randomUUID().toString());
        groups.save(new Group(id, name, members));
        return id;
    }

    public void addMember(GroupId groupId, UserId user) {
        Group group = find(groupId);
        group.addMember(user);
        groups.save(group);
    }

    public ExpenseId addExpense(GroupId groupId, String description, UserId paidBy,
                                Money total, SplitStrategy split) {
        Group group = find(groupId);
        ExpenseId expenseId = group.addExpense(Expense.withSplit(description, paidBy, total, split));
        groups.save(group);
        return expenseId;
    }

    public void voidExpense(GroupId groupId, ExpenseId expenseId) {
        Group group = find(groupId);
        group.voidExpense(expenseId);
        groups.save(group);
    }

    public void recordPayment(GroupId groupId, Payment payment) {
        Group group = find(groupId);
        group.recordPayment(payment);
        groups.save(group);
    }

    public Map<UserId, Money> balances(GroupId groupId) {
        return find(groupId).balances();
    }

    public List<Payment> suggestSettlement(GroupId groupId) {
        return find(groupId).suggestSettlement(settlement);
    }

    private Group find(GroupId id) {
        return groups.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
    }
}
