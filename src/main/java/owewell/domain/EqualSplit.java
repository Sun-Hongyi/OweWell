package owewell.domain;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record EqualSplit(List<UserId> people) implements SplitStrategy {

    public EqualSplit {
        if (people == null || people.isEmpty()) {
            throw new IllegalArgumentException("People list cannot be null or empty");
        }
        if (new HashSet<>(people).size() != people.size()) {
            throw new IllegalArgumentException("People list cannot contain duplicates");
        }
        people = List.copyOf(people);
    }

    @Override
    public Map<UserId, Money> split(Money total) {
        List<Money> shares = total.allocate(people.size());
        Map<UserId, Money> result = new LinkedHashMap<>();
        for (int i = 0; i < people.size(); i++) {
            result.put(people.get(i), shares.get(i));
        }
        return result;
    }
}
