package com.eduguard.controller;

import com.eduguard.ai.RiskPredictor;
import com.eduguard.model.RiskResult;
import com.eduguard.model.Student;
import com.eduguard.util.HibernateUtil;
import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;

@WebServlet("/api/risk")
public class RiskServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final RiskPredictor predictor = new RiskPredictor();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        int id;
        try {
            id = Integer.parseInt(req.getParameter("studentId"));
        } catch (NumberFormatException e) {
            resp.setStatus(400);
            resp.getWriter().write("{\"error\":\"studentId required\"}");
            return;
        }

        Transaction tx = null;
        try (Session s = HibernateUtil.getFactory().openSession()) {
            Student student = s.get(Student.class, id);
            if (student == null) {
                resp.setStatus(404);
                resp.getWriter().write("{\"error\":\"Student not found\"}");
                return;
            }

            Double att = s.createQuery(
                            "select sum(a.attendedClasses) * 100.0 / sum(a.totalClasses) " +
                                    "from Attendance a where a.student.id = :id", Double.class)
                    .setParameter("id", id).uniqueResult();

            Double avg = s.createQuery(
                            "select avg(m.marksObtained * 100.0 / m.maxMarks) " +
                                    "from Marks m where m.student.id = :id", Double.class)
                    .setParameter("id", id).uniqueResult();

            Long failed = s.createQuery(
                            "select count(m) from Marks m where m.student.id = :id " +
                                    "and m.marksObtained < 0.4 * m.maxMarks", Long.class)
                    .setParameter("id", id).uniqueResult();

            RiskResult r = predictor.evaluate(
                    student,
                    att == null ? 100 : att,
                    avg == null ? 100 : avg,
                    failed == null ? 0 : failed);

            tx = s.beginTransaction();
            s.persist(r);
            tx.commit();

            resp.getWriter().write(gson.toJson(r));
        } catch (RuntimeException e) {
            if (tx != null) tx.rollback();
            throw e;
        }
    }
}