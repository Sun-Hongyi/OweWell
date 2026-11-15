package owewell.domain;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class SettlementBenchmark {

    public static void main(String[] args) {
        Random random = new Random(42);   // fixed seed, so results are the same every run
        SettlementAlgorithm greedy = new GreedySettlement();
        SettlementAlgorithm optimal = new OptimalSettlement();
        int groupsPerSize = 2000;

        System.out.println("people | greedy avg | optimal avg | saved | optimal better in | greedy ms | optimal ms");

        for (int people = 3; people <= 12; people++) {
            long greedyPayments = 0, optimalPayments = 0, better = 0;
            long greedyNanos = 0, optimalNanos = 0;

            for (int g = 0; g < groupsPerSize; g++) {
                Map<UserId, Money> balances = randomBalances(random, people);

                long t0 = System.nanoTime();
                int gp = greedy.settle(balances).size();
                long t1 = System.nanoTime();
                int op = optimal.settle(balances).size();
                long t2 = System.nanoTime();

                greedyPayments += gp;
                optimalPayments += op;
                if (op < gp) better++;
                greedyNanos += t1 - t0;
                optimalNanos += t2 - t1;
            }

            double greedyAvg = (double) greedyPayments / groupsPerSize;
            double optimalAvg = (double) optimalPayments / groupsPerSize;

            System.out.printf("%6d | %10.2f | %11.2f | %4.1f%% | %16.1f%% | %9.3f | %10.3f%n",
                people,
                greedyAvg,
                optimalAvg,
                100 * (greedyAvg - optimalAvg) / greedyAvg,
                100.0 * better / groupsPerSize,
                greedyNanos / 1e6 / groupsPerSize,
                optimalNanos / 1e6 / groupsPerSize);
        }
    }

    // Random whole-dollar balances between -$100 and +$100 that add up to zero
    private static Map<UserId, Money> randomBalances(Random random, int people) {
        Map<UserId, Money> balances = new LinkedHashMap<>();
        long total = 0;
        for (int i = 0; i < people - 1; i++) {
            long cents = (random.nextInt(201) - 100) * 100L;
            balances.put(new UserId("p" + i), new Money(cents, "AUD"));
            total += cents;
        }
        balances.put(new UserId("p" + (people - 1)), new Money(-total, "AUD"));
        return balances;
    }
}
