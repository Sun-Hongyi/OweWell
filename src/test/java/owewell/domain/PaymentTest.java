package owewell.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class PaymentTest {

    private final UserId alex = new UserId("alex");
    private final UserId sam = new UserId("sam");

    @Test
    void createsAValidPayment() {
        Payment payment = new Payment(sam, alex, new Money(3000, "AUD"));

        assertEquals(sam, payment.from());
        assertEquals(alex, payment.to());
        assertEquals(new Money(3000, "AUD"), payment.amount());
    }

    @Test
    void cannotPayYourself() {
        assertThrows(IllegalArgumentException.class,
            () -> new Payment(alex, alex, new Money(3000, "AUD")));
    }

    @Test
    void cannotPayZero() {
        assertThrows(IllegalArgumentException.class,
            () -> new Payment(sam, alex, new Money(0, "AUD")));
    }

    @Test
    void cannotPayANegativeAmount() {
        assertThrows(IllegalArgumentException.class,
            () -> new Payment(sam, alex, new Money(-500, "AUD")));
    }
}
