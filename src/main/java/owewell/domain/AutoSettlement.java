package owewell.domain;

import java.util.List;
import java.util.Map;

public class AutoSettlement implements SettlementAlgorithm {

    private final SettlementAlgorithm optimal = new OptimalSettlement();
    private final SettlementAlgorithm greedy = new GreedySettlement();

    @Override
    public List<Payment> settle(Map<UserId, Money> balances) {
        long peopleWhoOwe = balances.values().stream()
            .filter(m -> !m.isZero())
            .count();

        if (peopleWhoOwe <= OptimalSettlement.MAX_PEOPLE) {
            return optimal.settle(balances);
        }
        return greedy.settle(balances);
    }
}
