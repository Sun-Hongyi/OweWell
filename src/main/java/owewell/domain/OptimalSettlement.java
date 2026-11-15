package owewell.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OptimalSettlement {

    public static final int MAX_PEOPLE = 20;

    private final GreedySettlement greedy = new GreedySettlement();

    public List<Payment> settle(Map<UserId, Money> balances) {
        // 1. Only people who aren't already even need to be settled
        List<UserId> people = new ArrayList<>();
        List<Money> amounts = new ArrayList<>();
        for (Map.Entry<UserId, Money> entry : balances.entrySet()) {
            if (!entry.getValue().isZero()) {
                people.add(entry.getKey());
                amounts.add(entry.getValue());
            }
        }

        int n = people.size();
        if (n == 0) {
            return List.of();
        }
        if (n > MAX_PEOPLE) {
            throw new IllegalArgumentException("Too many people for optimal settlement: " + n);
        }

        int full = (1 << n) - 1;   // bitmask with everyone in it, e.g. 11111 for 5 people

        // 2. sum[mask] = total balance of the people in that subset
        long[] sum = new long[1 << n];
        for (int mask = 1; mask <= full; mask++) {
            int lowest = Integer.numberOfTrailingZeros(mask);       // one person in the subset
            sum[mask] = sum[mask & (mask - 1)] + amounts.get(lowest).cents();   // subset without them, plus them
        }
        if (sum[full] != 0) {
            throw new IllegalArgumentException("Balances must add up to zero");
        }

        // 3. groups[mask] = the most zero-sum groups this subset can be split into
        //    (removing people one at a time, counting each time the remaining subset sums to zero)
        int[] groups = new int[1 << n];
        for (int mask = 1; mask <= full; mask++) {
            int best = 0;
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    best = Math.max(best, groups[mask ^ (1 << i)]);
                }
            }
            groups[mask] = best + (sum[mask] == 0 ? 1 : 0);
        }

        // 4. Rebuild the actual groups by retracing the best choices
        List<List<Integer>> zeroSumGroups = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        int mask = full;
        while (mask != 0) {
            int target = groups[mask] - (sum[mask] == 0 ? 1 : 0);
            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0 && groups[mask ^ (1 << i)] == target) {
                    current.add(i);
                    mask ^= (1 << i);   // remove person i
                    break;
                }
            }
            if (sum[mask] == 0) {        // the people removed since the last group add up to zero
                zeroSumGroups.add(current);
                current = new ArrayList<>();
            }
        }

        // 5. Settle each group separately. Greedy uses at most (size - 1) payments per group,
        //    which adds up to the minimum overall.
        List<Payment> payments = new ArrayList<>();
        for (List<Integer> group : zeroSumGroups) {
            Map<UserId, Money> groupBalances = new LinkedHashMap<>();
            for (int i : group) {
                groupBalances.put(people.get(i), amounts.get(i));
            }
            payments.addAll(greedy.settle(groupBalances));
        }
        return payments;
    }
}
