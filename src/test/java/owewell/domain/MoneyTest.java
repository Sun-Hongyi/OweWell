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

    @Test
    void allocatesByRatiosThreeToOne() {
        assertEquals(List.of(new Money(7500, "AUD"), new Money(2500, "AUD")),
            new Money(10000, "AUD").allocateByRatios(List.of(3, 1)));
    }

    @Test
    void equalRatiosMatchEqualAllocation() {
        assertEquals(new Money(1000, "AUD").allocate(3),
            new Money(1000, "AUD").allocateByRatios(List.of(1, 1, 1)));
    }

    @Test
    void leftoverCentGoesToFirstPerson() {
        assertEquals(List.of(new Money(34, "AUD"), new Money(66, "AUD")),
            new Money(100, "AUD").allocateByRatios(List.of(1, 2)));
    }

    @Test
    void zeroRatioPersonNeverGetsALeftoverCent() {
        assertEquals(List.of(new Money(0, "AUD"), new Money(51, "AUD"), new Money(50, "AUD")),
            new Money(101, "AUD").allocateByRatios(List.of(0, 1, 1)));
    }

    @Test
    void allocatesNegativeAmountsByRatiosWithoutLosingACent() {
        assertEquals(List.of(new Money(-34, "AUD"), new Money(-66, "AUD")),
            new Money(-100, "AUD").allocateByRatios(List.of(1, 2)));
    }

    @Test
    void rejectsNegativeRatios() {
        assertThrows(IllegalArgumentException.class,
            () -> new Money(1000, "AUD").allocateByRatios(List.of(3, -1)));
    }

    @Test
    void rejectsEmptyOrAllZeroRatios() {
        assertThrows(IllegalArgumentException.class,
            () -> new Money(1000, "AUD").allocateByRatios(List.of()));
        assertThrows(IllegalArgumentException.class,
            () -> new Money(1000, "AUD").allocateByRatios(List.of(0, 0)));
    }

    @Test
    void negateFlipsTheSign() {
        assertEquals(new Money(-500, "AUD"), new Money(500, "AUD").negate());
        assertEquals(new Money(500, "AUD"), new Money(-500, "AUD").negate());
    }
}
