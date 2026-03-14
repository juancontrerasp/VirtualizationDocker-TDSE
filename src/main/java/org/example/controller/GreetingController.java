package org.example.controller;

import java.util.concurrent.atomic.AtomicLong;

import org.example.annotation.GetMapping;
import org.example.annotation.RequestParam;
import org.example.annotation.RestController;

@RestController
public class GreetingController {

    private static final String template = "Hello, %s!";
    private static final AtomicLong counter = new AtomicLong();

    @GetMapping("/greeting")
    public static String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hola " + name;
    }
}
