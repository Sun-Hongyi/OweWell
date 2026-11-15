package owewell.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import org.junit.jupiter.api.Test;

class AutoSettlementTest {

    private static Money aud(long cents) {
        return new Money(cents, "AUD");
    }

    @Test
    void usesOptimalForSmallGroups() {
        Map<UserId, Money> balances = new LinkedHashMap<>();
        balances.put(new UserId("alex"), aud(10000));
        balances.put(new UserId("ben"), aud(8000));
        balances.put(new UserId("chris"), aud(-8000));
        balances.put(new UserId("dana"), aud(-7000));
        balances.put(new UserId("eli"), aud(-3000));

        // Optimal finds 3 payments here; greedy would find 4
        assertEquals(3, new AutoSettlement().settle(balances).size());
    }

    @Test
    void handlesLargeGroupsWithoutCrashing() {
        Map<UserId, Money> balances = new LinkedHashMap<>();
        for (int i = 0; i < 30; i++) {
            balances.put(new UserId("p" + i), aud(i % 2 == 0 ? 1000 : -1000));
        }

        // 30 people is over the optimal limit, so this uses greedy instead of throwing
        assertFalse(new AutoSettlement().settle(balances).isEmpty());
    }
}
