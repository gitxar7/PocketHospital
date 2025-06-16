/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.City;
import entity.Gender;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;

/**
 *
 * @author LOQ
 */
@WebServlet(name = "GetUsers", urlPatterns = {"/GetUsers"})
public class GetUsers extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Gson gson = new Gson();
            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria = session.createCriteria(User.class);
            List<Gender> users = criteria.list();

            JsonObject json = new JsonObject();
            json.add("UserList", gson.toJsonTree(users));

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(json));

            session.close();
        } catch (Exception e) {
            System.out.println("GetUsers: DoGet: " + new Date());
        }
    }

}
