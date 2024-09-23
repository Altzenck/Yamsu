package me.altzenck.util;

@FunctionalInterface
public interface Trifunction<A,B,C,R> {

    R apply(A a, B b, C c);
}
