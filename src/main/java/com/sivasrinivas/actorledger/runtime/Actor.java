package com.sivasrinivas.actorledger.runtime;

@FunctionalInterface
public interface Actor<M> {
    void receive(M message) throws Exception;
}
