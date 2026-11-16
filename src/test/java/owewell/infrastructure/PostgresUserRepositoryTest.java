package owewell.infrastructure;

import java.util.UUID;

import org.flywaydb.core.Flyway;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import owewell.domain.User;
import owewell.domain.UserId;

@Testcontainers
class PostgresUserRepositoryTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    private PostgresUserRepository repository;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        Flyway.configure().dataSource(dataSource).load().migrate();
        repository = new PostgresUserRepository(JdbcClient.create(dataSource));
    }

    @Test
    void savesAndLoadsAUser() {
        UserId id = new UserId(UUID.randomUUID().toString());
        repository.save(new User(id, "Alex", "alex@example.com"));

        User loaded = repository.findById(id).orElseThrow();

        assertEquals("Alex", loaded.name());
        assertEquals("alex@example.com", loaded.email());
    }

    @Test
    void signingInAgainUpdatesTheUserInsteadOfDuplicating() {
        UserId id = new UserId(UUID.randomUUID().toString());
        repository.save(new User(id, "Alex", "alex@example.com"));
        repository.save(new User(id, "Alex Smith", "alex.smith@example.com"));

        User loaded = repository.findById(id).orElseThrow();
        assertEquals("Alex Smith", loaded.name());
        assertEquals("alex.smith@example.com", loaded.email());
    }

    @Test
    void unknownUserIsNotFound() {
        assertTrue(repository.findById(new UserId("nobody")).isEmpty());
    }
}
