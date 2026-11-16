package owewell.infrastructure;

import java.sql.Timestamp;
import java.util.Optional;

import org.springframework.jdbc.core.simple.JdbcClient;

import owewell.application.InviteRepository;
import owewell.domain.GroupId;
import owewell.domain.Invite;
import owewell.domain.UserId;

public class PostgresInviteRepository implements InviteRepository {

    private final JdbcClient jdbc;

    public PostgresInviteRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void save(Invite invite) {
        jdbc.sql("""
                INSERT INTO invite_links (token_hash, group_id, created_by, expires_at)
                VALUES (:tokenHash, :groupId, :createdBy, :expiresAt)
                """)
            .param("tokenHash", invite.tokenHash())
            .param("groupId", invite.groupId().value())
            .param("createdBy", invite.createdBy().value())
            .param("expiresAt", Timestamp.from(invite.expiresAt()))
            .update();
    }

    @Override
    public Optional<Invite> findByTokenHash(String tokenHash) {
        return jdbc.sql("""
                SELECT token_hash, group_id, created_by, expires_at
                FROM invite_links WHERE token_hash = :tokenHash
                """)
            .param("tokenHash", tokenHash)
            .query((row, rowNum) -> new Invite(
                row.getString("token_hash"),
                new GroupId(row.getString("group_id")),
                new UserId(row.getString("created_by")),
                row.getTimestamp("expires_at").toInstant()))
            .optional();
    }
}
