package com.sivasrinivas.actorledger.bank;

import com.sivasrinivas.actorledger.runtime.ActorSystem;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BankConservationTest {
    @Test
    void moneyIsConserved() {
        int accounts = 500, transfers = 200_000; long opening = 10_000L;
        try (var system = new ActorSystem()) {
            var bank = new Bank(system);
            for (int i = 0; i < accounts; i++) bank.open(opening);
            long expected = (long) accounts * opening;

            try (var ex = Executors.newVirtualThreadPerTaskExecutor()) {
                for (int t = 0; t < transfers; t++)
                    ex.submit(() -> bank.transfer(
                            ThreadLocalRandom.current().nextInt(accounts),
                            ThreadLocalRandom.current().nextInt(accounts),
                            ThreadLocalRandom.current().nextLong(1, 200)));
            }
            assertEquals(expected, bank.totalBalance());
        }
    }
}