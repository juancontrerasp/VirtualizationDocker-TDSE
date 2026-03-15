package org.example.app;

import java.util.HashMap;
import java.util.Map;

import org.example.annotation.GetMapping;
import org.example.annotation.RequestParam;
import org.example.annotation.RestController;
import org.example.controller.GreetingController;
import org.example.controller.HelloController;
import org.example.core.HttpServer;
import org.example.core.Request;
import org.example.core.Response;


public class ManualTests {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  Manual Tests - Custom HTTP Framework  ");
        System.out.println("========================================\n");

        testHelloController();
        testGreetingController();
        testRequest();
        testHttpServerRoutes();
        testAnnotations();

        System.out.println("\n========================================");
        System.out.printf("  Results: %d passed, %d failed%n", passed, failed);
        System.out.println("========================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testHelloController() {
        System.out.println("--- HelloController ---");

        assertEqual("index()", "Greetings from Spring Boot!", HelloController.index());
        assertEqual("getPI()", "PI: " + Math.PI, HelloController.getPI());
        assertEqual("helloWorld()", "Hello World", HelloController.helloWorld());
        assertAnnotationPresent("@RestController on HelloController",
                HelloController.class.isAnnotationPresent(RestController.class));
    }


    private static void testGreetingController() {
        System.out.println("\n--- GreetingController ---");

        assertEqual("greeting(\"Alice\")", "Hola Alice", GreetingController.greeting("Alice"));
        assertEqual("greeting(\"World\")", "Hola World", GreetingController.greeting("World"));
        assertEqual("greeting(\"\")", "Hola ", GreetingController.greeting(""));
        assertAnnotationPresent("@RestController on GreetingController",
                GreetingController.class.isAnnotationPresent(RestController.class));

        try {
            GetMapping gm = GreetingController.class
                    .getDeclaredMethod("greeting", String.class)
                    .getAnnotation(GetMapping.class);
            assertEqual("@GetMapping value on greeting()", "/greeting", gm != null ? gm.value() : null);

            RequestParam rp = GreetingController.class
                    .getDeclaredMethod("greeting", String.class)
                    .getParameters()[0]
                    .getAnnotation(RequestParam.class);
            assertEqual("@RequestParam value", "name", rp != null ? rp.value() : null);
            assertEqual("@RequestParam defaultValue", "World", rp != null ? rp.defaultValue() : null);
        } catch (NoSuchMethodException e) {
            fail("Reflection on GreetingController.greeting(): " + e.getMessage());
        }
    }

    private static void testRequest() {
        System.out.println("\n--- Request ---");

        Map<String, String> params = new HashMap<>();
        params.put("name", "Bob");
        params.put("age", "42");
        Request req = new Request(params);

        assertEqual("getValues(\"name\")", "Bob", req.getValues("name"));
        assertEqual("getValues(\"age\")", "42", req.getValues("age"));
        assertEqual("getValues(missing key)", null, req.getValues("missing"));
        assertEqual("getValues(empty-value key)", "", new Request(Map.of("flag", "")).getValues("flag"));
    }


    private static void testHttpServerRoutes() {
        System.out.println("\n--- HttpServer routes ---");

        HttpServer.endPoints.clear();


        HttpServer.get("/test", (req, res) -> "ok");
        assertAnnotationPresent("get(path, Route) registers endpoint",
                HttpServer.endPoints.containsKey("/test"));

        String result = HttpServer.endPoints.get("/test").handle(new Request(new HashMap<>()), new Response());
        assertEqual("Route handler returns correct value", "ok", result);


        HttpServer.endPoints.clear();
        HttpServer.get("/wm", () -> "webmethod-result");
        assertAnnotationPresent("get(path, WebMethod) registers endpoint",
                HttpServer.endPoints.containsKey("/wm"));
        String wmResult = HttpServer.endPoints.get("/wm").handle(new Request(new HashMap<>()), new Response());
        assertEqual("WebMethod handler returns correct value", "webmethod-result", wmResult);


        HttpServer.endPoints.clear();
        HttpServer.path("/api");
        HttpServer.get("/items", (req, res) -> "items");
        assertAnnotationPresent("path prefix applied to registered route",
                HttpServer.endPoints.containsKey("/api/items"));
        HttpServer.path("");

        HttpServer.endPoints.clear();
        System.out.println("\n  Content-type detection:");
        assertContentType("file.png",  "image/png");
        assertContentType("photo.jpg", "image/jpeg");
        assertContentType("photo.jpeg","image/jpeg");
        assertContentType("anim.gif",  "image/gif");
        assertContentType("style.css", "text/css");
        assertContentType("app.js",    "application/javascript");
        assertContentType("index.html","text/html; charset=UTF-8");
        assertContentType("data.xyz",  "text/html; charset=UTF-8");
    }

    private static void assertContentType(String filename, String expected) {
        try {
            java.lang.reflect.Method m = HttpServer.class.getDeclaredMethod("getContentType", String.class);
            m.setAccessible(true);
            String actual = (String) m.invoke(null, filename);
            assertEqual("getContentType(\"" + filename + "\")", expected, actual);
        } catch (Exception e) {
            fail("getContentType reflection failed for " + filename + ": " + e.getMessage());
        }
    }

    private static void testAnnotations() {
        System.out.println("\n--- Annotations ---");

        java.lang.annotation.Retention rr = RestController.class.getAnnotation(java.lang.annotation.Retention.class);
        assertEqual("@RestController retention", java.lang.annotation.RetentionPolicy.RUNTIME,
                rr != null ? rr.value() : null);

        java.lang.annotation.Target rt = RestController.class.getAnnotation(java.lang.annotation.Target.class);
        assertEqual("@RestController target", java.lang.annotation.ElementType.TYPE,
                (rt != null && rt.value().length > 0) ? rt.value()[0] : null);

        java.lang.annotation.Retention gr = GetMapping.class.getAnnotation(java.lang.annotation.Retention.class);
        assertEqual("@GetMapping retention", java.lang.annotation.RetentionPolicy.RUNTIME,
                gr != null ? gr.value() : null);

        java.lang.annotation.Target gt = GetMapping.class.getAnnotation(java.lang.annotation.Target.class);
        assertEqual("@GetMapping target", java.lang.annotation.ElementType.METHOD,
                (gt != null && gt.value().length > 0) ? gt.value()[0] : null);

        java.lang.annotation.Retention pr = RequestParam.class.getAnnotation(java.lang.annotation.Retention.class);
        assertEqual("@RequestParam retention", java.lang.annotation.RetentionPolicy.RUNTIME,
                pr != null ? pr.value() : null);

        java.lang.annotation.Target pt = RequestParam.class.getAnnotation(java.lang.annotation.Target.class);
        assertEqual("@RequestParam target", java.lang.annotation.ElementType.PARAMETER,
                (pt != null && pt.value().length > 0) ? pt.value()[0] : null);
    }

    private static void assertEqual(String label, Object expected, Object actual) {
        if ((expected == null && actual == null) || (expected != null && expected.equals(actual))) {
            System.out.printf("  [PASS] %s%n", label);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s%n         expected: <%s>%n           actual: <%s>%n",
                    label, expected, actual);
            failed++;
        }
    }

    private static void assertAnnotationPresent(String label, boolean condition) {
        if (condition) {
            System.out.printf("  [PASS] %s%n", label);
            passed++;
        } else {
            System.out.printf("  [FAIL] %s (condition was false)%n", label);
            failed++;
        }
    }

    private static void fail(String label) {
        System.out.printf("  [FAIL] %s%n", label);
        failed++;
    }
}
