package com.sivasrinivas.actorledger.bank;

import java.util.concurrent.CompletableFuture;

public sealed interface AccountMsg permits AccountMsg.Deposit, AccountMsg.Withdraw, AccountMsg.GetBalance {

    record Deposit(long amount, String traceId) implements  AccountMsg {}
    record Withdraw(long amount, String traceId, CompletableFuture<Boolean> ack) implements  AccountMsg {}
    record GetBalance(String traceId, CompletableFuture<Long> reply) implements AccountMsg {}
}
