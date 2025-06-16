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
import java.io.IOException;
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
@WebServlet(name = "GetData", urlPatterns = {"/GetData"})
public class GetData extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Gson gson = new Gson();

            Session session = HibernateUtil.getSessionFactory().openSession();

            Criteria criteria1 = session.createCriteria(Gender.class);
            List<Gender> genders = criteria1.list();

            Criteria criteria2 = session.createCriteria(City.class);
            List<City> citys = criteria2.list();
            
            Criteria criteria3 = session.createCriteria(Status.class);
            List<Status> statuses = criteria3.list();
            
             JsonObject json = new JsonObject();
            json.add("GenderList", gson.toJsonTree(genders));
            json.add("CityList", gson.toJsonTree(citys));
            json.add("StatusList", gson.toJsonTree(statuses));

            resp.setContentType("application/json");
            resp.getWriter().write(gson.toJson(json));

            session.close();
        } catch (Exception e) {
            System.out.println("GetData: DoGet: "+ new Date());
        }
    }

    

}
