package owewell.api;

import java.util.List;

import owewell.domain.EqualSplit;
import owewell.domain.SplitStrategy;
import owewell.domain.UserId;

public record AddExpenseRequest(String description, String paidBy, long amountCents,
                                String currency, List<String> splitBetween, SplitRequest split) {

    public SplitStrategy toSplitStrategy() {
        if (split != null) {
            return split.toStrategy(currency);
        }
        if (splitBetween != null) {
            return new EqualSplit(splitBetween.stream().map(UserId::new).toList());
        }
        throw new IllegalArgumentException("Either 'split' or 'splitBetween' is required");
    }
}
