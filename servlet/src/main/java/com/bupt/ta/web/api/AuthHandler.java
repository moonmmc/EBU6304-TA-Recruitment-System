package com.bupt.ta.web.api;

import com.bupt.ta.web.JsonUtil;
import com.bupt.ta.web.User;
import com.bupt.ta.web.WebAuth;
import com.bupt.ta.web.WebAuth.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AuthHandler extends BaseApiHandler {

    public AuthHandler(ApiContext ctx) {
        super(ctx);
    }

    public boolean handleGet(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path.equals("/session")) {
            handleSession(req, resp);
            return true;
        }
        return false;
    }

    public boolean handlePost(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        switch (path) {
            case "/login":
                handleLogin(req, resp);
                return true;
            case "/register":
                handleRegister(req, resp);
                return true;
            case "/logout":
                handleLogout(req, resp);
                return true;
            default:
                return false;
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = JsonUtil.readBody(req);
        String userId = JsonUtil.getJsonString(body, "userId");
        String password = JsonUtil.getJsonString(body, "password");
        User user = ctx.users.login(userId, password);
        if (user == null) {
            sendError(resp, 401, "Invalid credentials");
            return;
        }
        HttpSession session = req.getSession(true);
        session.setAttribute("userId", user.getId());
        session.setAttribute("role", user.getRole());
        sendJson(resp, JsonUtil.toJson(JsonUtil.userToMap(user)));
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = JsonUtil.readBody(req);
        String name = JsonUtil.getJsonString(body, "name");
        String password = JsonUtil.getJsonString(body, "password");
        String email = JsonUtil.getJsonString(body, "email");
        String phone = JsonUtil.getJsonString(body, "phone");
        String programme = JsonUtil.getJsonString(body, "programme");
        String studentId = JsonUtil.getJsonString(body, "studentId");
        String id = "TA" + String.format("%03d", ctx.users.generateTAId());
        User user = User.builder()
                .id(id)
                .name(name)
                .password(password)
                .role("TA")
                .email(email)
                .phone(phone)
                .programme(programme)
                .studentId(studentId)
                .build();
        if (!ctx.users.register(user)) {
            sendError(resp, 409, "Email already exists");
            return;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("success", true);
        result.put("userId", id);
        sendJson(resp, JsonUtil.toJson(result));
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser user = WebAuth.requireLogin(req, resp);
        if (user == null) return;
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        sendSuccess(resp);
    }

    private void handleSession(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        SessionUser sessionUser = WebAuth.requireLogin(req, resp);
        if (sessionUser == null) return;
        User user = ctx.users.getUserById(sessionUser.userId);
        if (user == null) {
            sendError(resp, 401, "User not found");
            return;
        }
        sendJson(resp, JsonUtil.toJson(JsonUtil.userToMap(user)));
    }
}
