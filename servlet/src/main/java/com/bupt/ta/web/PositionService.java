package com.bupt.ta.web;

import java.util.ArrayList;
import java.util.List;

public class PositionService {
    private static final String FILE = "data/positions.txt";

    public List<Position> getAllPositions() {
        List<String> lines = FileUtil.read(FILE);
        List<Position> list = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            Position p = Position.fromLine(line);
            if (p != null) list.add(p);
        }
        return list;
    }

    public List<Position> getApprovedPositions() {
        List<Position> result = new ArrayList<>();
        for (Position p : getAllPositions()) {
            if ("APPROVED".equals(p.getStatus())) result.add(p);
        }
        return result;
    }

    public List<Position> getPendingPositions() {
        return getPositionsByStatus("PENDING");
    }

    public List<Position> getPositionsByStatus(String status) {
        List<Position> result = new ArrayList<>();
        for (Position p : getAllPositions()) {
            if (status.equals(p.getStatus())) result.add(p);
        }
        return result;
    }

    /** Approved and still within application deadline (for TA listing). */
    public List<Position> getOpenPositions() {
        List<Position> result = new ArrayList<>();
        for (Position p : getAllPositions()) {
            if (PositionRules.isOpenForTa(p)) result.add(p);
        }
        return result;
    }

    public List<Position> getPositionsByMo(String moId) {
        List<Position> result = new ArrayList<>();
        for (Position p : getAllPositions()) {
            if (p.getMoId().equals(moId)) result.add(p);
        }
        return result;
    }

    public Position getPositionById(String id) {
        for (Position p : getAllPositions()) {
            if (p.getId().equals(id)) return p;
        }
        return null;
    }

    public boolean updatePositionStatus(String posId, String status) {
        List<Position> positions = getAllPositions();
        List<String> lines = new ArrayList<>();
        boolean found = false;
        for (Position p : positions) {
            if (p.getId().equals(posId)) {
                p.setStatus(status);
                found = true;
            }
            lines.add(p.toLine());
        }
        if (found) FileUtil.write(FILE, lines);
        return found;
    }

    public void addPosition(Position pos) {
        List<String> lines = FileUtil.read(FILE);
        lines.add(pos.toLine());
        FileUtil.write(FILE, lines);
    }

    public int generateId() {
        List<String> ids = new ArrayList<>();
        for (Position p : getAllPositions()) ids.add(p.getId());
        return IdSequences.next("POS", ids);
    }
}
