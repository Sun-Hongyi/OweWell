package owewell.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import owewell.domain.EqualSplit;
import owewell.domain.ExactSplit;
import owewell.domain.Money;
import owewell.domain.PercentageSplit;
import owewell.domain.SharesSplit;
import owewell.domain.SplitStrategy;
import owewell.domain.UserId;

public record SplitRequest(String type,
                           List<String> people,
                           Map<String, Integer> weights,
                           Map<String, Long> amountsCents) {

    public SplitStrategy toStrategy(String currency) {
        if (type == null) {
            throw new IllegalArgumentException("split type is required");
        }
        return switch (type) {
            case "equal" -> new EqualSplit(require(people, "people").stream().map(UserId::new).toList());
            case "shares" -> new SharesSplit(toUserMap(require(weights, "weights")));
            case "percentage" -> new PercentageSplit(toUserMap(require(weights, "weights")));
            case "exact" -> new ExactSplit(toMoneyMap(require(amountsCents, "amountsCents"), currency));
            default -> throw new IllegalArgumentException("Unknown split type: " + type);
        };
    }

    private static <T> T require(T value, String field) {
        if (value == null) {
            throw new IllegalArgumentException(field + " is required for this split type");
        }
        return value;
    }

    private static Map<UserId, Integer> toUserMap(Map<String, Integer> raw) {
        Map<UserId, Integer> result = new LinkedHashMap<>();
        raw.forEach((user, value) -> result.put(new UserId(user), value));
        return result;
    }

    private static Map<UserId, Money> toMoneyMap(Map<String, Long> raw, String currency) {
        Map<UserId, Money> result = new LinkedHashMap<>();
        raw.forEach((user, cents) -> result.put(new UserId(user), new Money(cents, currency)));
        return result;
    }
}
