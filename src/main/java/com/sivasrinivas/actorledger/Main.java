package com.sivasrinivas.actorledger;

import com.sivasrinivas.actorledger.bank.Bank;
import com.sivasrinivas.actorledger.runtime.ActorSystem;
import com.sivasrinivas.actorledger.runtime.Trace;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class Main {
    static void main(String[] args) {

        int numAccounts = 1_000, numTransfers = 500_000;

        try(var system = new ActorSystem()) {
            var bank = new Bank(system);

            for(int i=0; i<numAccounts; i++) {bank.open(1_00_000L);} // open accounts with openingBalance

            long expected = bank.totalBalance();
            var succeeded = new AtomicLong();

            try(var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                for(int i=0; i<numTransfers; i++) {
                    int finalI = i;
                    executor.submit(() -> {
                       int from = ThreadLocalRandom.current().nextInt(0, numAccounts);
                       int to = ThreadLocalRandom.current().nextInt(0, numAccounts);
                       long amount = ThreadLocalRandom.current().nextLong(1, 500);

                        boolean ok = ScopedValue.where(Trace.ID, "txn-" + finalI).call(() -> bank.transfer(from, to, amount));

                       if(ok) {succeeded.incrementAndGet();}
                    });
                }
            }

            var actual = bank.totalBalance();

            System.out.printf("succeeded=%d, conserved=%b\n", succeeded.get(), expected == actual);

        }
    }
}
