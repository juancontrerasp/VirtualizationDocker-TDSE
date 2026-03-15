package org.example.controller;

import org.junit.jupiter.api.Test;

import org.example.annotation.GetMapping;
import org.example.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloControllerTest {

    @Test
    void index_returnsGreeting() {
        assertEquals("Greetings from Spring Boot!", HelloController.index());
    }

    @Test
    void getPI_returnsPiValue() {
        assertEquals("PI: " + Math.PI, HelloController.getPI());
    }

    @Test
    void helloWorld_returnsHelloWorld() {
        assertEquals("Hello World", HelloController.helloWorld());
    }

    @Test
    void helloControllerIsAnnotatedWithRestController() {
        assertTrue(HelloController.class.isAnnotationPresent(RestController.class));
    }

    @Test
    void indexMethodIsAnnotatedWithGetMapping() throws NoSuchMethodException {
        GetMapping annotation = HelloController.class
                .getDeclaredMethod("index")
                .getAnnotation(GetMapping.class);
        assertNotNull(annotation);
        assertEquals("/", annotation.value());
    }

    @Test
    void piMethodIsAnnotatedWithGetMapping() throws NoSuchMethodException {
        GetMapping annotation = HelloController.class
                .getDeclaredMethod("getPI")
                .getAnnotation(GetMapping.class);
        assertNotNull(annotation);
        assertEquals("/pi", annotation.value());
    }

    @Test
    void helloWorldMethodIsAnnotatedWithGetMapping() throws NoSuchMethodException {
        GetMapping annotation = HelloController.class
                .getDeclaredMethod("helloWorld")
                .getAnnotation(GetMapping.class);
        assertNotNull(annotation);
        assertEquals("/hello", annotation.value());
    }

    private static void assertTrue(boolean condition) {
        org.junit.jupiter.api.Assertions.assertTrue(condition);
    }

    private static void assertNotNull(Object obj) {
        org.junit.jupiter.api.Assertions.assertNotNull(obj);
    }
}
