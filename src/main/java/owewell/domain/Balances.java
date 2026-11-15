package owewell.domain;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Balances {

    private Balances() {}

    public static Map<UserId, Money> calculate(List<Expense> expenses) {
        Map<UserId, Money> balances = new LinkedHashMap<>();

        for (Expense expense : expenses) {
            balances.merge(expense.paidBy(), expense.total(), Money::add);

            for (Map.Entry<UserId, Money> entry : expense.shares().entrySet()) {
                Money share = entry.getValue();
                balances.merge(entry.getKey(),
                    new Money(-share.cents(), share.currency()), Money::add);
            }
        }

        return balances;
    }
}
