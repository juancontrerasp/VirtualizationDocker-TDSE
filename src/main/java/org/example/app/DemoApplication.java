package org.example.app;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

import org.example.annotation.GetMapping;
import org.example.annotation.RestController;
import org.example.annotation.RequestParam;
import org.example.core.HttpServer;
import org.example.core.Request;
import org.example.core.Response;

public class DemoApplication {

	public static void main(String[] args) throws IOException, URISyntaxException {
		scanClasspath();
		HttpServer.start();
	}

	private static void scanClasspath() {
		String classpath = System.getProperty("java.class.path");
		for (String entry : classpath.split(File.pathSeparator)) {
			File file = new File(entry);
			if (file.isDirectory()) {
				scanDirectory(file, file);
			}
		}
	}

	private static void scanDirectory(File root, File dir) {
		File[] files = dir.listFiles();
		if (files == null) return;
		for (File file : files) {
			if (file.isDirectory()) {
				scanDirectory(root, file);
			} else if (file.getName().endsWith(".class")) {
				String className = root.toURI().relativize(file.toURI()).getPath()
						.replace('/', '.').replace(".class", "");
				try {
					Class<?> c = Class.forName(className);
					registerController(c);
				} catch (Throwable ignored) {}
			}
		}
	}

	private static void registerController(Class<?> c) {
		if (!c.isAnnotationPresent(RestController.class)) return;
		for (Method m : c.getDeclaredMethods()) {
			if (!m.isAnnotationPresent(GetMapping.class)) continue;
			String path = m.getAnnotation(GetMapping.class).value();
			System.out.println("Registering route: " + path + " -> " + c.getSimpleName() + "." + m.getName());
			HttpServer.get(path, (req, res) -> {
				try {
					java.lang.reflect.Parameter[] params = m.getParameters();
					if (params.length == 0) {
						return (String) m.invoke(null);
					}
					Object[] invokeArgs = new Object[params.length];
					for (int i = 0; i < params.length; i++) {
						RequestParam rp = params[i].getAnnotation(RequestParam.class);
						if (rp != null) {
							String val = req.getValues(rp.value());
							invokeArgs[i] = (val != null) ? val : rp.defaultValue();
						} else {
							invokeArgs[i] = null;
						}
					}
					return (String) m.invoke(null, invokeArgs);
				} catch (Exception e) {
					return "Error: " + e.getMessage();
				}
			});
		}
	}
}
