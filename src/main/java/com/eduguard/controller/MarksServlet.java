package com.eduguard.controller;

import com.eduguard.dao.GenericDAO;
import com.eduguard.model.Marks;
import com.eduguard.util.HibernateUtil;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.hibernate.Session;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/marks")
public class MarksServlet extends HttpServlet {

    private final GenericDAO<Marks> dao = new GenericDAO<>(Marks.class);
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String sid = req.getParameter("studentId");

        if (sid == null) {
            resp.getWriter().write(gson.toJson(dao.findAll()));
        } else {
            try (Session s = HibernateUtil.getFactory().openSession()) {
                List<Marks> list = s.createQuery(
                                "from Marks m where m.student.id = :id", Marks.class)
                        .setParameter("id", Integer.parseInt(sid)).list();
                resp.getWriter().write(gson.toJson(list));
            }
        }
    }

    // JSON body: {"student":{"id":1},"subject":"Maths","examType":"Midterm","marksObtained":35,"maxMarks":100}
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            Marks m = gson.fromJson(req.getReader(), Marks.class);
            if (m.getMaxMarks() <= 0 || m.getMarksObtained() > m.getMaxMarks()) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"Invalid marks values\"}");
                return;
            }
            resp.getWriter().write(gson.toJson(dao.save(m)));
        } catch (RuntimeException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Could not save marks\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        dao.delete(Integer.parseInt(req.getParameter("id")));
        resp.setContentType("application/json");
        resp.getWriter().write("{\"success\":true}");
    }
}