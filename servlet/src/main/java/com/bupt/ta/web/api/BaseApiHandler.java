package com.bupt.ta.web.api;

import com.bupt.ta.web.Application;
import com.bupt.ta.web.JsonUtil;
import com.bupt.ta.web.Position;
import com.bupt.ta.web.WebAuth;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract class BaseApiHandler {
    protected final ApiContext ctx;

    protected BaseApiHandler(ApiContext ctx) {
        this.ctx = ctx;
    }

    protected void sendJson(HttpServletResponse resp, String json) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(json);
        out.flush();
    }

    protected void sendError(HttpServletResponse resp, int code, String msg) throws IOException {
        WebAuth.sendError(resp, code, msg);
    }

    protected void sendSuccess(HttpServletResponse resp) throws IOException {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("success", true);
        sendJson(resp, JsonUtil.toJson(r));
    }

    protected int parseInt(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }

    protected List<Map<String, Object>> toPositionMaps(List<Position> positions) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Position p : positions) list.add(JsonUtil.positionToMap(p));
        return list;
    }

    protected List<Map<String, Object>> toApplicationMaps(List<Application> apps) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Application a : apps) list.add(JsonUtil.applicationToMap(a));
        return list;
    }

    protected List<Position> filterPositionsByStatus(List<Position> positions, String status) {
        List<Position> filtered = new ArrayList<>();
        for (Position p : positions) {
            if (status.equals(p.getStatus())) filtered.add(p);
        }
        return filtered;
    }
}
