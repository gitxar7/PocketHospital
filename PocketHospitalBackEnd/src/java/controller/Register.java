/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import entity.City;
import entity.Gender;
import entity.Status;
import entity.User;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import model.HibernateUtil;
import model.Validations;
import org.hibernate.Session;

/**
 *
 * @author LOQ
 */
@WebServlet(name = "Register", urlPatterns = {"/Register"})
public class Register extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(req.getReader(), JsonObject.class);

            Session session = HibernateUtil.getSessionFactory().openSession();

            JsonObject responseObj = new JsonObject();
            responseObj.addProperty("scc", false);
            responseObj.addProperty("msg", "Unknown error");

            String mobile = jsonObject.get("mobile").getAsString();
            String password = jsonObject.get("password").getAsString();
            String name = jsonObject.get("name").getAsString();
            String birthdayString = jsonObject.get("birthday").getAsString();
            int cityId = jsonObject.get("city").getAsInt();
            int genderId = jsonObject.get("gender").getAsInt();

            City city = (City) session.get(City.class, cityId);
            Gender gender = (Gender) session.get(Gender.class, genderId);
            Status status = (Status) session.get(Status.class, 1);

            if (name.isEmpty()) {
                responseObj.addProperty("msg", "Please enter your Name");
            } else if (birthdayString.isEmpty()) {
                responseObj.addProperty("msg", "Please enter your Birthday");
            } else if (city == null) {
                responseObj.addProperty("msg", "Invalid City");
            } else if (gender == null) {
                responseObj.addProperty("msg", "Invalid Gender");
            } else if (mobile.isEmpty()) {
                responseObj.addProperty("msg", "Please enter your Mobile");
            } else if (!Validations.isMobileNumberValid(mobile)) {
                responseObj.addProperty("msg", "Invalid Mobile");
            } else if (password.isEmpty()) {
                responseObj.addProperty("msg", "Please enter your Password");
            } else {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date birthday = sdf.parse(birthdayString);

                final User user = new User();
                user.setName(name);
                user.setBirthday(birthday);
                user.setCity(city);
                user.setGender(gender);
                user.setMobile(mobile);
                user.setPassword(password);
                user.setRegistered_date(new Date());
                user.setStatus(status);

                session.save(user);
                session.beginTransaction().commit();

                responseObj.addProperty("scc", true);
                responseObj.addProperty("msg", "Registration Successful");
            }

            session.close();
            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(responseObj)); 

        } catch (Exception e) {
            System.out.println("Register: DoPost: " + new Date());
        }
    }

}
