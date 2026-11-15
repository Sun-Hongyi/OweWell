package owewell.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record Money(long cents, String currency) {
    public Money {
        Objects.requireNonNull(currency, "currency is required");
    }

    public Money add(Money other) {
        requireSameCurrency(other);
        return new Money(this.cents + other.cents, this.currency);
    }

    public Money subtract(Money other) {
        requireSameCurrency(other);
        return new Money(this.cents - other.cents, this.currency);
    }

    public boolean isZero() {
        return this.cents == 0;
    }

    public List<Money> allocate(int parts) {
        if (parts <= 0) {
            throw new IllegalArgumentException("parts must be positive");
        }
        if (cents < 0) {
            // Split the positive amount, then flip each share back to negative
            return new Money(-cents, currency).allocate(parts).stream()
                .map(share -> new Money(-share.cents, currency))
                .toList();
        }
        long base = cents / parts;
        long remainder = cents % parts;
        List<Money> result = new ArrayList<>();
        for (int i = 0; i < parts; i++) {
            result.add(new Money(base + (i < remainder ? 1 : 0), currency));
        }
        return result;
    }

    private void requireSameCurrency(Money other) {
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                "Currency mismatch: " + currency + " vs " + other.currency);
        }
    }
}