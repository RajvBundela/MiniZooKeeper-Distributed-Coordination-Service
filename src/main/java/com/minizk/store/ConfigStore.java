package com.minizk.store;

import java.util.*;

public class ConfigStore {
    private final NodeEntry root = new NodeEntry("");

    public synchronized void put(String path, String value) {
        String[] parts = pathSplit(path);
        NodeEntry cur = root;
        for (String p : parts) {
            cur = cur.children.computeIfAbsent(p, k -> new NodeEntry(k));
        }
        cur.value = value;
    }

    public synchronized Optional<String> get(String path) {
        NodeEntry cur = traverse(path);
        return cur == null ? Optional.empty() : Optional.ofNullable(cur.value);
    }

    private NodeEntry traverse(String path) {
        String[] parts = pathSplit(path);
        NodeEntry cur = root;
        for (String p : parts) {
            cur = cur.children.get(p);
            if (cur == null) return null;
        }
        return cur;
    }

    private String[] pathSplit(String path) {
        if (path == null || path.equals("/") || path.isEmpty()) return new String[0];
        return Arrays.stream(path.split("/"))
                     .filter(s -> !s.isEmpty())
                     .toArray(String[]::new);
    }

    static class NodeEntry {
        String name;
        String value;
        Map<String, NodeEntry> children = new HashMap<>();
        NodeEntry(String name) { this.name = name; }
    }
}