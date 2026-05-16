# EBU6304 TA Recruitment System

> BUPT International School - Teaching Assistant Recruitment System  
> Group 73 | EBU6304 Software Engineering

## Team Members

| Github User Name | QMID |
| ---------------- | ---- |
| BUPT-XJT | 231226820 |
| liugaoyuan2-bit | 231226761 |
| 12q23q | 221169140 |
| moonmmc | 231226417 |
| irving-cell | 231226381 |
| soyomoon | 231226428 |

## Project Structure

```
servlet/                          # Maven project (Web only)
├── pom.xml
├── data/                         # File-based storage (txt)
│   ├── users.txt
│   ├── positions.txt
│   └── applications.txt
└── src/
    ├── main/java/com/bupt/ta/web/
    │   ├── WebServerMain.java    # Jetty entry point
    │   ├── ApiServlet.java       # REST API (/api/*)
    │   ├── *Service.java         # Business logic
    │   └── *.java                # Domain models & utilities
    └── webapp/                   # Static frontend
        ├── index.html            # Login
        ├── register.html
        ├── css/, js/
        ├── ta/                   # TA pages
        ├── mo/                   # Module Owner pages
        ├── admin/                # Admin pages
        └── jsp/                  # Server-side JSP demo
```

## Run

From the `servlet` directory:

```bash
mvn compile exec:java -Dexec.mainClass=com.bupt.ta.web.WebServerMain
```

Or in VS Code / Cursor: run **Web (Jetty) — WebServerMain** (working directory is `servlet`).

Open http://localhost:8080

**Demo accounts:** TA001 / 123 · MO001 / 123 · ADMIN001 / 123
