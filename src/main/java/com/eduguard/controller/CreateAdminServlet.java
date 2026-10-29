package com.eduguard.controller;

import com.eduguard.model.User;
import com.eduguard.util.HibernateUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;

@WebServlet("/create-admin")
public class CreateAdminServlet extends HttpServlet {

    // CHANGE THESE WHEN YOU WANT TO CREATE A NEW ADMIN
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws IOException {

        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");

        Transaction transaction = null;

        try (Session session = HibernateUtil.getFactory().openSession()) {

            User existingUser = session.createQuery(
                            "from User where username = :username",
                            User.class
                    )
                    .setParameter("username", ADMIN_USERNAME)
                    .uniqueResult();

            // If admin already exists, DON'T change anything
            if (existingUser != null) {

                resp.getWriter().println(
                        "Admin already exists."
                );

                resp.getWriter().println(
                        "No password or account details were changed."
                );

                return;
            }

            transaction = session.beginTransaction();

            User user = new User();

            user.setUsername(ADMIN_USERNAME);

            user.setPasswordHash(
                    BCrypt.hashpw(
                            ADMIN_PASSWORD,
                            BCrypt.gensalt()
                    )
            );

            user.setRole("ADMIN");

            session.persist(user);

            transaction.commit();

            resp.getWriter().println(
                    "Admin created successfully!"
            );

            resp.getWriter().println(
                    "Username: " + ADMIN_USERNAME
            );

            resp.getWriter().println(
                    "Password: " + ADMIN_PASSWORD
            );

            resp.getWriter().println(
                    "Role: ADMIN"
            );

        } catch (Exception e) {

            if (transaction != null) {
                transaction.rollback();
            }

            resp.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            resp.getWriter().println(
                    "Error creating admin:"
            );

            e.printStackTrace(resp.getWriter());
        }
    }
}