package owewell.domain;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class BalancesTest {

    private final UserId alex = new UserId("alex");
    private final UserId sam = new UserId("sam");
    private final UserId priya = new UserId("priya");

    @Test
    void oneDinnerPaidByAlex() {
        Expense dinner = Expense.splitEqually(
            "Dinner", alex, new Money(9000, "AUD"), List.of(alex, sam, priya));

        Map<UserId, Money> balances = Balances.calculate(List.of(dinner));

        assertEquals(new Money(6000, "AUD"), balances.get(alex));
        assertEquals(new Money(-3000, "AUD"), balances.get(sam));
        assertEquals(new Money(-3000, "AUD"), balances.get(priya));
    }

    @Test
    void dinnerPaidByAlexAndTaxiPaidBySam() {
        Expense dinner = Expense.splitEqually(
            "Dinner", alex, new Money(9000, "AUD"), List.of(alex, sam, priya));
        Expense taxi = Expense.splitEqually(
            "Taxi", sam, new Money(3000, "AUD"), List.of(alex, sam, priya));

        Map<UserId, Money> balances = Balances.calculate(List.of(dinner, taxi));

        assertEquals(new Money(5000, "AUD"), balances.get(alex));
        assertEquals(new Money(-1000, "AUD"), balances.get(sam));
        assertEquals(new Money(-4000, "AUD"), balances.get(priya));
    }

    @Test
    void balancesAlwaysAddUpToZero() {
        Expense dinner = Expense.splitEqually(
            "Dinner", alex, new Money(1000, "AUD"), List.of(alex, sam, priya));
        Expense taxi = Expense.splitEqually(
            "Taxi", priya, new Money(2500, "AUD"), List.of(sam, priya));

        Map<UserId, Money> balances = Balances.calculate(List.of(dinner, taxi));

        Money sum = balances.values().stream()
            .reduce(new Money(0, "AUD"), Money::add);
        assertTrue(sum.isZero());
    }
}
