package com.bupt.ta.web.api;

import com.bupt.ta.web.ApplicationService;
import com.bupt.ta.web.PositionService;
import com.bupt.ta.web.UserService;

/** Shared service instances for API handlers. */
public final class ApiContext {
    public final UserService users = new UserService();
    public final PositionService positions = new PositionService();
    public final ApplicationService applications = new ApplicationService();
}
