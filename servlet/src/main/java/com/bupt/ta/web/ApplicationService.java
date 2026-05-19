package com.bupt.ta.web;

import java.util.ArrayList;
import java.util.List;

public class ApplicationService {
    private static final String FILE = "data/applications.txt";

    public List<Application> getAllApplications() {
        List<String> lines = FileUtil.read(FILE);
        List<Application> list = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            Application a = Application.fromLine(line);
            if (a != null) list.add(a);
        }
        return list;
    }

    public List<Application> getApplicationsByUser(String taId) {
        List<Application> result = new ArrayList<>();
        for (Application a : getAllApplications()) {
            if (a.getTaId().equals(taId)) result.add(a);
        }
        return result;
    }

    public List<Application> getApplicationsByPosition(String positionId) {
        List<Application> result = new ArrayList<>();
        for (Application a : getAllApplications()) {
            if (a.getPositionId().equals(positionId)) result.add(a);
        }
        return result;
    }

    public Application getApplicationById(String appId) {
        for (Application a : getAllApplications()) {
            if (a.getId().equals(appId)) return a;
        }
        return null;
    }

    public boolean hasApplied(String taId, String positionId) {
        for (Application a : getAllApplications()) {
            if (a.getTaId().equals(taId) && a.getPositionId().equals(positionId)) return true;
        }
        return false;
    }

    public int countPassedForPosition(String positionId) {
        int count = 0;
        for (Application a : getAllApplications()) {
            if (a.getPositionId().equals(positionId) && "PASSED".equals(a.getStatus())) count++;
        }
        return count;
    }

    public boolean updateApplicationStatus(String appId, String status, String feedback) {
        List<Application> apps = getAllApplications();
        List<String> lines = new ArrayList<>();
        boolean found = false;
        for (Application a : apps) {
            if (a.getId().equals(appId)) {
                a.setStatus(status);
                a.setFeedback(feedback);
                found = true;
            }
            lines.add(a.toLine());
        }
        if (found) FileUtil.write(FILE, lines);
        return found;
    }

    public boolean updateApplicationStatus(String appId, String status) {
        return updateApplicationStatus(appId, status, "");
    }

    public boolean createApplication(Application app) {
        List<String> lines = FileUtil.read(FILE);
        lines.add(app.toLine());
        FileUtil.write(FILE, lines);
        return true;
    }

    public int generateId() {
        List<String> ids = new ArrayList<>();
        for (Application a : getAllApplications()) ids.add(a.getId());
        return IdSequences.next("APP", ids);
    }
}
