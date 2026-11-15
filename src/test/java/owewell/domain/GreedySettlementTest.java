package owewell.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class GreedySettlementTest {

    private final UserId alex = new UserId("alex");
    private final UserId sam = new UserId("sam");
    private final UserId priya = new UserId("priya");

    private static Money aud(long cents) {
        return new Money(cents, "AUD");
    }

    // Applies the payments to the balances and checks everyone ends at $0
    private static void assertEveryoneSettled(Map<UserId, Money> balances, List<Payment> payments) {
        Map<UserId, Money> after = new LinkedHashMap<>(balances);
        for (Payment p : payments) {
            after.merge(p.from(), p.amount(), Money::add);       // payer's debt shrinks
            after.merge(p.to(), p.amount(), Money::subtract);    // receiver is owed less
        }
        for (Money balance : after.values()) {
            assertTrue(balance.isZero(), "Expected everyone at $0 but got " + after);
        }
    }

    @Test
    void oneDinnerSettlesWithTwoPayments() {
        Map<UserId, Money> balances = new LinkedHashMap<>();
        balances.put(alex, aud(6000));
        balances.put(sam, aud(-3000));
        balances.put(priya, aud(-3000));

        List<Payment> payments = new GreedySettlement().settle(balances);

        assertEquals(2, payments.size());
        assertTrue(payments.contains(new Payment(sam, alex, aud(3000))));
        assertTrue(payments.contains(new Payment(priya, alex, aud(3000))));
    }

    @Test
    void nobodyOwesAnythingMeansNoPayments() {
        Map<UserId, Money> balances = new LinkedHashMap<>();
        balances.put(alex, aud(0));
        balances.put(sam, aud(0));

        assertTrue(new GreedySettlement().settle(balances).isEmpty());
    }

    @Test
    void everyoneEndsAtZeroAfterSettling() {
        Map<UserId, Money> balances = new LinkedHashMap<>();
        balances.put(alex, aud(5000));
        balances.put(sam, aud(-1000));
        balances.put(priya, aud(-4000));

        List<Payment> payments = new GreedySettlement().settle(balances);

        assertEveryoneSettled(balances, payments);
    }

    @Test
    void greedyIsNotAlwaysOptimal() {
        // The example from earlier: the best answer is 3 payments, greedy finds 4
        UserId ben = new UserId("ben");
        UserId chris = new UserId("chris");
        UserId dana = new UserId("dana");
        UserId eli = new UserId("eli");

        Map<UserId, Money> balances = new LinkedHashMap<>();
        balances.put(alex, aud(10000));
        balances.put(ben, aud(8000));
        balances.put(chris, aud(-8000));
        balances.put(dana, aud(-7000));
        balances.put(eli, aud(-3000));

        List<Payment> payments = new GreedySettlement().settle(balances);

        assertEveryoneSettled(balances, payments);
        assertEquals(4, payments.size());
    }

    @Test
    void rejectsBalancesThatDontAddUpToZero() {
        Map<UserId, Money> balances = new LinkedHashMap<>();
        balances.put(alex, aud(5000));
        balances.put(sam, aud(-1000));

        assertThrows(IllegalArgumentException.class,
            () -> new GreedySettlement().settle(balances));
    }

    @Test
    void rejectsBalancesWhereMoreIsOwedThanOwing() {
        Map<UserId, Money> balances = new LinkedHashMap<>();
        balances.put(alex, aud(1000));
        balances.put(sam, aud(-5000));

        assertThrows(IllegalArgumentException.class,
            () -> new GreedySettlement().settle(balances));
    }
}
