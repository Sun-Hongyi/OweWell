package owewell.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ExpenseTest {

    private final UserId alex = new UserId("alex");
    private final UserId sam = new UserId("sam");
    private final UserId priya = new UserId("priya");

    @Test
    void createsAnEqualSplitExpense() {
        Expense dinner = Expense.splitEqually(
            "Dinner", alex, new Money(9000, "AUD"), List.of(alex, sam, priya));

        assertEquals("Dinner", dinner.description());
        assertEquals(alex, dinner.paidBy());
        assertEquals(new Money(9000, "AUD"), dinner.total());
        assertEquals(new Money(3000, "AUD"), dinner.shares().get(sam));
    }

    @Test
    void throwsExceptionWhenSharesDontAddUpToTotal() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Expense("Taxi", alex, new Money(1000, "AUD"),
                    Map.of(alex, new Money(300, "AUD"), sam, new Money(300, "AUD")));
        });
    }

    @Test
    void throwsExceptionWhenNoShares() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Expense("Lunch", alex, new Money(1000, "AUD"), Map.of());
        });
    }

    @Test
    void throwsExceptionWhenTotalIsNotPositive() {
        assertThrows(IllegalArgumentException.class, () ->
            Expense.splitEqually("Refund?", alex, new Money(0, "AUD"), List.of(alex, sam)));
    }

    @Test
    void createsExpenseWithPercentageSplit() {
        Map<UserId, Integer> percentages = new LinkedHashMap<>();
        percentages.put(alex, 60);
        percentages.put(sam, 40);

        Expense dinner = Expense.withSplit(
            "Dinner", alex, new Money(10000, "AUD"), new PercentageSplit(percentages));

        assertEquals(new Money(6000, "AUD"), dinner.shares().get(alex));
        assertEquals(new Money(4000, "AUD"), dinner.shares().get(sam));
    }

    @Test
    void createsExpenseWithExactSplit() {
        Expense taxi = Expense.withSplit(
            "Taxi", sam, new Money(5000, "AUD"),
            new ExactSplit(Map.of(alex, new Money(2000, "AUD"), sam, new Money(3000, "AUD"))));

        assertEquals(new Money(2000, "AUD"), taxi.shares().get(alex));
        assertEquals(new Money(3000, "AUD"), taxi.shares().get(sam));
    }

    @Test
    void createsExpenseWithSharesSplit() {
        Map<UserId, Integer> shares = new LinkedHashMap<>();
        shares.put(alex, 2);
        shares.put(sam, 1);

        Expense drinks = Expense.withSplit(
            "Drinks", alex, new Money(3000, "AUD"), new SharesSplit(shares));

        assertEquals(new Money(2000, "AUD"), drinks.shares().get(alex));
        assertEquals(new Money(1000, "AUD"), drinks.shares().get(sam));
    }
}
