package com.bupt.ta.web.api;

import com.bupt.ta.web.Application;
import com.bupt.ta.web.JsonUtil;
import com.bupt.ta.web.Position;
import com.bupt.ta.web.User;
import com.bupt.ta.web.WebAuth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StatsHandler extends BaseApiHandler {

    public StatsHandler(ApiContext ctx) {
        super(ctx);
    }

    public boolean handleGet(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path.equals("/stats")) {
            handleGetStats(req, resp);
            return true;
        }
        return false;
    }

    private void handleGetStats(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (WebAuth.requireRole(req, resp, "ADMIN") == null) return;

        Map<String, Object> stats = new LinkedHashMap<>();
        List<Position> allPos = ctx.positions.getAllPositions();
        List<Application> allApps = ctx.applications.getAllApplications();
        List<User> allUsers = ctx.users.getAllUsers();

        stats.put("totalPositions", allPos.size());
        int approved = 0, pending = 0;
        for (Position p : allPos) {
            if ("APPROVED".equals(p.getStatus())) approved++;
            if ("PENDING".equals(p.getStatus())) pending++;
        }
        stats.put("approvedPositions", approved);
        stats.put("pendingPositions", pending);
        stats.put("totalApplications", allApps.size());

        int appPending = 0, appPassed = 0, appFailed = 0;
        for (Application a : allApps) {
            if ("PENDING".equals(a.getStatus())) appPending++;
            if ("PASSED".equals(a.getStatus())) appPassed++;
            if ("FAILED".equals(a.getStatus())) appFailed++;
        }
        stats.put("pendingApplications", appPending);
        stats.put("passedApplications", appPassed);
        stats.put("failedApplications", appFailed);

        int taCount = 0, moCount = 0;
        for (User u : allUsers) {
            if ("TA".equals(u.getRole())) taCount++;
            if ("MO".equals(u.getRole())) moCount++;
        }
        stats.put("totalTAs", taCount);
        stats.put("totalMOs", moCount);
        stats.put("totalUsers", allUsers.size());

        sendJson(resp, JsonUtil.toJson(stats));
    }
}
