/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Status;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "DeactivateUser", urlPatterns = {"/DeactivateUser"})
public class DeactivateUser extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(req.getReader(), JsonObject.class);

            JsonObject responseObj = new JsonObject();
            responseObj.addProperty("scc", false);
            responseObj.addProperty("msg", "Unknown error");
            responseObj.addProperty("id", 0);

            String mobile = jsonObject.get("mobile").getAsString();
            String stateId = jsonObject.get("id").getAsString();

            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Restrictions.eq("mobile", mobile));
            
             Status status = (Status) session.get(Status.class, Integer.parseInt(stateId));

            if (!criteria.list().isEmpty()) {
                User user = (User) criteria.uniqueResult();
                
                user.setStatus(status);
                session.update(user);
                session.beginTransaction().commit();
                
                responseObj.addProperty("scc", true);
                responseObj.addProperty("msg", "User Active State Changed");
                responseObj.addProperty("id", stateId);
                session.close();
            }

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(responseObj));
        } catch (Exception e) {
            System.out.println("DeactivateUser: DoPost: " + new Date());
        }
    }

}
