package com.bupt.ta.web;

/** Next numeric suffix for prefixed IDs (e.g. POS001 → 2 after delete). */
public final class IdSequences {
    private IdSequences() {}

    public static int next(String prefix, Iterable<String> ids) {
        int max = 0;
        for (String id : ids) {
            if (id == null || !id.startsWith(prefix)) continue;
            max = Math.max(max, numericSuffix(id, prefix.length()));
        }
        return max + 1;
    }

    private static int numericSuffix(String id, int prefixLength) {
        if (id.length() <= prefixLength) return 0;
        try {
            return Integer.parseInt(id.substring(prefixLength));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
