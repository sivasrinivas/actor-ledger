package com.sivasrinivas.actorledger.bank;

import com.sivasrinivas.actorledger.runtime.Actor;

public final class Account implements Actor<AccountMsg> {

    private long balance;

    public Account(long balance) {
        this.balance = balance;
    }

    @Override
    public void receive(AccountMsg message) {
        String tid;
        switch (message) {
            case AccountMsg.Deposit(long amount, String traceId) -> {
                tid = traceId;
                balance += amount;
            }
            case AccountMsg.Withdraw(long amount, String traceId, var ack) -> {
                tid = traceId;
                if(balance > amount) {balance -= amount; ack.complete(true);}
                else {ack.complete(false);}
            }
            case AccountMsg.GetBalance(String traceId, var reply) -> {
                tid = traceId;
                reply.complete(balance);
            }
        }
        System.out.printf("[trace %s] %s%n", tid, message.getClass().getSimpleName());
    }
}
