<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.bupt.ta.web.Position" %>
<%@ page import="com.bupt.ta.web.PositionService" %>
<%@ page import="java.util.List" %>
<%
    List<Position> positions = new PositionService().getOpenPositions();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Approved positions (JSP) - BUPT TA</title>
    <link rel="stylesheet" href="/css/style.css">
    <style>
        .jsp-wrap { max-width: 960px; margin: 2rem auto; padding: 0 1rem; }
        .jsp-wrap h1 { font-size: 1.5rem; margin-bottom: 0.5rem; }
        .jsp-note { color: #64748b; font-size: 0.9rem; margin-bottom: 1.5rem; }
        table.jsp-table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,.08); }
        .jsp-table th, .jsp-table td { padding: 10px 12px; text-align: left; border-bottom: 1px solid #e2e8f0; }
        .jsp-table th { background: #f8fafc; font-weight: 600; }
        .jsp-back { display: inline-block; margin-top: 1.5rem; color: #2563eb; }
    </style>
</head>
<body>
<div class="jsp-wrap">
    <h1>Open TA positions</h1>
    <p class="jsp-note">Approved positions still accepting applications (not closed or past deadline). Loaded from <code>data/positions.txt</code> via <code>PositionService</code>.</p>
    <table class="jsp-table">
        <thead>
        <tr>
            <th>ID</th>
            <th>Course</th>
            <th>Code</th>
            <th>Slots</th>
            <th>Pay</th>
            <th>Deadline</th>
        </tr>
        </thead>
        <tbody>
        <% if (positions.isEmpty()) { %>
        <tr><td colspan="6">No approved positions.</td></tr>
        <% } else { for (Position p : positions) { %>
        <tr>
            <td><%= p.getId() %></td>
            <td><%= p.getCourseName() %></td>
            <td><%= p.getCourseCode() %></td>
            <td><%= p.getNumPositions() %></td>
            <td><%= p.getPayRate() %></td>
            <td><%= p.getDeadline() %></td>
        </tr>
        <% } } %>
        </tbody>
    </table>
    <a class="jsp-back" href="/index.html">&larr; Back to Sign In</a>
    <a class="jsp-back" href="/ta/dashboard.html" style="margin-left:1rem">&larr; TA Dashboard</a>
</div>
</body>
</html>
