package owewell.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record Money(long cents, String currency) {

    public Money {
        Objects.requireNonNull(currency, "currency is required");
    }

    public Money add(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add " + currency + " and " + other.currency);
        }
        return new Money(cents + other.cents, currency);
    }

    public List<Money> allocate(int parts) {
        if (parts <= 0) {
            throw new IllegalArgumentException("parts must be positive");
        }
        long base = cents / parts;
        long remainder = cents % parts;
        List<Money> result = new ArrayList<>();
        for (int i = 0; i < parts; i++) {
            result.add(new Money(base + (i < remainder ? 1 : 0), currency));
        }
        return result;
    }
}
