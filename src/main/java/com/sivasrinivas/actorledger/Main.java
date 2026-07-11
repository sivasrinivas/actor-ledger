package com.sivasrinivas.actorledger;

import com.sivasrinivas.actorledger.runtime.ActorSystem;

public class Main {
    static void main(String[] args) throws InterruptedException {

        try(var system = new ActorSystem()) {
            var greeter = system.actorOf("greeter", msg -> System.out.println("Handled: " + msg));
            greeter.tell("Hello");
            greeter.tell("world");
            // No need to call close() explicitly as ActorSystem is auto-closable
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
