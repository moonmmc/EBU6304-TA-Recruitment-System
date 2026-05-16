package com.bupt.ta.web.api;

import com.bupt.ta.web.JsonUtil;
import com.bupt.ta.web.Position;
import com.bupt.ta.web.PositionRules;
import com.bupt.ta.web.User;
import com.bupt.ta.web.WebAuth;
import com.bupt.ta.web.WebAuth.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PositionHandler extends BaseApiHandler {

    public PositionHandler(ApiContext ctx) {
        super(ctx);
    }

    public boolean handleGet(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path.equals("/positions")) {
            handleGetPositions(req, resp);
            return true;
        }
        if (path.startsWith("/positions/") && !path.endsWith("/status")) {
            handleGetPosition(req, resp, path);
            return true;
        }
        return false;
    }

    public boolean handlePost(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path.equals("/positions")) {
            handleCreatePosition(req, resp);
            return true;
        }
        return false;
    }

    public boolean handlePut(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path.matches("/positions/.+/status")) {
            handleUpdatePositionStatus(req, resp, path);
            return true;
        }
        return false;
    }

    private void handleGetPositions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser user = WebAuth.requireLogin(req, resp);
        if (user == null) return;

        String status = req.getParameter("status");
        String moId = req.getParameter("moId");
        List<Position> positions;

        if ("ADMIN".equals(user.role)) {
            if (moId != null && !moId.isEmpty()) {
                positions = ctx.positions.getPositionsByMo(moId);
                if (status != null && !status.isEmpty()) {
                    positions = filterPositionsByStatus(positions, status);
                }
            } else if (status != null && !status.isEmpty()) {
                positions = ctx.positions.getPositionsByStatus(status);
            } else {
                positions = ctx.positions.getAllPositions();
            }
        } else if ("MO".equals(user.role)) {
            String targetMoId = (moId != null && !moId.isEmpty()) ? moId : user.userId;
            if (!user.userId.equals(targetMoId)) {
                sendError(resp, 403, "Cannot view another module owner's positions");
                return;
            }
            positions = ctx.positions.getPositionsByMo(targetMoId);
            if (status != null && !status.isEmpty()) {
                positions = filterPositionsByStatus(positions, status);
            }
        } else {
            if (moId != null && !moId.isEmpty()) {
                sendError(resp, 403, "Insufficient permissions");
                return;
            }
            if (status != null && !status.isEmpty() && !"OPEN".equals(status) && !"APPROVED".equals(status)) {
                sendError(resp, 403, "Insufficient permissions");
                return;
            }
            positions = ctx.positions.getOpenPositions();
        }

        sendJson(resp, JsonUtil.toJsonArray(toPositionMaps(positions)));
    }

    private void handleGetPosition(HttpServletRequest req, HttpServletResponse resp, String path)
            throws IOException {
        SessionUser user = WebAuth.requireLogin(req, resp);
        if (user == null) return;

        String id = path.replace("/positions/", "");
        Position p = ctx.positions.getPositionById(id);
        if (p == null) {
            sendError(resp, 404, "Position not found");
            return;
        }
        if (!canViewPosition(user, p)) {
            sendError(resp, 403, "Cannot view this position");
            return;
        }
        sendJson(resp, JsonUtil.toJson(JsonUtil.positionToMap(p)));
    }

    private void handleCreatePosition(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser user = WebAuth.requireRole(req, resp, "MO");
        if (user == null) return;

        User mo = ctx.users.getUserById(user.userId);
        String body = JsonUtil.readBody(req);
        String courseName = JsonUtil.getJsonString(body, "courseName");
        String courseCode = JsonUtil.getJsonString(body, "courseCode");
        String department = JsonUtil.getJsonString(body, "department");
        int numPositions = parseInt(JsonUtil.getJsonString(body, "numPositions"), 1);
        int hoursPerWeek = parseInt(JsonUtil.getJsonString(body, "hoursPerWeek"), 0);
        String payRate = JsonUtil.getJsonString(body, "payRate");
        String deadline = JsonUtil.getJsonString(body, "deadline");
        String requiredSkills = JsonUtil.getJsonString(body, "requiredSkills");
        String duties = JsonUtil.getJsonString(body, "duties");

        String id = "POS" + String.format("%03d", ctx.positions.generateId());
        Position pos = new Position(id, user.userId, mo != null ? mo.getName() : user.userId,
                courseName, courseCode, department, numPositions, hoursPerWeek, payRate,
                deadline, requiredSkills, duties, "PENDING");
        ctx.positions.addPosition(pos);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("id", id);
        sendJson(resp, JsonUtil.toJson(result));
    }

    private void handleUpdatePositionStatus(HttpServletRequest req, HttpServletResponse resp, String path)
            throws IOException {
        SessionUser user = WebAuth.requireLogin(req, resp);
        if (user == null) return;

        String id = path.split("/")[2];
        Position pos = ctx.positions.getPositionById(id);
        if (pos == null) {
            sendError(resp, 404, "Position not found");
            return;
        }

        String body = JsonUtil.readBody(req);
        String status = JsonUtil.getJsonString(body, "status");
        String err = PositionRules.validateTransition(user.role, user.userId, pos, status);
        if (err != null) {
            sendError(resp, 400, err);
            return;
        }

        boolean ok = ctx.positions.updatePositionStatus(id, status);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("success", ok);
        sendJson(resp, JsonUtil.toJson(r));
    }

    private boolean canViewPosition(SessionUser user, Position p) {
        if ("ADMIN".equals(user.role)) return true;
        if ("MO".equals(user.role) && user.userId.equals(p.getMoId())) return true;
        if ("TA".equals(user.role)) return PositionRules.isOpenForTa(p);
        return false;
    }
}
