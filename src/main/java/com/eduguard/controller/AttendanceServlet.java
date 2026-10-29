package com.eduguard.controller;

import com.eduguard.dao.GenericDAO;
import com.eduguard.model.Attendance;
import com.eduguard.util.HibernateUtil;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.hibernate.Session;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/attendance")
public class AttendanceServlet extends HttpServlet {

    private final GenericDAO<Attendance> dao = new GenericDAO<>(Attendance.class);
    private final Gson gson = new Gson();

    // GET api/attendance            -> all records
    // GET api/attendance?studentId=1 -> one student's records
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        String sid = req.getParameter("studentId");

        if (sid == null) {
            resp.getWriter().write(gson.toJson(dao.findAll()));
        } else {
            try (Session s = HibernateUtil.getFactory().openSession()) {
                List<Attendance> list = s.createQuery(
                                "from Attendance a where a.student.id = :id", Attendance.class)
                        .setParameter("id", Integer.parseInt(sid)).list();
                resp.getWriter().write(gson.toJson(list));
            }
        }
    }

    // JSON body: {"student":{"id":1},"subject":"Maths","totalClasses":40,"attendedClasses":30}
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        try {
            Attendance a = gson.fromJson(req.getReader(), Attendance.class);
            if (a.getAttendedClasses() > a.getTotalClasses()) {
                resp.setStatus(400);
                resp.getWriter().write("{\"error\":\"Attended classes cannot exceed total classes\"}");
                return;
            }
            resp.getWriter().write(gson.toJson(dao.save(a)));
        } catch (RuntimeException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"Could not save attendance\"}");
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        dao.delete(Integer.parseInt(req.getParameter("id")));
        resp.setContentType("application/json");
        resp.getWriter().write("{\"success\":true}");
    }
}