package owewell.domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class MoneyTest {

    @Test
    void splitsTenDollarsThreeWaysWithoutLosingACent() {
        Money total = new Money(1000, "AUD");
        assertEquals(List.of(
            new Money(334, "AUD"),
            new Money(333, "AUD"),
            new Money(333, "AUD")
        ), total.allocate(3));
    }

    @Test
    void addsMoneyInTheSameCurrency() {
        assertEquals(new Money(750, "AUD"),
            new Money(500, "AUD").add(new Money(250, "AUD")));
    }

    @Test
    void refusesToAddDifferentCurrencies() {
        assertThrows(IllegalArgumentException.class,
            () -> new Money(500, "AUD").add(new Money(500, "USD")));
    }
}
