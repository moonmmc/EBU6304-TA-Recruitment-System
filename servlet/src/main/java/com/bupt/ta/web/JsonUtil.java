package com.bupt.ta.web;

import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.BufferedReader;
import jakarta.servlet.http.HttpServletRequest;

public class JsonUtil {

    public static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escape(e.getKey())).append("\":");
            sb.append(valueToJson(e.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    public static String toJsonArray(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(toJson(list.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    private static String valueToJson(Object val) {
        if (val == null) return "null";
        if (val instanceof Number) return val.toString();
        if (val instanceof Boolean) return val.toString();
        if (val instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) val;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(valueToJson(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        if (val instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) val;
            return toJson(map);
        }
        return "\"" + escape(val.toString()) + "\"";
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    public static Map<String, Object> userToMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("name", u.getName());
        m.put("role", u.getRole());
        m.put("email", u.getEmail());
        m.put("phone", u.getPhone());
        m.put("programme", u.getProgramme());
        m.put("yearOfStudy", u.getYearOfStudy());
        m.put("skills", u.getSkills());
        m.put("experience", u.getExperience());
        m.put("cvFileName", u.getCvFileName());
        m.put("university", u.getUniversity());
        m.put("gpa", u.getGpa());
        m.put("studentId", u.getStudentId());
        return m;
    }

    public static Map<String, Object> positionToMap(Position p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("moId", p.getMoId());
        m.put("moName", p.getMoName());
        m.put("courseName", p.getCourseName());
        m.put("courseCode", p.getCourseCode());
        m.put("department", p.getDepartment());
        m.put("numPositions", p.getNumPositions());
        m.put("hoursPerWeek", p.getHoursPerWeek());
        m.put("payRate", p.getPayRate());
        m.put("deadline", p.getDeadline());
        m.put("requiredSkills", p.getRequiredSkills());
        m.put("duties", p.getDuties());
        m.put("status", p.getStatus());
        return m;
    }

    public static Map<String, Object> applicationToMap(Application a) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.getId());
        m.put("taId", a.getTaId());
        m.put("taName", a.getTaName());
        m.put("positionId", a.getPositionId());
        m.put("positionTitle", a.getPositionTitle());
        m.put("courseCode", a.getCourseCode());
        m.put("appliedDate", a.getAppliedDate());
        m.put("status", a.getStatus());
        m.put("feedback", a.getFeedback());
        return m;
    }

    public static String readBody(HttpServletRequest req) {
        try {
            BufferedReader reader = req.getReader();
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static String getJsonString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        idx = json.indexOf(":", idx) + 1;
        while (idx < json.length() && json.charAt(idx) == ' ') idx++;
        if (idx >= json.length()) return "";
        if (json.charAt(idx) == '"') {
            int end = json.indexOf('"', idx + 1);
            return end > idx ? json.substring(idx + 1, end) : "";
        }
        int end = idx;
        while (end < json.length() && json.charAt(end) != ',' && json.charAt(end) != '}') end++;
        return json.substring(idx, end).trim();
    }
}
