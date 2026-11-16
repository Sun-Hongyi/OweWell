package owewell.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ExactSplit(Map<UserId, Money> amounts) implements SplitStrategy {

    public ExactSplit {
        if (amounts == null || amounts.isEmpty()) {
            throw new IllegalArgumentException("Amounts map cannot be null or empty");
        }
        for (Money amount : amounts.values()) {
            if (amount.cents() < 0) {
                throw new IllegalArgumentException("Amounts cannot be negative");
            }
        }
        amounts = Collections.unmodifiableMap(new LinkedHashMap<>(amounts));
    }

    @Override
    public Map<UserId, Money> split(Money total) {
        Money sum = amounts.values().stream()
            .reduce(new Money(0, total.currency()), Money::add);

        if (!sum.equals(total)) {
            throw new IllegalArgumentException(
                "Amounts add up to " + sum.cents() + " cents but the total is "
                + total.cents() + " cents");
        }
        return amounts;
    }
}
