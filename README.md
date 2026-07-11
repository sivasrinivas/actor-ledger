# actor-ledger

A mini in-memory bank ledger built on a tiny actor runtime over Java 25 virtual threads.

The point of the project is to get hands-on with three things at once:

- **Java 25** language + library features — sealed interfaces, record patterns, exhaustive `switch`, and the now-final **Scoped Values** API (JEP 506).
- **Concurrency** — thousands of concurrent operations driven by **virtual threads**, verified by a hard correctness invariant.
- **The actor model** — state is confined to single-threaded actors that communicate only by asynchronous messages, so there are **no locks anywhere** in the code.

## The idea

Money transfers are the classic case where lock-based sharing gets painful. Here, each bank account is an **actor**: it owns its balance and mutates it only in response to messages arriving one-at-a-time on its mailbox. Because a single virtual thread drives each actor, the balance field needs no `synchronized`, no `volatile`, and no atomics.

A transfer is a two-step message protocol — `Withdraw` on the source, then `Deposit` on the destination only if the withdrawal succeeded — so no actor ever touches two accounts' state at once. That removes any possibility of a lock-ordering deadlock.

The correctness contract: **no sequence of concurrent transfers may ever change the total amount of money in the bank.** The stress test fires hundreds of thousands of random transfers across virtual threads and asserts the total is unchanged.

## Project layout

```
src/main/java/com/sivasrinivas/actorledger/
  runtime/
    Actor.java         # behavior: how an actor handles one message
    ActorRef.java      # mailbox + virtual-thread message loop
    ActorSystem.java   # spawns actors, orderly shutdown (AutoCloseable)
    Trace.java         # ScopedValue-based per-message trace id
  bank/
    AccountMsg.java    # sealed message protocol (records)
    Account.java       # the account actor — lock-free mutable balance
    Bank.java          # facade: open accounts, transfer, total balance
  Main.java            # demo: storm of concurrent transfers + conservation check
src/test/java/com/sivasrinivas/actorledger/bank/
    BankConservationTest.java
```

## Build & run (Maven)

Requires **JDK 25**.

```bash
mvn verify                 # compile + run the conservation stress test
mvn -q exec:java           # run the demo with defaults (1,000 accounts / 500,000 transfers)
mvn -q package
java -jar target/actor-ledger.jar 2000 1000000   # <numAccounts> <numTransfers>
```

Set `-Dactorledger.traceLog=true` to see the Scoped Value trace id printed per message.

## Build & run (Docker / Podman)

The `Dockerfile` is a multi-stage build (JDK+Maven to build and test, JRE to run) and works the same under both engines:

```bash
# Docker
docker build -t actor-ledger .
docker run --rm actor-ledger 2000 1000000

# Podman (drop-in)
podman build -t actor-ledger .
podman run --rm actor-ledger 2000 1000000
```

## Where each Java 25 / concurrency feature shows up

| Feature | Location |
| --- | --- |
| Virtual threads (one per actor) | `ActorRef` message loop |
| Virtual threads (one per transfer) | `Main`, `BankConservationTest` |
| Sealed interface + records | `AccountMsg` |
| Record patterns + exhaustive `switch` | `Account.receive` |
| Scoped Values (JEP 506, final in 25) | `Trace`, bound in `Main` / the test |
| `AutoCloseable` structured lifecycle | `ActorSystem`, virtual-thread executors |

## Stretch goals

1. **Let it crash** — add a supervisor actor that restarts an account whose handler throws, Erlang-style.
2. **Backpressure** — swap the unbounded mailbox for a bounded queue and make `tell` block or reject when full.
3. **Structured concurrency** — replace the per-transfer executor with `StructuredTaskScope` to fan out a batch and join it as a unit with clean cancellation. (Still a preview API in 25 — compile and run with `--enable-preview`.)
4. **Swap in a real framework** — reimplement the bank on **Apache Pekko** (the open-source Akka fork) and compare ergonomics and throughput.
