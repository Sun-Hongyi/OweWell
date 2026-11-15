package owewell.domain;

import java.util.Objects;

public record Payment(UserId from, UserId to, Money amount) {
    public Payment {
        Objects.requireNonNull(from, "from is required");
        Objects.requireNonNull(to, "to is required");
        Objects.requireNonNull(amount, "amount is required");
        if (from.equals(to)) {
            throw new IllegalArgumentException("Cannot pay yourself");
        }
        if (amount.cents() <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
    }
}
