package com.bupt.ta.web;

import java.util.ArrayList;
import java.util.List;

public class UserService {
    private static final String FILE = "data/users.txt";

    public User login(String userId, String password) {
        List<User> users = getAllUsers();
        for (User u : users) {
            if (u.getId().equals(userId) && u.getPassword().equals(password)) {
                return u;
            }
        }
        return null;
    }

    public boolean register(User user) {
        List<User> users = getAllUsers();
        for (User u : users) {
            if (u.getId().equals(user.getId()) || u.getEmail().equals(user.getEmail())) {
                return false;
            }
        }
        List<String> lines = FileUtil.read(FILE);
        lines.add(user.toLine());
        FileUtil.write(FILE, lines);
        return true;
    }

    public List<User> getAllUsers() {
        List<String> lines = FileUtil.read(FILE);
        List<User> list = new ArrayList<>();
        for (String line : lines) {
            if (line.trim().isEmpty()) continue;
            User u = User.fromLine(line);
            if (u != null) list.add(u);
        }
        return list;
    }

    public List<User> getAllTAs() {
        List<User> all = getAllUsers();
        List<User> tas = new ArrayList<>();
        for (User u : all) {
            if ("TA".equals(u.getRole())) tas.add(u);
        }
        return tas;
    }

    public User getUserById(String id) {
        for (User u : getAllUsers()) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }

    public void updateUser(User updated) {
        List<User> users = getAllUsers();
        List<String> lines = new ArrayList<>();
        for (User u : users) {
            if (u.getId().equals(updated.getId())) {
                lines.add(updated.toLine());
            } else {
                lines.add(u.toLine());
            }
        }
        FileUtil.write(FILE, lines);
    }

    public boolean changePassword(String userId, String oldPwd, String newPwd) {
        User user = getUserById(userId);
        if (user == null || !user.getPassword().equals(oldPwd)) return false;
        user.setPassword(newPwd);
        updateUser(user);
        return true;
    }

    public int generateTAId() {
        List<String> ids = new ArrayList<>();
        for (User u : getAllTAs()) ids.add(u.getId());
        return IdSequences.next("TA", ids);
    }

    public boolean isEmailUsedByOther(String userId, String email) {
        if (email == null || email.trim().isEmpty()) return false;
        for (User u : getAllUsers()) {
            if (!u.getId().equals(userId) && email.equalsIgnoreCase(u.getEmail())) {
                return true;
            }
        }
        return false;
    }
}
