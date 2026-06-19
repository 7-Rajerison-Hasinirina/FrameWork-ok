package framework;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.util.ArrayList;
import java.util.List;

import annotation.Controller;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Utilitaires;

public class FrontControllerServlet extends HttpServlet {

    protected List<String> listeController;

    @Override
    public void init() throws ServletException {
        try {
            if (!this.getInitParameter("controllerPackage").isEmpty()) {
                String ControllerPackage = this.getInitParameter("controllerPackage");
                listeController = Utilitaires.getClassByPackageAndAnnotation(Controller.class, ControllerPackage,
                        ElementType.TYPE);
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }


    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request,  HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }


    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getRequestURI();
        PrintWriter out = response.getWriter();
        out.println( url);
        for (String controllerName : listeController) {
            out.println(controllerName);
        }
    }

}