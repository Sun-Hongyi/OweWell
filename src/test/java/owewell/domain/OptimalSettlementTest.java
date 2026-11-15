package owewell.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class OptimalSettlementTest {

    private final UserId alex = new UserId("alex");
    private final UserId ben = new UserId("ben");
    private final UserId chris = new UserId("chris");
    private final UserId dana = new UserId("dana");
    private final UserId eli = new UserId("eli");

    private static Money aud(long cents) {
        return new Money(cents, "AUD");
    }

    private static void assertEveryoneSettled(Map<UserId, Money> balances, List<Payment> payments) {
        Map<UserId, Money> after = new LinkedHashMap<>(balances);
        for (Payment p : payments) {
            after.merge(p.from(), p.amount(), Money::add);
            after.merge(p.to(), p.amount(), Money::subtract);
        }
        for (Money balance : after.values()) {
            assertTrue(balance.isZero(), "Expected everyone at $0 but got " + after);
        }
    }

    @Test
    void findsThreePaymentsWhereGreedyNeedsFour() {
        Map<UserId, Money> balances = new LinkedHashMap<>();
        balances.put(alex, aud(10000));
        balances.put(ben, aud(8000));
        balances.put(chris, aud(-8000));
        balances.put(dana, aud(-7000));
        balances.put(eli, aud(-3000));

        List<Payment> payments = new OptimalSettlement().settle(balances);

        assertEveryoneSettled(balances, payments);
        assertEquals(3, payments.size());
    }

    @Test
    void nobodyOwesAnythingMeansNoPayments() {
        Map<UserId, Money> balances = new LinkedHashMap<>();
        balances.put(alex, aud(0));
        balances.put(ben, aud(0));

        assertTrue(new OptimalSettlement().settle(balances).isEmpty());
    }

    @Test
    void rejectsBalancesThatDontAddUpToZero() {
        Map<UserId, Money> balances = new LinkedHashMap<>();
        balances.put(alex, aud(5000));
        balances.put(ben, aud(-1000));

        assertThrows(IllegalArgumentException.class,
            () -> new OptimalSettlement().settle(balances));
    }

    @Test
    void neverWorseThanGreedyOnRandomGroups() {
        Random random = new Random(42);   // fixed seed, so the test gives the same result every run

        for (int round = 0; round < 500; round++) {
            int people = 2 + random.nextInt(7);   // groups of 2 to 8 people
            Map<UserId, Money> balances = new LinkedHashMap<>();
            long total = 0;
            for (int i = 0; i < people - 1; i++) {
                long cents = random.nextInt(10001) - 5000;   // between -$50 and +$50
                balances.put(new UserId("p" + i), aud(cents));
                total += cents;
            }
            balances.put(new UserId("p" + (people - 1)), aud(-total));   // makes the sum zero

            List<Payment> optimal = new OptimalSettlement().settle(balances);
            List<Payment> greedy = new GreedySettlement().settle(balances);

            assertEveryoneSettled(balances, optimal);
            assertTrue(optimal.size() <= greedy.size(),
                "Optimal used " + optimal.size() + " but greedy used " + greedy.size());
        }
    }
}
