package com.eduguard.controller;

import com.eduguard.dao.GenericDAO;
import com.eduguard.model.Student;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;

@WebServlet("/api/students")
public class StudentServlet extends HttpServlet {

    private final GenericDAO<Student> dao = new GenericDAO<>(Student.class);
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(gson.toJson(dao.findAll()));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            Student s = gson.fromJson(req.getReader(), Student.class);
            resp.getWriter().write(gson.toJson(dao.save(s)));
        } catch (RuntimeException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Could not save student (duplicate roll number?)\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        try {
            dao.delete(Integer.parseInt(req.getParameter("id")));
            resp.getWriter().write("{\"success\":true}");
        } catch (RuntimeException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Cannot delete: student has attendance, marks or risk records\"}");
        }
    }
}