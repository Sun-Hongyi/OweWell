package owewell.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record SharesSplit(Map<UserId, Integer> shares) implements SplitStrategy {

    public SharesSplit {
        if (shares == null || shares.isEmpty()) {
            throw new IllegalArgumentException("Shares map cannot be null or empty");
        }

        for (Integer share : shares.values()) {
            if (share < 0) {
                throw new IllegalArgumentException("Shares cannot be negative");
            }
        }

        if (shares.values().stream().allMatch(share -> share == 0)) {
            throw new IllegalArgumentException("Shares cannot all be zero");
        }

        shares = Collections.unmodifiableMap(new LinkedHashMap<>(shares));
    }

    @Override
    public Map<UserId, Money> split(Money total) {
        List<UserId> people = new ArrayList<>();
        List<Integer> ratios = new ArrayList<>();
        for (Map.Entry<UserId, Integer> entry : shares.entrySet()) {
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
