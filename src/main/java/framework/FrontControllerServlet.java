package framework;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import annotation.Controller;
import annotation.UrlMapping;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.Utilitaires;

public class FrontControllerServlet extends HttpServlet {

    protected List<String> listeController;
    protected HashMap<UrlMethod, Mapping> urlMappings;
    protected String controllerPackage;

    @Override
    public void init() throws ServletException {
        try {

            if (!this.getInitParameter("controllerPackage").isEmpty()) {

                controllerPackage = this.getInitParameter("controllerPackage");

                listeController = Utilitaires.getClassByPackageAndAnnotation(
                        Controller.class,
                        controllerPackage,
                        ElementType.TYPE
                );

                urlMappings = new HashMap<>();

                for (String nomControleur : listeController) {

                    Class<?> classeControleur = Class.forName(
                            controllerPackage + "." + nomControleur
                    );

                    List<Method> methodes = Utilitaires.getMethodesAnnotees(
                            classeControleur,
                            UrlMapping.class
                    );

                    for (Method methode : methodes) {

                        UrlMapping annotation = methode.getAnnotation(
                                UrlMapping.class
                        );

                        String url = annotation.value();
                        String methodeHttp = annotation.method();

                        UrlMethod cle = new UrlMethod(
                                url,
                                methodeHttp
                        );

                        if (urlMappings.containsKey(cle)) {
                            throw new Exception(
                                    "URL déjà utilisée : "
                                            + methodeHttp
                                            + " "
                                            + url
                            );
                        }

                        Mapping mapping = new Mapping(
                                nomControleur,
                                methode.getName()
                        );

                        urlMappings.put(
                                cle,
                                mapping
                        );
                    }
                }
            }

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String url = request.getRequestURI();
        String contexte = request.getContextPath();
        String urlRecherchee = url.substring(contexte.length());

        String methodeHttp = request.getMethod();

        UrlMethod urlMethod = new UrlMethod(
                urlRecherchee,
                methodeHttp
        );

        Mapping mapping = urlMappings.get(
                urlMethod
        );

        PrintWriter out = response.getWriter();

        if (mapping == null) {

            out.println("URL inconnue : " + urlRecherchee);
            out.println("<br><br>");
            out.println("URLs disponibles :");
            out.println("<br>");

            for (UrlMethod cle : urlMappings.keySet()) {
                out.println(
                        cle.getMethodeHttp()
                                + " "
                                + cle.getUrl()
                );
                out.println("<br>");
            }

        } else {

            try {

                Class<?> classeControleur = Class.forName(
                        controllerPackage
                                + "."
                                + mapping.getNomClasse()
                );

                Object instanceControleur =
                        classeControleur
                                .getDeclaredConstructor()
                                .newInstance();

                Method methode =
                        classeControleur.getDeclaredMethod(
                                mapping.getNomMethode()
                        );

                if (methode.getParameterCount() != 0) {
                    throw new Exception(
                            "La méthode "
                                    + methode.getName()
                                    + " possède des paramètres. "
                                    + "Le Sprint 3-bis ne supporte pas encore cela."
                    );
                }

                Object resultat =
                        methode.invoke(
                                instanceControleur
                        );

                out.println("Invocation réussie");
                out.println("<br>");
                out.println("URL : " + urlRecherchee);
                out.println("<br>");
                out.println("HTTP : " + methodeHttp);
                out.println("<br>");
                out.println("Controller : " + mapping.getNomClasse());
                out.println("<br>");
                out.println("Méthode exécutée : " + mapping.getNomMethode());

                if (resultat != null) {
                    out.println("<br>");
                    out.println("Valeur retournée : " + resultat);
                }

            } catch (Exception e) {

                out.println(
                        "Erreur lors de l'invocation : "
                                + e.getMessage()
                );

                out.println("<br><br>");

                e.printStackTrace(out);
            }
        }
    }
}