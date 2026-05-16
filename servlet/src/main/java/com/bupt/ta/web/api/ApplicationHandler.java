package com.bupt.ta.web.api;

import com.bupt.ta.web.Application;
import com.bupt.ta.web.ApplicationRules;
import com.bupt.ta.web.JsonUtil;
import com.bupt.ta.web.Position;
import com.bupt.ta.web.User;
import com.bupt.ta.web.WebAuth;
import com.bupt.ta.web.WebAuth.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class ApplicationHandler extends BaseApiHandler {

    public ApplicationHandler(ApiContext ctx) {
        super(ctx);
    }

    public boolean handleGet(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path.equals("/applications")) {
            handleGetApplications(req, resp);
            return true;
        }
        return false;
    }

    public boolean handlePost(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path.equals("/applications")) {
            handleCreateApplication(req, resp);
            return true;
        }
        return false;
    }

    public boolean handlePut(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path.matches("/applications/.+/status")) {
            handleUpdateApplicationStatus(req, resp, path);
            return true;
        }
        return false;
    }

    private void handleGetApplications(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser user = WebAuth.requireLogin(req, resp);
        if (user == null) return;

        String taId = req.getParameter("taId");
        String positionId = req.getParameter("positionId");
        List<Application> apps;

        if ("ADMIN".equals(user.role)) {
            if (taId != null && !taId.isEmpty()) {
                apps = ctx.applications.getApplicationsByUser(taId);
            } else if (positionId != null && !positionId.isEmpty()) {
                apps = ctx.applications.getApplicationsByPosition(positionId);
            } else {
                apps = ctx.applications.getAllApplications();
            }
        } else if ("MO".equals(user.role)) {
            if (taId != null && !taId.isEmpty()) {
                sendError(resp, 403, "Insufficient permissions");
                return;
            }
            if (positionId != null && !positionId.isEmpty()) {
                Position pos = ctx.positions.getPositionById(positionId);
                if (!WebAuth.requireMoOwnsPosition(user, pos, resp)) return;
                apps = ctx.applications.getApplicationsByPosition(positionId);
            } else {
                apps = applicationsForMo(user.userId);
            }
        } else {
            if (positionId != null && !positionId.isEmpty()) {
                sendError(resp, 403, "Insufficient permissions");
                return;
            }
            String targetTaId = (taId != null && !taId.isEmpty()) ? taId : user.userId;
            if (!user.userId.equals(targetTaId)) {
                sendError(resp, 403, "Cannot view another user's applications");
                return;
            }
            apps = ctx.applications.getApplicationsByUser(targetTaId);
        }

        sendJson(resp, JsonUtil.toJsonArray(toApplicationMaps(apps)));
    }

    private void handleCreateApplication(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser user = WebAuth.requireRole(req, resp, "TA");
        if (user == null) return;

        String body = JsonUtil.readBody(req);
        String positionId = JsonUtil.getJsonString(body, "positionId");
        if (positionId.isEmpty()) {
            sendError(resp, 400, "positionId is required");
            return;
        }

        Position pos = ctx.positions.getPositionById(positionId);
        if (pos == null) {
            sendError(resp, 404, "Position not found");
            return;
        }
        if (!ApplicationRules.isOpenForApplication(pos)) {
            String msg = ApplicationRules.isPastDeadline(pos.getDeadline())
                    ? "Application deadline has passed"
                    : "Position is not open for applications";
            sendError(resp, 400, msg);
            return;
        }

        User ta = ctx.users.getUserById(user.userId);
        String taName = ta != null ? ta.getName() : user.userId;

        if (ctx.applications.hasApplied(user.userId, positionId)) {
            sendError(resp, 409, "Already applied");
            return;
        }

        String id = "APP" + String.format("%03d", ctx.applications.generateId());
        String date = LocalDate.now().toString();
        Application app = new Application(id, user.userId, taName, positionId,
                ApplicationRules.positionTitle(pos), pos.getCourseCode(),
                date, "PENDING", "Under review");
        ctx.applications.createApplication(app);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("id", id);
        sendJson(resp, JsonUtil.toJson(result));
    }

    private void handleUpdateApplicationStatus(HttpServletRequest req, HttpServletResponse resp, String path)
            throws IOException {
        SessionUser user = WebAuth.requireRole(req, resp, "MO");
        if (user == null) return;

        String id = path.split("/")[2];
        Application app = ctx.applications.getApplicationById(id);
        if (app == null) {
            sendError(resp, 404, "Application not found");
            return;
        }
        Position pos = ctx.positions.getPositionById(app.getPositionId());
        if (!WebAuth.requireMoOwnsPosition(user, pos, resp)) return;

        String body = JsonUtil.readBody(req);
        String status = JsonUtil.getJsonString(body, "status");
        String feedback = JsonUtil.getJsonString(body, "feedback");
        if (!ApplicationRules.isValidStatus(status)) {
            sendError(resp, 400, "Invalid status");
            return;
        }

        boolean becomingPassed = "PASSED".equals(status) && !"PASSED".equals(app.getStatus());
        if (becomingPassed) {
            if (!ApplicationRules.hasVacancy(pos, ctx.applications)) {
                sendError(resp, 400, "All positions for this role are filled");
                return;
            }
            if (ApplicationRules.wouldExceedWorkload(app.getTaId(), pos, ctx.applications,
                    ctx.positions, app.getId())) {
                sendError(resp, 400,
                        "TA would exceed " + ApplicationRules.MAX_HOURS_PER_WEEK + " hours/week workload limit");
                return;
            }
        }

        boolean ok = ctx.applications.updateApplicationStatus(id, status, feedback);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("success", ok);
        sendJson(resp, JsonUtil.toJson(r));
    }

    private List<Application> applicationsForMo(String moId) {
        List<Application> result = new ArrayList<>();
        for (Application a : ctx.applications.getAllApplications()) {
            Position p = ctx.positions.getPositionById(a.getPositionId());
            if (p != null && moId.equals(p.getMoId())) result.add(a);
        }
        return result;
    }
}
