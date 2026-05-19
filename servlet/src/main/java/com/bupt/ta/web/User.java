package com.bupt.ta.web;

public class User {
    private String id;
    private String name;
    private String password;
    private String role;       // TA, MO, ADMIN
    private String email;
    private String phone;
    private String programme;
    private String yearOfStudy;
    private String skills;     // semicolon-separated, e.g. "Java;Python;SQL"
    private String experience;
    private String cvFileName;
    private String university;
    private String gpa;
    /** University student number (separate from login account id). */
    private String studentId;

    private User(Builder b) {
        this.id = b.id;
        this.name = b.name;
        this.password = b.password;
        this.role = b.role;
        this.email = b.email;
        this.phone = empty(b.phone);
        this.programme = empty(b.programme);
        this.yearOfStudy = empty(b.yearOfStudy);
        this.skills = empty(b.skills);
        this.experience = empty(b.experience);
        this.cvFileName = empty(b.cvFileName);
        this.university = empty(b.university);
        this.gpa = empty(b.gpa);
        this.studentId = empty(b.studentId);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String name;
        private String password;
        private String role;
        private String email;
        private String phone = "";
        private String programme = "";
        private String yearOfStudy = "";
        private String skills = "";
        private String experience = "";
        private String cvFileName = "";
        private String university = "";
        private String gpa = "";
        private String studentId = "";

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder password(String password) { this.password = password; return this; }
        public Builder role(String role) { this.role = role; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder programme(String programme) { this.programme = programme; return this; }
        public Builder yearOfStudy(String yearOfStudy) { this.yearOfStudy = yearOfStudy; return this; }
        public Builder skills(String skills) { this.skills = skills; return this; }
        public Builder experience(String experience) { this.experience = experience; return this; }
        public Builder cvFileName(String cvFileName) { this.cvFileName = cvFileName; return this; }
        public Builder university(String university) { this.university = university; return this; }
        public Builder gpa(String gpa) { this.gpa = gpa; return this; }
        public Builder studentId(String studentId) { this.studentId = studentId; return this; }

        public User build() {
            return new User(this);
        }
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getProgramme() { return programme; }
    public String getYearOfStudy() { return yearOfStudy; }
    public String getSkills() { return skills; }
    public String getExperience() { return experience; }
    public String getCvFileName() { return cvFileName; }
    public String getUniversity() { return university; }
    public String getGpa() { return gpa; }
    public String getStudentId() { return studentId; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setPassword(String password) { this.password = password; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setProgramme(String programme) { this.programme = programme; }
    public void setYearOfStudy(String yearOfStudy) { this.yearOfStudy = yearOfStudy; }
    public void setSkills(String skills) { this.skills = skills; }
    public void setExperience(String experience) { this.experience = experience; }
    public void setCvFileName(String cvFileName) { this.cvFileName = cvFileName; }
    public void setUniversity(String university) { this.university = university; }
    public void setGpa(String gpa) { this.gpa = gpa; }

    public String toLine() {
        return String.join("|", id, name, password, role, email, phone,
                programme, yearOfStudy, skills, experience, cvFileName, university, gpa, studentId);
    }

    public static User fromLine(String line) {
        String[] s = line.split("\\|", -1);
        if (s.length < 5) return null;
        return builder()
                .id(s[0])
                .name(s[1])
                .password(s[2])
                .role(s[3])
                .email(s[4])
                .phone(s.length > 5 ? s[5] : "")
                .programme(s.length > 6 ? s[6] : "")
                .yearOfStudy(s.length > 7 ? s[7] : "")
                .skills(s.length > 8 ? s[8] : "")
                .experience(s.length > 9 ? s[9] : "")
                .cvFileName(s.length > 10 ? s[10] : "")
                .university(s.length > 11 ? s[11] : "")
                .gpa(s.length > 12 ? s[12] : "")
                .studentId(s.length > 13 ? s[13] : "")
                .build();
    }

    private static String empty(String value) {
        return value != null ? value : "";
    }
}
