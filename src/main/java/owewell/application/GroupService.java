package owewell.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import owewell.domain.Expense;
import owewell.domain.ExpenseEntry;
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

    public GroupId createGroup(String name, Set<UserId> otherMembers, UserId creator) {
        Set<UserId> members = new LinkedHashSet<>();
        members.add(Objects.requireNonNull(creator, "creator is required"));
        if (otherMembers != null) {
            members.addAll(otherMembers);
        }
        GroupId id = new GroupId(UUID.randomUUID().toString());
        groups.save(new Group(id, name, members));
        return id;
    }

    public void addMember(GroupId groupId, UserId actor, UserId newMember) {
        Group group = findForMember(groupId, actor);
        group.addMember(newMember);
        groups.save(group);
    }

    public ExpenseId addExpense(GroupId groupId, UserId actor, String description,
                                UserId paidBy, Money total, SplitStrategy split) {
        Group group = findForMember(groupId, actor);
        ExpenseId expenseId = group.addExpense(Expense.withSplit(description, paidBy, total, split));
        groups.save(group);
        return expenseId;
    }

    public void voidExpense(GroupId groupId, UserId actor, ExpenseId expenseId) {
        Group group = findForMember(groupId, actor);
        group.voidExpense(expenseId);
        groups.save(group);
    }

    public void recordPayment(GroupId groupId, UserId actor, Payment payment) {
        Group group = findForMember(groupId, actor);
        group.recordPayment(payment);
        groups.save(group);
    }

    // Any member can delete a group, but only once everyone is settled up,
    // so nobody's debt disappears. It's a soft delete: the history is kept.
    public void deleteGroup(GroupId groupId, UserId actor) {
        Group group = findForMember(groupId, actor);
        boolean everyoneSettled = group.balances().values().stream()
            .allMatch(balance -> balance.cents() == 0);
        if (!everyoneSettled) {
            throw new GroupNotSettledException(groupId);
        }
        groups.markDeleted(groupId);
    }

    public Map<UserId, Money> balances(GroupId groupId, UserId actor) {
        return findForMember(groupId, actor).balances();
    }

    public List<Payment> suggestSettlement(GroupId groupId, UserId actor) {
        return findForMember(groupId, actor).suggestSettlement(settlement);
    }

    public List<GroupSummary> groupsFor(UserId actor) {
        return groups.findSummariesForMember(actor);
    }

    public Group group(GroupId groupId, UserId actor) {
        return findForMember(groupId, actor);
    }

    public List<ExpenseEntry> expenseHistory(GroupId groupId, UserId actor) {
        return findForMember(groupId, actor).expenseHistory();
    }

    // Loads the group, and refuses if the person asking isn't a member
    private Group findForMember(GroupId id, UserId actor) {
        Group group = groups.findById(id).orElseThrow(() -> new GroupNotFoundException(id));
        if (!group.members().contains(actor)) {
            throw new NotAGroupMemberException(id, actor);
        }
        return group;
    }
}
