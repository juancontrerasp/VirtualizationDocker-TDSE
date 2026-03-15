package org.example.controller;

import org.example.annotation.GetMapping;
import org.example.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/")
    public static String index() {
        return "Greetings!";
    }
    @GetMapping("/pi")
    public static String getPI() {
        return "PI: "+ Math.PI;
    }
    @GetMapping("/hello")
    public static String helloWorld() {
        return "Hello World";
    }



}
