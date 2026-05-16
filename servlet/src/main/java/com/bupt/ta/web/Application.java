package com.bupt.ta.web;

public class Application {
    private String id;
    private String taId;
    private String taName;
    private String positionId;
    private String positionTitle;
    private String courseCode;
    private String appliedDate;
    private String status;      // PENDING, PASSED, FAILED
    private String feedback;

    public Application(String id, String taId, String taName, String positionId,
                       String positionTitle, String courseCode, String appliedDate,
                       String status, String feedback) {
        this.id = id;
        this.taId = taId;
        this.taName = taName;
        this.positionId = positionId;
        this.positionTitle = positionTitle;
        this.courseCode = courseCode;
        this.appliedDate = appliedDate;
        this.status = status;
        this.feedback = feedback;
    }

    public String getId() { return id; }
    public String getTaId() { return taId; }
    public String getTaName() { return taName; }
    public String getPositionId() { return positionId; }
    public String getPositionTitle() { return positionTitle; }
    public String getCourseCode() { return courseCode; }
    public String getAppliedDate() { return appliedDate; }
    public String getStatus() { return status; }
    public String getFeedback() { return feedback; }

    public void setStatus(String status) { this.status = status; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public String toLine() {
        return String.join("|", id, taId, taName, positionId, positionTitle,
                courseCode, appliedDate, status, feedback);
    }

    public static Application fromLine(String line) {
        String[] s = line.split("\\|", -1);
        if (s.length < 9) return null;
        return new Application(s[0], s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8]);
    }
}
