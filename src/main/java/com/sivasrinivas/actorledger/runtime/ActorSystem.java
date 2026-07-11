package com.sivasrinivas.actorledger.runtime;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ActorSystem implements AutoCloseable{

    private final List<ActorRef<?>> actors = new CopyOnWriteArrayList<>();

    public <M> ActorRef<M> actorOf(String name, Actor<M> behavior) {
        ActorRef<M> ref = new ActorRef<>(name, behavior);
        // add to the list
        actors.add(ref);
        return ref;
    }

    @Override
    public void close() throws Exception {
        actors.forEach(ActorRef::stop); // stop() will set the running flag to false
        for (ActorRef<?> actorRef : actors) {
            try {
                actorRef.awaitsTermination();
            } catch (InterruptedException e) { // catching interrupted exception will reset internal `isInterrupted` flag, so always call Thread.currentThread().interrupt()
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
