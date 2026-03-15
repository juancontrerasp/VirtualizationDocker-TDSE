package org.example.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class HttpServerTest {

    @BeforeEach
    void clearRoutes() {
        HttpServer.endPoints.clear();
    }

    @Test
    void get_withRoute_registersEndpoint() {
        Route route = (req, res) -> "ok";
        HttpServer.get("/test", route);

        assertTrue(HttpServer.endPoints.containsKey("/test"));
    }

    @Test
    void get_withWebMethod_registersEndpointAndDelegates() {
        HttpServer.get("/wm", () -> "webmethod-result");

        assertTrue(HttpServer.endPoints.containsKey("/wm"));
        String result = HttpServer.endPoints.get("/wm").handle(new Request(new java.util.HashMap<>()), new Response());
        assertEquals("webmethod-result", result);
    }

    @Test
    void get_withRoute_handlerInvokesCorrectly() {
        HttpServer.get("/hello", (req, res) -> "Hello World");

        Route registered = HttpServer.endPoints.get("/hello");
        assertNotNull(registered);
        String response = registered.handle(new Request(new java.util.HashMap<>()), new Response());
        assertEquals("Hello World", response);
    }

    @Test
    void path_setsPrefix_affectsRegistration() throws Exception {
        java.lang.reflect.Field appPathField = HttpServer.class.getDeclaredField("appPath");
        appPathField.setAccessible(true);
        appPathField.set(null, "/api");

        HttpServer.get("/items", (req, res) -> "items");

        assertTrue(HttpServer.endPoints.containsKey("/api/items"));
        assertFalse(HttpServer.endPoints.containsKey("/items"));

        appPathField.set(null, "");
    }

    @Test
    void path_publicMethod_setsPrefix() throws Exception {
        HttpServer.path("/v1");

        java.lang.reflect.Field appPathField = HttpServer.class.getDeclaredField("appPath");
        appPathField.setAccessible(true);
        assertEquals("/v1", appPathField.get(null));

        appPathField.set(null, "");
    }

    @Test
    void staticfiles_publicMethod_setsLocation() throws Exception {
        HttpServer.staticfiles("/public");

        java.lang.reflect.Field field = HttpServer.class.getDeclaredField("staticFilesLocation");
        field.setAccessible(true);
        assertEquals("/public", field.get(null));

        field.set(null, null);
    }

    @Test
    void getContentType_png_returnsImagePng() throws Exception {
        assertEquals("image/png", invokeGetContentType("file.png"));
    }

    @Test
    void getContentType_jpg_returnsImageJpeg() throws Exception {
        assertEquals("image/jpeg", invokeGetContentType("photo.jpg"));
    }

    @Test
    void getContentType_jpeg_returnsImageJpeg() throws Exception {
        assertEquals("image/jpeg", invokeGetContentType("photo.jpeg"));
    }

    @Test
    void getContentType_gif_returnsImageGif() throws Exception {
        assertEquals("image/gif", invokeGetContentType("anim.gif"));
    }

    @Test
    void getContentType_css_returnsTextCss() throws Exception {
        assertEquals("text/css", invokeGetContentType("style.css"));
    }

    @Test
    void getContentType_js_returnsApplicationJavascript() throws Exception {
        assertEquals("application/javascript", invokeGetContentType("app.js"));
    }

    @Test
    void getContentType_html_returnsTextHtml() throws Exception {
        assertEquals("text/html; charset=UTF-8", invokeGetContentType("index.html"));
    }

    @Test
    void getContentType_unknownExtension_returnsTextHtml() throws Exception {
        assertEquals("text/html; charset=UTF-8", invokeGetContentType("file.xyz"));
    }

    private String invokeGetContentType(String path) throws Exception {
        Method method = HttpServer.class.getDeclaredMethod("getContentType", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, path);
    }
}
