package owewell.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class ExactSplitTest {

    private final UserId alex = new UserId("alex");
    private final UserId sam = new UserId("sam");

    @Test
    void returnsTheExactAmounts() {
        Map<UserId, Money> amounts = new LinkedHashMap<>();
        amounts.put(alex, new Money(7000, "AUD"));
        amounts.put(sam, new Money(3000, "AUD"));

        Map<UserId, Money> result = new ExactSplit(amounts).split(new Money(10000, "AUD"));

        assertEquals(new Money(7000, "AUD"), result.get(alex));
        assertEquals(new Money(3000, "AUD"), result.get(sam));
    }

    @Test
    void rejectsAmountsThatDontAddUpToTheTotal() {
        ExactSplit split = new ExactSplit(Map.of(
            alex, new Money(7000, "AUD"),
            sam, new Money(2000, "AUD")));

        assertThrows(IllegalArgumentException.class,
            () -> split.split(new Money(10000, "AUD")));
    }

    @Test
    void rejectsEmptyAmounts() {
        assertThrows(IllegalArgumentException.class,
            () -> new ExactSplit(Map.of()));
    }

    @Test
    void rejectsNegativeAmounts() {
        assertThrows(IllegalArgumentException.class,
            () -> new ExactSplit(Map.of(alex, new Money(-500, "AUD"))));
    }

    @Test
    void rejectsMixedCurrencies() {
        ExactSplit split = new ExactSplit(Map.of(
            alex, new Money(5000, "AUD"),
            sam, new Money(5000, "USD")));

        assertThrows(IllegalArgumentException.class,
            () -> split.split(new Money(10000, "AUD")));
    }
}
