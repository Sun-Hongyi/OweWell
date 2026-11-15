package owewell.domain;

import java.util.List;
import java.util.Map;

public interface SettlementAlgorithm {
    List<Payment> settle(Map<UserId, Money> balances);
}
