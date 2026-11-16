package owewell.infrastructure;

import java.time.Instant;
import java.util.Set;
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

import owewell.domain.Group;
import owewell.domain.GroupId;
import owewell.domain.Invite;
import owewell.domain.UserId;
import tools.jackson.databind.json.JsonMapper;

@Testcontainers
class PostgresInviteRepositoryTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    private PostgresInviteRepository invites;
    private PostgresGroupRepository groups;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
            postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
        Flyway.configure().dataSource(dataSource).load().migrate();
        JdbcClient jdbc = JdbcClient.create(dataSource);
        invites = new PostgresInviteRepository(jdbc);
        groups = new PostgresGroupRepository(jdbc, JsonMapper.builder().build());
    }

    @Test
    void savesAndFindsAnInvite() {
        // An invite must belong to a real group, because of the REFERENCES rule
        Group group = new Group(new GroupId(UUID.randomUUID().toString()), "Bali Trip", Set.of(new UserId("alex")));
        groups.save(group);

        Invite invite = new Invite("hash-" + UUID.randomUUID(), group.id(), new UserId("alex"),
            Instant.parse("2026-11-23T10:00:00Z"));
        invites.save(invite);

        assertEquals(invite, invites.findByTokenHash(invite.tokenHash()).orElseThrow());
    }

    @Test
    void unknownHashIsNotFound() {
        assertTrue(invites.findByTokenHash("no-such-hash").isEmpty());
    }
}
