package owewell.domain;

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
}
