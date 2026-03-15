package org.example.core;

@FunctionalInterface
public interface Route {
    String handle(Request req, Response res);
}
