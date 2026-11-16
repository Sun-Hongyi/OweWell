package owewell.api;

import java.util.List;

public record AddExpenseRequest(String description, String paidBy, long amountCents,
                                String currency, List<String> splitBetween) {}
