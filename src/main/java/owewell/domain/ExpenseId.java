package owewell.domain;

public record ExpenseId(String value) {
    public ExpenseId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ExpenseId cannot be null or blank");
        }
    }
}
