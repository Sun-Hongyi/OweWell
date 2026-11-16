package owewell.api;

public record ExpenseDto(String id, String description, String paidBy,
                         long totalCents, String currency, boolean voided) {}
