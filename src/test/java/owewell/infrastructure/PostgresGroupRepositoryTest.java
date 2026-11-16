package owewell.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.flywaydb.core.Flyway;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import owewell.application.ConcurrentUpdateException;
import owewell.domain.Expense;
import owewell.domain.ExpenseId;
import owewell.domain.Group;
import owewell.domain.GroupId;
import owewell.domain.Money;
import owewell.domain.Payment;
import owewell.domain.SharesSplit;
import owewell.domain.UserId;
import tools.jackson.databind.json.JsonMapper;

@Testcontainers
class PostgresGroupRepositoryTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    private PostgresGroupRepository repository;

    private final UserId alex = new UserId("alex");
    private final UserId sam = new UserId("sam");
    private final UserId priya = new UserId("priya");

    private static Money aud(long cents) {
        return new Money(cents, "AUD");
    }

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

        Flyway.configure().dataSource(dataSource).load().migrate();   // create the tables

        repository = new PostgresGroupRepository(JdbcClient.create(dataSource), JsonMapper.builder().build());
    }

    // Each test uses a fresh random id, so tests never see each other's data
    private Group newGroup() {
        return new Group(new GroupId(UUID.randomUUID().toString()), "Bali Trip", Set.of(alex, sam, priya));
    }

    @Test
    void unknownGroupIsNotFound() {
        assertTrue(repository.findById(new GroupId("does-not-exist")).isEmpty());
    }

    @Test
    void savesAndLoadsAGroup() {
        Group group = newGroup();
        repository.save(group);

        Group loaded = repository.findById(group.id()).orElseThrow();

        assertEquals("Bali Trip", loaded.name());
        assertEquals(Set.of(alex, sam, priya), loaded.members());
    }

    @Test
    void everyEventTypeSurvivesTheRoundTrip() {
        Group group = newGroup();
        ExpenseId dinner = group.addExpense(Expense.splitEqually("Dinner", alex, aud(9000), List.of(alex, sam, priya)));
        group.addExpense(Expense.withSplit("Drinks", sam, aud(3000), new SharesSplit(Map.of(alex, 2, sam, 1))));
        group.recordPayment(new Payment(priya, alex, aud(3000)));
        group.voidExpense(dinner);
        repository.save(group);

        Group loaded = repository.findById(group.id()).orElseThrow();

        assertEquals(group.history(), loaded.history());
        assertEquals(group.balances(), loaded.balances());
    }

    @Test
    void savingAgainOnlyAddsTheNewEvents() {
        Group group = newGroup();
        group.addExpense(Expense.splitEqually("Dinner", alex, aud(9000), List.of(alex, sam, priya)));
        repository.save(group);

        Group loaded = repository.findById(group.id()).orElseThrow();
        ExpenseId taxi = loaded.addExpense(Expense.splitEqually("Taxi", sam, aud(3000), List.of(alex, sam)));
        repository.save(loaded);

        Group reloaded = repository.findById(group.id()).orElseThrow();
        assertEquals(2, reloaded.history().size());
        assertEquals(new ExpenseId("e2"), taxi);
    }

    @Test
    void newMembersAreSaved() {
        Group group = newGroup();
        repository.save(group);

        Group loaded = repository.findById(group.id()).orElseThrow();
        loaded.addMember(new UserId("jordan"));
        repository.save(loaded);

        assertTrue(repository.findById(group.id()).orElseThrow().members().contains(new UserId("jordan")));
    }

    @Test
    void twoPeopleEditingAtOnceIsDetected() {
        Group group = newGroup();
        repository.save(group);

        // Alex and Sam both load the same version of the group
        Group alexCopy = repository.findById(group.id()).orElseThrow();
        Group samCopy = repository.findById(group.id()).orElseThrow();

        alexCopy.addExpense(Expense.splitEqually("Dinner", alex, aud(9000), List.of(alex, sam, priya)));
        repository.save(alexCopy);   // Alex saves first: fine

        samCopy.addExpense(Expense.splitEqually("Taxi", sam, aud(3000), List.of(alex, sam)));
        assertThrows(ConcurrentUpdateException.class, () -> repository.save(samCopy));   // Sam is rejected

        // Only Alex's dinner was saved, and nothing was silently lost
        assertEquals(1, repository.findById(group.id()).orElseThrow().history().size());
    }

    @Test
    void findsGroupsForAMember() {
        Group group = newGroup();
        repository.save(group);

        assertTrue(repository.findSummariesForMember(alex).stream()
            .anyMatch(summary -> summary.id().equals(group.id())));
        assertTrue(repository.findSummariesForMember(new UserId("nobody-" + UUID.randomUUID())).isEmpty());
    }
}
