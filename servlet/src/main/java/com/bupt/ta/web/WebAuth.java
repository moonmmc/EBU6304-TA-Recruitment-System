package com.bupt.ta.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/** Session-based authorization for REST API handlers. */
public final class WebAuth {

    public static final class SessionUser {
        public final String userId;
        public final String role;

        SessionUser(String userId, String role) {
            this.userId = userId;
            this.role = role;
        }

        public boolean hasRole(String... roles) {
            for (String r : roles) {
                if (r.equals(role)) return true;
            }
            return false;
        }
    }

    private WebAuth() {}

    public static SessionUser current(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object userId = session.getAttribute("userId");
        Object role = session.getAttribute("role");
        if (userId == null || role == null) return null;
        return new SessionUser((String) userId, (String) role);
    }

    public static SessionUser requireLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser user = current(req);
        if (user == null) {
            sendError(resp, 401, "Not logged in");
            return null;
        }
        return user;
    }

    public static SessionUser requireRole(HttpServletRequest req, HttpServletResponse resp, String... roles)
            throws IOException {
        SessionUser user = requireLogin(req, resp);
        if (user == null) return null;
        if (!user.hasRole(roles)) {
            sendError(resp, 403, "Insufficient permissions");
            return null;
        }
        return user;
    }

    public static boolean requireSelfOrAdmin(SessionUser user, String targetUserId, HttpServletResponse resp)
            throws IOException {
        if (user.userId.equals(targetUserId) || "ADMIN".equals(user.role)) return true;
        sendError(resp, 403, "Cannot access another user's data");
        return false;
    }

    public static boolean requireMoOwnsPosition(SessionUser user, Position position, HttpServletResponse resp)
            throws IOException {
        if (position == null) {
            sendError(resp, 404, "Position not found");
            return false;
        }
        if ("ADMIN".equals(user.role)) return true;
        if ("MO".equals(user.role) && user.userId.equals(position.getMoId())) return true;
        sendError(resp, 403, "Cannot access this position");
        return false;
    }

    public static void sendError(HttpServletResponse resp, int code, String msg) throws IOException {
        resp.setStatus(code);
        Map<String, Object> err = new LinkedHashMap<>();
        err.put("error", msg);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try (PrintWriter out = resp.getWriter()) {
            out.print(JsonUtil.toJson(err));
        }
    }
}
