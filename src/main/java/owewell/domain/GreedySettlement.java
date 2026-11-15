package owewell.domain;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GreedySettlement implements SettlementAlgorithm {

    @Override
    public List<Payment> settle(Map<UserId, Money> balances) {
        List<Payment> payments = new ArrayList<>();
        Map<UserId, Money> remaining = new LinkedHashMap<>(balances);

        while (true) {
            UserId creditor = null;   // owed the most (biggest positive)
            UserId debtor = null;     // owes the most (biggest negative)

            for (Map.Entry<UserId, Money> entry : remaining.entrySet()) {
                UserId user = entry.getKey();
                Money balance = entry.getValue();

                if (balance.cents() > 0) {
                    if (creditor == null || balance.cents() > remaining.get(creditor).cents()) {
                        creditor = user;
                    }
                } else if (balance.cents() < 0) {
                    if (debtor == null || balance.cents() < remaining.get(debtor).cents()) {
                        debtor = user;
                    }
                }
            }

            if (creditor == null && debtor == null) {
                break;   // everyone is settled
            }
            if (creditor == null || debtor == null) {
                throw new IllegalArgumentException("Balances must add up to zero");
            }

            Money owed = remaining.get(creditor);
            Money owes = remaining.get(debtor);
            Money amount = new Money(Math.min(owed.cents(), -owes.cents()), owed.currency());

            payments.add(new Payment(debtor, creditor, amount));
            remaining.put(creditor, owed.subtract(amount));
            remaining.put(debtor, owes.add(amount));
        }

        return payments;
    }
}
