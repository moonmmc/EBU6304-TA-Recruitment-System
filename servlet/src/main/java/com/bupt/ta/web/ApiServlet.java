package com.bupt.ta.web;

import com.bupt.ta.web.api.ApiContext;
import com.bupt.ta.web.api.ApplicationHandler;
import com.bupt.ta.web.api.AuthHandler;
import com.bupt.ta.web.api.PositionHandler;
import com.bupt.ta.web.api.StatsHandler;
import com.bupt.ta.web.api.UserHandler;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** REST API entry point; routes requests to resource handlers. */
public class ApiServlet extends HttpServlet {

    private final ApiContext ctx = new ApiContext();
    private final AuthHandler auth = new AuthHandler(ctx);
    private final PositionHandler positions = new PositionHandler(ctx);
    private final ApplicationHandler applications = new ApplicationHandler(ctx);
    private final UserHandler users = new UserHandler(ctx);
    private final StatsHandler stats = new StatsHandler(ctx);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        dispatch(req, resp, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        dispatch(req, resp, "POST");
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        dispatch(req, resp, "PUT");
    }

    private void dispatch(HttpServletRequest req, HttpServletResponse resp, String method) throws IOException {
        String path = req.getPathInfo();
        if (path == null) path = "/";

        try {
            if (route(method, path, req, resp)) return;
            WebAuth.sendError(resp, 404, "Not found");
        } catch (Exception e) {
            WebAuth.sendError(resp, 500, e.getMessage());
        }
    }

    private boolean route(String method, String path, HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        switch (method) {
            case "GET":
                return auth.handleGet(path, req, resp)
                        || positions.handleGet(path, req, resp)
                        || applications.handleGet(path, req, resp)
                        || users.handleGet(path, req, resp)
                        || stats.handleGet(path, req, resp);
            case "POST":
                return auth.handlePost(path, req, resp)
                        || positions.handlePost(path, req, resp)
                        || applications.handlePost(path, req, resp);
            case "PUT":
                return positions.handlePut(path, req, resp)
                        || applications.handlePut(path, req, resp)
                        || users.handlePut(path, req, resp);
            default:
                return false;
        }
    }
}
