package com.sivasrinivas.actorledger.runtime;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ActorRef<M> {
    private final String name;
    private final BlockingQueue<M> mailbox = new LinkedBlockingQueue<>();
    private final Thread thread;
    private volatile  boolean running = true;

    public ActorRef(String name, Actor<M> behavior) {
        this.name = name;
        this.thread = Thread.ofVirtual().name(name).start(() -> runLoop(behavior));
    }

    private void runLoop(Actor<M> behavior) {
        while (running || !mailbox.isEmpty()) {
            try {
                M msg = mailbox.poll(50, TimeUnit.MILLISECONDS);
                if (msg != null) {behavior.receive(msg);}
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void tell(M msg) {
        mailbox.add(msg);
    }

    public void stop() {
        this.running = false;
    }

    public void awaitsTermination() throws InterruptedException {
        thread.join();
    }
}
