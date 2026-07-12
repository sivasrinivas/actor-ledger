package com.sivasrinivas.actorledger.runtime;

public final class Trace {
    public static final ScopedValue<String> ID = ScopedValue.newInstance();
    private  Trace() {
    }

    public static String current() {return ID.isBound() ? ID.get() : "-";}
}

