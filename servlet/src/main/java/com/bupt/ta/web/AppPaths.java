package com.bupt.ta.web;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Resolves webapp and data directories regardless of the process working directory. */
public final class AppPaths {
    private static Path projectRoot;
    private static Path webappDir;
    private static Path dataDir;

    private AppPaths() {}

    public static void init() {
        projectRoot = locateProjectRoot();
        webappDir = projectRoot.resolve("src").resolve("webapp");
        dataDir = projectRoot.resolve("data");
        if (!Files.isDirectory(webappDir)) {
            throw new IllegalStateException("Webapp not found: " + webappDir);
        }
        if (!Files.isDirectory(dataDir)) {
            throw new IllegalStateException("Data directory not found: " + dataDir);
        }
    }

    public static Path webappDir() {
        return webappDir;
    }

    public static Path dataDir() {
        return dataDir;
    }

    public static Path dataFile(String fileName) {
        return dataDir.resolve(fileName);
    }

    public static String resolve(String path) {
        if (path != null && path.startsWith("data/")) {
            return dataFile(path.substring("data/".length())).toString();
        }
        return path;
    }

    private static Path locateProjectRoot() {
        Path cwd = Paths.get("").toAbsolutePath().normalize();
        Path fromCwd = findModuleRoot(cwd);
        if (fromCwd != null) return fromCwd;

        Path fromClass = findModuleRootFromClasspath();
        if (fromClass != null) return fromClass;

        throw new IllegalStateException(
            "Cannot locate servlet module (expected src/webapp and data/). cwd=" + cwd);
    }

    private static Path findModuleRoot(Path start) {
        if (isModuleRoot(start)) return start;
        Path nested = start.resolve("servlet");
        if (isModuleRoot(nested)) return nested;
        return null;
    }

    private static Path findModuleRootFromClasspath() {
        try {
            Path code = Paths.get(WebServerMain.class.getProtectionDomain()
                .getCodeSource().getLocation().toURI()).normalize();
            Path dir = Files.isDirectory(code) ? code : code.getParent();
            for (int i = 0; i < 6 && dir != null; i++, dir = dir.getParent()) {
                if (isModuleRoot(dir)) return dir;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static boolean isModuleRoot(Path dir) {
        return Files.isDirectory(dir.resolve("src").resolve("webapp"))
            && Files.isDirectory(dir.resolve("data"));
    }
}
