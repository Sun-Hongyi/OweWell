package owewell.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

class UserIdTest {

    @Test
    void twoIdsWithTheSameValueAreEqual() {
        assertEquals(new UserId("alex"), new UserId("alex"));
    }

    @Test
    void rejectsBlankIds() {
        assertThrows(IllegalArgumentException.class, () -> new UserId("  "));
    }

    @Test
    void rejectsNullIds() {
        assertThrows(IllegalArgumentException.class, () -> new UserId(null));
    }
}
