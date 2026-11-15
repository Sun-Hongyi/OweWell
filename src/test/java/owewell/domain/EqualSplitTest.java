package owewell.domain;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class EqualSplitTest {

    private final UserId alex = new UserId("alex");
    private final UserId sam = new UserId("sam");
    private final UserId priya = new UserId("priya");

    @Test
    void splitsNinetyDollarsEquallyBetweenThreePeople() {
        EqualSplit split = new EqualSplit(List.of(alex, sam, priya));

        Map<UserId, Money> shares = split.split(new Money(9000, "AUD"));

        assertEquals(new Money(3000, "AUD"), shares.get(alex));
        assertEquals(new Money(3000, "AUD"), shares.get(sam));
        assertEquals(new Money(3000, "AUD"), shares.get(priya));
    }

    @Test
    void splitsTenDollarsEquallyBetweenThreePeople() {
        EqualSplit split = new EqualSplit(List.of(alex, sam, priya));

        Map<UserId, Money> shares = split.split(new Money(1000, "AUD"));

        Money total = shares.values().stream().reduce(new Money(0, "AUD"), Money::add);
        assertEquals(new Money(1000, "AUD"), total);
    }

    @Test
    void throwsExceptionWhenNoPeople() {
        assertThrows(IllegalArgumentException.class,
            () -> new EqualSplit(List.of()));
    }

    @Test
    void throwsExceptionWhenSamePersonIsListedTwice() {
        assertThrows(IllegalArgumentException.class,
            () -> new EqualSplit(List.of(alex, sam, alex)));
    }
}
