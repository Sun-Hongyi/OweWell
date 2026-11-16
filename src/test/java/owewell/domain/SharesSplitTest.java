package owewell.domain;

import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SharesSplitTest {

    private final UserId alex = new UserId("alex");
    private final UserId sam = new UserId("sam");
    private final UserId priya = new UserId("priya");

    @Test
    void splitsByShares() {
        Map<UserId, Integer> shares = new LinkedHashMap<>();
        shares.put(alex, 2);
        shares.put(sam, 1);

        Map<UserId, Money> result = new SharesSplit(shares).split(new Money(3000, "AUD"));

        assertEquals(new Money(2000, "AUD"), result.get(alex));
        assertEquals(new Money(1000, "AUD"), result.get(sam));
    }

    @Test
    void equalSharesAddUpExactly() {
        Map<UserId, Integer> shares = new LinkedHashMap<>();
        shares.put(alex, 1);
        shares.put(sam, 1);
        shares.put(priya, 1);

        Map<UserId, Money> result = new SharesSplit(shares).split(new Money(1000, "AUD"));

        Money total = result.values().stream().reduce(new Money(0, "AUD"), Money::add);
        assertEquals(new Money(1000, "AUD"), total);
    }

    @Test
    void rejectsEmptyShares() {
        assertThrows(IllegalArgumentException.class,
            () -> new SharesSplit(Map.of()));
    }

    @Test
    void rejectsNegativeShares() {
        assertThrows(IllegalArgumentException.class,
            () -> new SharesSplit(Map.of(alex, 2, sam, -1)));
    }

    @Test
    void rejectsAllZeroShares() {
        assertThrows(IllegalArgumentException.class,
            () -> new SharesSplit(Map.of(alex, 0, sam, 0)));
    }
}
