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

    public Money negate() {
        return new Money(-cents, currency);
    }

    public List<Money> allocate(int parts) {
        if (parts <= 0) {
            throw new IllegalArgumentException("parts must be positive");
        }
        if (cents < 0) {
            // Split the positive amount, then flip each share back to negative
            return new Money(-cents, currency).allocate(parts).stream()
                .map(Money::negate)
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

    public List<Money> allocateByRatios(List<Integer> ratios) {
        if (ratios == null || ratios.isEmpty()) {
            throw new IllegalArgumentException("ratios must not be null or empty");
        }

        long totalRatio = 0;
        for (int ratio : ratios) {
            if (ratio < 0) {
                throw new IllegalArgumentException("ratios cannot be negative");
            }
            totalRatio += ratio;
        }
        if (totalRatio == 0) {
            throw new IllegalArgumentException("ratios cannot all be zero");
        }

        // Same trick as allocate(): split the positive amount, then flip back
        if (cents < 0) {
            return new Money(-cents, currency).allocateByRatios(ratios).stream()
                .map(Money::negate)
                .toList();
        }

        // 1. Give everyone their rounded-down share
        long[] shareCents = new long[ratios.size()];
        long allocated = 0;
        for (int i = 0; i < ratios.size(); i++) {
            shareCents[i] = Math.multiplyExact(cents, (long) ratios.get(i)) / totalRatio;
            allocated += shareCents[i];
        }

        // 2. Hand out the leftover cents one each, skipping anyone with a ratio of 0
        long leftover = cents - allocated;
        for (int i = 0; i < ratios.size() && leftover > 0; i++) {
            if (ratios.get(i) > 0) {
                shareCents[i]++;
                leftover--;
            }
        }

        List<Money> result = new ArrayList<>();
        for (long c : shareCents) {
            result.add(new Money(c, currency));
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
