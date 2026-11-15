package owewell.domain;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

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

    @Test
    void splitsNegativeAmountsWithoutLosingACent() {
        assertEquals(List.of(
            new Money(-334, "AUD"),
            new Money(-333, "AUD"),
            new Money(-333, "AUD")
        ), new Money(-1000, "AUD").allocate(3));
    }

    @Test
    void subtractsMoneyInTheSameCurrency() {
        assertEquals(new Money(250, "AUD"),
            new Money(500, "AUD").subtract(new Money(250, "AUD")));
    }

    @Test
    void subtractingMoreThanYouHaveGivesANegativeAmount() {
        assertEquals(new Money(-200, "AUD"),
            new Money(300, "AUD").subtract(new Money(500, "AUD")));
    }

    @Test
    void refusesToSubtractDifferentCurrencies() {
        assertThrows(IllegalArgumentException.class,
            () -> new Money(500, "AUD").subtract(new Money(100, "USD")));
    }

    @Test
    void knowsWhenItIsZero() {
        assertTrue(new Money(0, "AUD").isZero());
        assertFalse(new Money(1, "AUD").isZero());
    }
}
