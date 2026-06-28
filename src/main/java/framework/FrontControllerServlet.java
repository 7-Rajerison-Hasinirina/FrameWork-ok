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

import java.util.HashMap;
import framework.Mapping;
import java.lang.reflect.Method;
import annotation.UrlMapping;

public class FrontControllerServlet extends HttpServlet {

    protected List<String> listeController;
    protected HashMap<String, Mapping> urlMappings;

    @Override
    public void init() throws ServletException {
        try {
            if (!this.getInitParameter("controllerPackage").isEmpty()) {
                String controllerPackage = this.getInitParameter("controllerPackage");
                listeController = Utilitaires.getClassByPackageAndAnnotation(
                        Controller.class,
                        controllerPackage,
                        ElementType.TYPE);

                urlMappings = new HashMap<>();

                for (String nomControleur : listeController) {
                    Class<?> classeControleur = Class.forName(controllerPackage + "." + nomControleur);
                    List<Method> methodes = Utilitaires.getMethodesAnnotees(classeControleur, UrlMapping.class);

                    for (Method methode : methodes) {
                        UrlMapping annotation = methode.getAnnotation(UrlMapping.class);
                        String url = annotation.value();
                        Mapping mapping = new Mapping(nomControleur, methode.getName());
                        urlMappings.put(url, mapping);
                    }
                }
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getRequestURI();
        String contexte = request.getContextPath();
        String urlRecherchee = url.substring(contexte.length());
        PrintWriter out = response.getWriter();
        Mapping mapping = urlMappings.get(urlRecherchee);

        if (mapping == null) {
            out.println("URL inconnue : " + urlRecherchee);
        } else {
            out.println(  "URL : "  + urlRecherchee);
            out.println("<br>");

            out.println("Controller : " + mapping.getNomClasse());
            out.println("<br>");

            out.println("Methode : " + mapping.getNomMethode());
        }
    }

}