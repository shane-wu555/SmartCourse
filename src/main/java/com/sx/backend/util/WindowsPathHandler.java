package com.sx.backend.util;

import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class WindowsPathHandler {
    public Path getWindowsPath(String base, String relative) {
        return Paths.get(base, relative);
    }

    public String escapeSpaces(String path) {
        return path.contains(" ") ? "\"" + path + "\"" : path;
    }

    public String toWebPath(String path) {
        return path.replace("\\", "/");
    }
}