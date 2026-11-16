package owewell.domain;

import java.util.Objects;

public sealed interface LedgerEvent {

    record ExpenseAdded(ExpenseId id, Expense expense) implements LedgerEvent {
        public ExpenseAdded {
            Objects.requireNonNull(id, "id is required");
            Objects.requireNonNull(expense, "expense is required");
        }
    }

    record ExpenseVoided(ExpenseId id) implements LedgerEvent {
        public ExpenseVoided {
            Objects.requireNonNull(id, "id is required");
        }
    }

    record PaymentRecorded(Payment payment) implements LedgerEvent {
        public PaymentRecorded {
            Objects.requireNonNull(payment, "payment is required");
        }
    }
}
