package owewell.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class PercentageSplitTest {

    private final UserId alex = new UserId("alex");
    private final UserId sam = new UserId("sam");

    @Test
    void splitsSixtyForty() {
        Map<UserId, Integer> percentages = new LinkedHashMap<>();
        percentages.put(alex, 60);
        percentages.put(sam, 40);

        Map<UserId, Money> result = new PercentageSplit(percentages).split(new Money(10000, "AUD"));

        assertEquals(new Money(6000, "AUD"), result.get(alex));
        assertEquals(new Money(4000, "AUD"), result.get(sam));
    }

    @Test
    void oddAmountSplitFiftyFiftyAddsUpExactly() {
        Map<UserId, Integer> percentages = new LinkedHashMap<>();
        percentages.put(alex, 50);
        percentages.put(sam, 50);

        Map<UserId, Money> result = new PercentageSplit(percentages).split(new Money(1001, "AUD"));

        Money sum = result.values().stream().reduce(new Money(0, "AUD"), Money::add);
        assertEquals(new Money(1001, "AUD"), sum);
    }

    @Test
    void rejectsPercentagesThatDontAddUpTo100() {
        assertThrows(IllegalArgumentException.class,
            () -> new PercentageSplit(Map.of(alex, 50, sam, 40)));
    }

    @Test
    void rejectsEmptyPercentages() {
        assertThrows(IllegalArgumentException.class,
            () -> new PercentageSplit(Map.of()));
    }

    @Test
    void rejectsNegativePercentageEvenIfTotalIs100() {
        assertThrows(IllegalArgumentException.class,
            () -> new PercentageSplit(Map.of(alex, 110, sam, -10)));
    }
}
