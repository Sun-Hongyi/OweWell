package owewell.domain;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public record Expense(String description, UserId paidBy, Money total, Map<UserId, Money> shares) {

    public Expense {
        Objects.requireNonNull(description, "description is required");
        Objects.requireNonNull(paidBy, "paidBy is required");
        Objects.requireNonNull(total, "total is required");

        if (total.cents() <= 0) {
            throw new IllegalArgumentException("Expense total must be positive");
        }

        if (shares == null || shares.isEmpty()) {
            throw new IllegalArgumentException("An expense must have at least one share");
        }

        Money sumOfShares = shares.values().stream()
            .reduce(new Money(0, total.currency()), Money::add);

        if (!sumOfShares.equals(total)) {
            throw new IllegalArgumentException(
                "Shares add up to " + sumOfShares.cents() + " cents but the total is "
                + total.cents() + " cents");
        }

        shares = Map.copyOf(shares);
    }

    public static Expense withSplit(String description, UserId paidBy,
                                    Money total, SplitStrategy strategy) {
        Map<UserId, Money> shares = strategy.split(total);
        return new Expense(description, paidBy, total, shares);
    }

    public static Expense splitEqually(String description, UserId paidBy,
                                       Money total, List<UserId> people) {
        return withSplit(description, paidBy, total, new EqualSplit(people));
    }
}
