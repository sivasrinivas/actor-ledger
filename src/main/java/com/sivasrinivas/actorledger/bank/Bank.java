package com.sivasrinivas.actorledger.bank;

import com.sivasrinivas.actorledger.runtime.ActorRef;
import com.sivasrinivas.actorledger.runtime.ActorSystem;
import com.sivasrinivas.actorledger.runtime.Trace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class Bank {

    private final ActorSystem system;
    private final List<ActorRef<AccountMsg>> accounts = new ArrayList<>();

    public  Bank(ActorSystem system) {
        this.system = system;
    }

    // Bank will open an account with given openingBalance
    public int open(long openingBalance) {
        int id = accounts.size();
        accounts.add(system.actorOf("account-"+id, new Account(openingBalance)));
        return id;
    }

    public boolean transfer(int fromId, int toId, long amount) {
        var ack = new CompletableFuture<Boolean>();
        accounts.get(fromId).tell(new AccountMsg.Withdraw(amount, Trace.current(), ack));
        boolean withdrawn = ack.join();
        if (withdrawn) {
            accounts.get(toId).tell(new AccountMsg.Deposit(amount, Trace.current()));
        }
        return  withdrawn;
    }

    public long totalBalance () {
        List<CompletableFuture<Long>> replies = new ArrayList<>();

        for (var account : accounts) {
            var reply = new CompletableFuture<Long>();
            account.tell(new AccountMsg.GetBalance(Trace.current(), reply));
            replies.add(reply);
        }

        return replies.stream().mapToLong(CompletableFuture::join).sum();
    }

}
