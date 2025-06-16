/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.User;
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

/**
 *
 * @author LOQ
 */
@WebServlet(name = "SignIn", urlPatterns = {"/SignIn"})
public class SignIn extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(req.getReader(), JsonObject.class);

            JsonObject responseObj = new JsonObject();
            responseObj.addProperty("scc", false);
            responseObj.addProperty("msg", "Unknown error");
            responseObj.add("user", null);

            String mobile = jsonObject.get("mobile").getAsString();
            String password = jsonObject.get("password").getAsString();

            if (mobile.isEmpty()) {
                responseObj.addProperty("msg", "Please enter your Mobile");
            } else if (password.isEmpty()) {
                responseObj.addProperty("msg", "Please enter your Password");
            } else {
                Session session = HibernateUtil.getSessionFactory().openSession();

                Criteria criteria = session.createCriteria(User.class);
                criteria.add(Restrictions.eq("mobile", mobile));
                criteria.add(Restrictions.eq("password", password));

                if (!criteria.list().isEmpty()) {
                    User user = (User) criteria.uniqueResult();
                    System.out.println(user.getStatus().getId());
                    if (user.getStatus().getId() == 1) {
                        responseObj.addProperty("scc", true);
                        responseObj.addProperty("msg", "Sign In Successful");
                        user.setPassword(null);
                        responseObj.add("user", gson.toJsonTree(user));
                    } else{
                        responseObj.addProperty("msg", "User has been Deactivated");
                    }
                } else {
                    responseObj.addProperty("msg", "Invalid Details");
                }

                session.close();
            }

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(responseObj));
        } catch (Exception e) {
            System.out.println("SignIn: DoPost: " + new Date());
        }
    }

}
