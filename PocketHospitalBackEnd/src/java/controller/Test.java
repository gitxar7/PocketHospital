/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.Admin;
import entity.City;
import entity.Gender;
import entity.User;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

/**
 *
 * @author LOQ
 */
@WebServlet(name = "Test", urlPatterns = {"/Test"})
public class Test extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Gson gson = new Gson();

            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria1 = session.createCriteria(Gender.class);
            List<Gender> genders = criteria1.list();

            Criteria criteria2 = session.createCriteria(City.class);
            List<City> citys = criteria2.list();

            Criteria criteria3 = session.createCriteria(Admin.class);
            List<Admin> admins = criteria3.list();

            Criteria criteria4 = session.createCriteria(User.class);
            List<User> users = criteria4.list();

            JsonObject json = new JsonObject();
            json.add("GenderList", gson.toJsonTree(genders));
            json.add("CityList", gson.toJsonTree(citys));
            json.add("AdminList", gson.toJsonTree(admins));
            json.add("UserList", gson.toJsonTree(users));

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(json));

            session.close();

            System.out.println("Test");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
