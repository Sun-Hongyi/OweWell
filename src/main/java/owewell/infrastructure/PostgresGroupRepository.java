package owewell.infrastructure;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import owewell.application.GroupRepository;
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

        // The ledger only ever grows, so only insert events that aren't saved yet
        long alreadySaved = jdbc.sql("SELECT COUNT(*) FROM ledger_events WHERE group_id = :groupId")
            .param("groupId", groupId)
            .query(Long.class)
            .single();

        List<LedgerEvent> history = group.history();
        for (int i = (int) alreadySaved; i < history.size(); i++) {
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
    }

    // ---------- Loading (database → object) ----------

    @Override
    public Optional<Group> findById(GroupId id) {
        Optional<String> name = jdbc.sql("SELECT name FROM groups WHERE id = :id")
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
