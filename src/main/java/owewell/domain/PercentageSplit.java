package owewell.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record PercentageSplit(Map<UserId, Integer> percentages) implements SplitStrategy {

    public PercentageSplit {
        if (percentages == null || percentages.isEmpty()) {
            throw new IllegalArgumentException("Percentages map cannot be null or empty");
        }

        for (Integer percentage : percentages.values()) {
            if (percentage < 0) {
                throw new IllegalArgumentException("Percentages cannot be negative");
            }
        }

        int totalPercentage = percentages.values().stream().mapToInt(Integer::intValue).sum();
        if (totalPercentage != 100) {
            throw new IllegalArgumentException("Total percentage must equal 100, but was " + totalPercentage);
        }

        percentages = Collections.unmodifiableMap(new LinkedHashMap<>(percentages));
    }

    @Override
    public Map<UserId, Money> split(Money total) {
        List<UserId> people = new ArrayList<>();
        List<Integer> ratios = new ArrayList<>();
        for (Map.Entry<UserId, Integer> entry : percentages.entrySet()) {
            people.add(entry.getKey());
            ratios.add(entry.getValue());
        }

        List<Money> amounts = total.allocateByRatios(ratios);

        Map<UserId, Money> result = new LinkedHashMap<>();
        for (int i = 0; i < people.size(); i++) {
            result.put(people.get(i), amounts.get(i));
        }
        return result;
    }
}
