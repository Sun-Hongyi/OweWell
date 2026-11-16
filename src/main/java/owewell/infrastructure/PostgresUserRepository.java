package owewell.infrastructure;

import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;

import owewell.application.UserRepository;
import owewell.domain.User;
import owewell.domain.UserId;

public class PostgresUserRepository implements UserRepository {

    private final JdbcClient jdbc;

    public PostgresUserRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(User user) {
        jdbc.sql("""
                INSERT INTO users (id, name, email) VALUES (:id, :name, :email)
                ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name, email = EXCLUDED.email
                """)
            .param("id", user.id().value())
            .param("name", user.name())
            .param("email", user.email())
            .update();
    }

    @Override
    public Optional<User> findById(UserId id) {
        return jdbc.sql("SELECT id, name, email FROM users WHERE id = :id")
            .param("id", id.value())
            .query((row, rowNum) -> new User(
                new UserId(row.getString("id")),
                row.getString("name"),
                row.getString("email")))
            .optional();
    }
}
