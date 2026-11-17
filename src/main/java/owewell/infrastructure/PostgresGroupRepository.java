package owewell.infrastructure;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import owewell.application.ConcurrentUpdateException;
import owewell.application.GroupRepository;
import owewell.application.GroupSummary;
import owewell.domain.Expense;
import owewell.domain.ExpenseId;
import owewell.domain.Group;
import owewell.domain.GroupId;
import owewell.domain.LedgerEvent;
import owewell.domain.Money;
import owewell.domain.Payment;
import owewell.domain.UserId;
import tools.jackson.databind.json.JsonMapper;

public class PostgresGroupRepository implements GroupRepository {

    private final JdbcClient jdbc;
    private final JsonMapper json;

    public PostgresGroupRepository(JdbcClient jdbc, JsonMapper json) {
        this.jdbc = jdbc;
        this.json = json;
    }

    // ---------- Saving (object → database) ----------

    @Override
    @Transactional
    public void save(Group group) {
        String groupId = group.id().value();

        jdbc.sql("""
                INSERT INTO groups (id, name) VALUES (:id, :name)
                ON CONFLICT (id) DO UPDATE SET name = EXCLUDED.name
                """)
            .param("id", groupId)
            .param("name", group.name())
            .update();

        for (UserId member : group.members()) {
            jdbc.sql("""
                    INSERT INTO group_members (group_id, user_id) VALUES (:groupId, :userId)
                    ON CONFLICT DO NOTHING
                    """)
                .param("groupId", groupId)
                .param("userId", member.value())
                .update();
        }

        // Insert only the events added since this group was loaded (its version).
        // If someone else saved in the meantime, their events already use these
        // sequence numbers, so the primary key rejects ours.
        List<LedgerEvent> history = group.history();
        try {
            for (int i = group.version(); i < history.size(); i++) {
                LedgerEvent event = history.get(i);
                jdbc.sql("""
                        INSERT INTO ledger_events (group_id, sequence, type, payload)
                        VALUES (:groupId, :sequence, :type, CAST(:payload AS jsonb))
                        """)
                    .param("groupId", groupId)
                    .param("sequence", i + 1)
                    .param("type", typeOf(event))
                    .param("payload", toJson(event))
                    .update();
            }
        } catch (DuplicateKeyException e) {
            throw new ConcurrentUpdateException(group.id());
        }
    }

    // Soft delete: the group and its history stay in the database, but it's hidden everywhere
    @Override
    public void markDeleted(GroupId id) {
        jdbc.sql("UPDATE groups SET deleted_at = now() WHERE id = :id AND deleted_at IS NULL")
            .param("id", id.value())
            .update();
    }

    // ---------- Loading (database → object) ----------

    @Override
    public Optional<Group> findById(GroupId id) {
        Optional<String> name = jdbc.sql("SELECT name FROM groups WHERE id = :id AND deleted_at IS NULL")
            .param("id", id.value())
            .query(String.class)
            .optional();
        if (name.isEmpty()) {
            return Optional.empty();
        }

        Set<UserId> members = new LinkedHashSet<>();
        jdbc.sql("SELECT user_id FROM group_members WHERE group_id = :groupId ORDER BY user_id")
            .param("groupId", id.value())
            .query(String.class)
            .list()
            .forEach(userId -> members.add(new UserId(userId)));

        List<LedgerEvent> events = jdbc.sql("""
                SELECT type, payload::text AS payload FROM ledger_events
                WHERE group_id = :groupId ORDER BY sequence
                """)
            .param("groupId", id.value())
            .query((row, rowNum) -> fromJson(row.getString("type"), row.getString("payload")))
            .list();

        return Optional.of(Group.restore(id, name.get(), members, events));
    }

    @Override
    public List<GroupSummary> findSummariesForMember(UserId member) {
        return jdbc.sql("""
                SELECT g.id, g.name
                FROM groups g
                JOIN group_members m ON m.group_id = g.id
                WHERE m.user_id = :userId
                  AND g.deleted_at IS NULL
                ORDER BY g.created_at DESC
                """)
            .param("userId", member.value())
            .query((row, rowNum) -> new GroupSummary(new GroupId(row.getString("id")), row.getString("name")))
            .list();
    }

    // ---------- Turning events into JSON and back ----------

    public record ExpenseAddedPayload(String expenseId, String description, String paidBy,
                                      long totalCents, String currency, Map<String, Long> sharesCents) {}

    public record ExpenseVoidedPayload(String expenseId) {}

    public record PaymentRecordedPayload(String from, String to, long amountCents, String currency) {}

    private static String typeOf(LedgerEvent event) {
        return switch (event) {
            case LedgerEvent.ExpenseAdded e -> "ExpenseAdded";
            case LedgerEvent.ExpenseVoided e -> "ExpenseVoided";
            case LedgerEvent.PaymentRecorded e -> "PaymentRecorded";
        };
    }

    private String toJson(LedgerEvent event) {
        Object payload = switch (event) {
            case LedgerEvent.ExpenseAdded added -> {
                Expense expense = added.expense();
                Map<String, Long> shares = new LinkedHashMap<>();
                expense.shares().forEach((user, money) -> shares.put(user.value(), money.cents()));
                yield new ExpenseAddedPayload(added.id().value(), expense.description(),
                    expense.paidBy().value(), expense.total().cents(), expense.total().currency(), shares);
            }
            case LedgerEvent.ExpenseVoided voided -> new ExpenseVoidedPayload(voided.id().value());
            case LedgerEvent.PaymentRecorded recorded -> {
                Payment p = recorded.payment();
                yield new PaymentRecordedPayload(p.from().value(), p.to().value(),
                    p.amount().cents(), p.amount().currency());
            }
        };
        return json.writeValueAsString(payload);
    }

    private LedgerEvent fromJson(String type, String payload) {
        return switch (type) {
            case "ExpenseAdded" -> {
                ExpenseAddedPayload p = json.readValue(payload, ExpenseAddedPayload.class);
                Map<UserId, Money> shares = new LinkedHashMap<>();
                p.sharesCents().forEach((user, cents) -> shares.put(new UserId(user), new Money(cents, p.currency())));
                yield new LedgerEvent.ExpenseAdded(new ExpenseId(p.expenseId()),
                    new Expense(p.description(), new UserId(p.paidBy()),
                        new Money(p.totalCents(), p.currency()), shares));
            }
            case "ExpenseVoided" -> {
                ExpenseVoidedPayload p = json.readValue(payload, ExpenseVoidedPayload.class);
                yield new LedgerEvent.ExpenseVoided(new ExpenseId(p.expenseId()));
            }
            case "PaymentRecorded" -> {
                PaymentRecordedPayload p = json.readValue(payload, PaymentRecordedPayload.class);
                yield new LedgerEvent.PaymentRecorded(new Payment(new UserId(p.from()), new UserId(p.to()),
                    new Money(p.amountCents(), p.currency())));
            }
            default -> throw new IllegalStateException("Unknown event type: " + type);
        };
    }
}
