package com.bupt.ta.web;

public class Position {
    private String id;
    private String moId;
    private String moName;
    private String courseName;
    private String courseCode;
    private String department;
    private int numPositions;
    private int hoursPerWeek;
    private String payRate;
    private String deadline;
    private String requiredSkills;  // semicolon-separated
    private String duties;
    private String status;          // PENDING, APPROVED, REJECTED, CLOSED

    public Position(String id, String moId, String moName, String courseName, String courseCode,
                    String department, int numPositions, int hoursPerWeek, String payRate,
                    String deadline, String requiredSkills, String duties, String status) {
        this.id = id;
        this.moId = moId;
        this.moName = moName;
        this.courseName = courseName;
        this.courseCode = courseCode;
        this.department = department;
        this.numPositions = numPositions;
        this.hoursPerWeek = hoursPerWeek;
        this.payRate = payRate;
        this.deadline = deadline;
        this.requiredSkills = requiredSkills;
        this.duties = duties;
        this.status = status;
    }

    public String getId() { return id; }
    public String getMoId() { return moId; }
    public String getMoName() { return moName; }
    public String getCourseName() { return courseName; }
    public String getCourseCode() { return courseCode; }
    public String getDepartment() { return department; }
    public int getNumPositions() { return numPositions; }
    public int getHoursPerWeek() { return hoursPerWeek; }
    public String getPayRate() { return payRate; }
    public String getDeadline() { return deadline; }
    public String getRequiredSkills() { return requiredSkills; }
    public String getDuties() { return duties; }
    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String toLine() {
        return String.join("|", id, moId, moName, courseName, courseCode, department,
                String.valueOf(numPositions), String.valueOf(hoursPerWeek), payRate,
                deadline, requiredSkills, duties.replace("\n", "\\n"), status);
    }

    public static Position fromLine(String line) {
        String[] s = line.split("\\|", -1);
        if (s.length < 13) return null;
        try {
            return new Position(s[0], s[1], s[2], s[3], s[4], s[5],
                    Integer.parseInt(s[6]), Integer.parseInt(s[7]), s[8],
                    s[9], s[10], s[11].replace("\\n", "\n"), s[12]);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
