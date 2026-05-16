package com.bupt.ta.web.api;

import com.bupt.ta.web.JsonUtil;
import com.bupt.ta.web.User;
import com.bupt.ta.web.WebAuth;
import com.bupt.ta.web.WebAuth.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class UserHandler extends BaseApiHandler {

    public UserHandler(ApiContext ctx) {
        super(ctx);
    }

    public boolean handleGet(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path.equals("/users")) {
            handleGetUsers(req, resp);
            return true;
        }
        if (path.startsWith("/users/") && !path.endsWith("/password")) {
            handleGetUser(req, resp, path);
            return true;
        }
        return false;
    }

    public boolean handlePut(String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (path.matches("/users/.+/password")) {
            handleChangePassword(req, resp, path);
            return true;
        }
        if (path.matches("/users/.+")) {
            handleUpdateUser(req, resp, path);
            return true;
        }
        return false;
    }

    private void handleGetUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (WebAuth.requireRole(req, resp, "ADMIN") == null) return;

        String role = req.getParameter("role");
        List<User> users;
        if (role != null && !role.isEmpty()) {
            users = new ArrayList<>();
            for (User u : ctx.users.getAllUsers()) {
                if (u.getRole().equals(role)) users.add(u);
            }
        } else {
            users = ctx.users.getAllUsers();
        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (User u : users) list.add(JsonUtil.userToMap(u));
        sendJson(resp, JsonUtil.toJsonArray(list));
    }

    private void handleGetUser(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        SessionUser user = WebAuth.requireLogin(req, resp);
        if (user == null) return;

        String rawId = path.replace("/users/", "");
        if (rawId.contains("/")) return;

        User target = ctx.users.getUserById(rawId);
        if (target == null) {
            sendError(resp, 404, "User not found");
            return;
        }

        if (user.userId.equals(rawId)) {
            sendJson(resp, JsonUtil.toJson(JsonUtil.userToMap(target)));
            return;
        }
        if ("ADMIN".equals(user.role)) {
            sendJson(resp, JsonUtil.toJson(JsonUtil.userToMap(target)));
            return;
        }
        if ("MO".equals(user.role) && "TA".equals(target.getRole())) {
            sendJson(resp, JsonUtil.toJson(JsonUtil.userToMap(target)));
            return;
        }

        sendError(resp, 403, "Cannot view this user profile");
    }

    private void handleUpdateUser(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        SessionUser user = WebAuth.requireLogin(req, resp);
        if (user == null) return;

        String id = path.replace("/users/", "");
        if (id.contains("/")) return;
        if (!WebAuth.requireSelfOrAdmin(user, id, resp)) return;

        User existing = ctx.users.getUserById(id);
        if (existing == null) {
            sendError(resp, 404, "User not found");
            return;
        }

        String body = JsonUtil.readBody(req);
        String name = JsonUtil.getJsonString(body, "name");
        if (!name.isEmpty()) existing.setName(name);
        String phone = JsonUtil.getJsonString(body, "phone");
        if (!phone.isEmpty()) existing.setPhone(phone);
        String programme = JsonUtil.getJsonString(body, "programme");
        if (!programme.isEmpty()) existing.setProgramme(programme);
        String yearOfStudy = JsonUtil.getJsonString(body, "yearOfStudy");
        if (!yearOfStudy.isEmpty()) existing.setYearOfStudy(yearOfStudy);
        String skills = JsonUtil.getJsonString(body, "skills");
        if (!skills.isEmpty()) existing.setSkills(skills);
        String experience = JsonUtil.getJsonString(body, "experience");
        if (!experience.isEmpty()) existing.setExperience(experience);
        String university = JsonUtil.getJsonString(body, "university");
        if (!university.isEmpty()) existing.setUniversity(university);
        String gpa = JsonUtil.getJsonString(body, "gpa");
        if (!gpa.isEmpty()) existing.setGpa(gpa);
        String studentId = JsonUtil.getJsonString(body, "studentId");
        if (!studentId.isEmpty()) existing.setStudentId(studentId);
        String email = JsonUtil.getJsonString(body, "email");
        if (!email.isEmpty()) {
            if (ctx.users.isEmailUsedByOther(existing.getId(), email)) {
                sendError(resp, 409, "Email already in use");
                return;
            }
            existing.setEmail(email);
        }

        ctx.users.updateUser(existing);
        sendJson(resp, JsonUtil.toJson(JsonUtil.userToMap(existing)));
    }

    private void handleChangePassword(HttpServletRequest req, HttpServletResponse resp, String path)
            throws IOException {
        SessionUser user = WebAuth.requireLogin(req, resp);
        if (user == null) return;

        String id = path.split("/")[2];
        if (!WebAuth.requireSelfOrAdmin(user, id, resp)) return;

        String body = JsonUtil.readBody(req);
        String oldPwd = JsonUtil.getJsonString(body, "oldPassword");
        String newPwd = JsonUtil.getJsonString(body, "newPassword");
        boolean ok = ctx.users.changePassword(id, oldPwd, newPwd);
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("success", ok);
        if (!ok) r.put("message", "Old password incorrect");
        sendJson(resp, JsonUtil.toJson(r));
    }
}
