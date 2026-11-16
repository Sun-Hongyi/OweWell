package owewell.api;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import owewell.application.GroupService;
import owewell.domain.EqualSplit;
import owewell.domain.ExpenseId;
import owewell.domain.GroupId;
import owewell.domain.Money;
import owewell.domain.Payment;
import owewell.domain.UserId;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final GroupService groups;

    public GroupController(GroupService groups) {
        this.groups = groups;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateGroupResponse createGroup(@RequestBody CreateGroupRequest request) {
        Set<UserId> members = request.members().stream()
            .map(UserId::new)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        GroupId id = groups.createGroup(request.name(), members);
        return new CreateGroupResponse(id.value());
    }

    @PostMapping("/{groupId}/expenses")
    @ResponseStatus(HttpStatus.CREATED)
    public AddExpenseResponse addExpense(@PathVariable String groupId,
                                         @RequestBody AddExpenseRequest request) {
        List<UserId> people = request.splitBetween().stream().map(UserId::new).toList();
        ExpenseId id = groups.addExpense(
            new GroupId(groupId),
            request.description(),
            new UserId(request.paidBy()),
            new Money(request.amountCents(), request.currency()),
            new EqualSplit(people));
        return new AddExpenseResponse(id.value());
    }

    @GetMapping("/{groupId}/balances")
    public List<BalanceDto> balances(@PathVariable String groupId) {
        return groups.balances(new GroupId(groupId)).entrySet().stream()
            .map(e -> new BalanceDto(e.getKey().value(), e.getValue().cents(), e.getValue().currency()))
            .toList();
    }

    @GetMapping("/{groupId}/settlement")
    public List<PaymentDto> settlement(@PathVariable String groupId) {
        return groups.suggestSettlement(new GroupId(groupId)).stream()
            .map(PaymentDto::from)
            .toList();
    }

    @PostMapping("/{groupId}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public void recordPayment(@PathVariable String groupId, @RequestBody PaymentDto request) {
        groups.recordPayment(new GroupId(groupId), new Payment(
            new UserId(request.from()),
            new UserId(request.to()),
            new Money(request.amountCents(), request.currency())));
    }
}
