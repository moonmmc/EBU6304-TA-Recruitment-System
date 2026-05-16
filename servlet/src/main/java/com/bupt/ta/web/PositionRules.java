package com.bupt.ta.web;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Position lifecycle: PENDING → APPROVED|REJECTED; APPROVED → CLOSED. */
public final class PositionRules {
    private static final Set<String> STATUSES = new HashSet<>(
            Arrays.asList("PENDING", "APPROVED", "REJECTED", "CLOSED"));

    private PositionRules() {}

    public static boolean isValidStatus(String status) {
        return status != null && STATUSES.contains(status);
    }

    public static boolean isPastDeadline(String deadline) {
        if (deadline == null || deadline.trim().isEmpty()) return false;
        try {
            return LocalDate.now().isAfter(LocalDate.parse(deadline.trim()));
        } catch (Exception e) {
            return true;
        }
    }

    /** TA may browse and apply: approved, not closed/rejected, before deadline. */
    public static boolean isOpenForTa(Position pos) {
        return pos != null
                && "APPROVED".equals(pos.getStatus())
                && !isPastDeadline(pos.getDeadline());
    }

    /**
     * @return null if allowed, otherwise an error message for the client
     */
    public static String validateTransition(String role, String userId, Position pos, String newStatus) {
        if (pos == null) return "Position not found";
        if (!isValidStatus(newStatus)) return "Invalid status";

        String from = pos.getStatus();
        if (from.equals(newStatus)) return null;

        if ("REJECTED".equals(from) || "CLOSED".equals(from)) {
            return "Position status cannot be changed from " + from;
        }

        if ("ADMIN".equals(role)) {
            if ("PENDING".equals(from) && ("APPROVED".equals(newStatus) || "REJECTED".equals(newStatus))) {
                return null;
            }
            if ("APPROVED".equals(from) && "CLOSED".equals(newStatus)) {
                return null;
            }
            return "Admin cannot change status from " + from + " to " + newStatus;
        }

        if ("MO".equals(role)) {
            if (!userId.equals(pos.getMoId())) {
                return "Cannot manage another module owner's position";
            }
            if ("APPROVED".equals(from) && "CLOSED".equals(newStatus)) {
                return null;
            }
            return "Cannot change status from " + from + " to " + newStatus;
        }

        return "Insufficient permissions";
    }
}
