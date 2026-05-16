package com.bupt.ta.web;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/** Business rules for TA applications and MO decisions. */
public final class ApplicationRules {
    public static final int MAX_HOURS_PER_WEEK = 20;
    private static final Set<String> APPLICATION_STATUSES = new HashSet<>(
            Arrays.asList("PENDING", "PASSED", "FAILED"));

    private ApplicationRules() {}

    public static boolean isValidStatus(String status) {
        return status != null && APPLICATION_STATUSES.contains(status);
    }

    public static boolean isPastDeadline(String deadline) {
        return PositionRules.isPastDeadline(deadline);
    }

    public static boolean isOpenForApplication(Position pos) {
        return PositionRules.isOpenForTa(pos);
    }

    public static String positionTitle(Position pos) {
        return pos.getCourseName() + " TA";
    }

    public static boolean hasVacancy(Position pos, ApplicationService applications) {
        if (pos.getNumPositions() <= 0) return false;
        return applications.countPassedForPosition(pos.getId()) < pos.getNumPositions();
    }

    public static int passedHoursForTa(String taId, ApplicationService applications,
                                       PositionService positions, String excludeApplicationId) {
        int total = 0;
        for (Application a : applications.getApplicationsByUser(taId)) {
            if (!"PASSED".equals(a.getStatus())) continue;
            if (excludeApplicationId != null && excludeApplicationId.equals(a.getId())) continue;
            Position p = positions.getPositionById(a.getPositionId());
            if (p != null) total += p.getHoursPerWeek();
        }
        return total;
    }

    public static boolean wouldExceedWorkload(String taId, Position position, ApplicationService applications,
                                              PositionService positions, String excludeApplicationId) {
        int current = passedHoursForTa(taId, applications, positions, excludeApplicationId);
        return current + position.getHoursPerWeek() > MAX_HOURS_PER_WEEK;
    }
}
