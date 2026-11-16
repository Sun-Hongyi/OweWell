package owewell.domain;

public record ExpenseEntry(ExpenseId id, Expense expense, boolean voided) {}
