package listener;

import java.lang.annotation.ElementType;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import annotations.Controllers;
import annotations.UrlMapping;
import exceptions.DuplicateRouteException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import models.Mapping;
import models.UrlMethode;
import utilitaires.Utilitaires;

@WebListener
public class FrameworkContextListener implements ServletContextListener {

    private static final String CONTROLLERS_PARAMETER = "Controllers";
    private static final String ROUTES_ATTRIBUTE = "routes";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String controllerPackage = context.getInitParameter(CONTROLLERS_PARAMETER);

        if (controllerPackage == null || controllerPackage.trim().isEmpty()) {
            context.log("Framework Error: Le paramètre d'initialisation global 'Controllers' est manquant.");
            throw new RuntimeException("Paramètre d'initialisation global 'Controllers' manquant.");
        }

        try {
            Map<UrlMethode, Mapping> routes = buildRoutes(controllerPackage);
            context.setAttribute(ROUTES_ATTRIBUTE, routes);
            context.log("Framework initialized successfully with " + routes.size() + " routes.");
        } catch (DuplicateRouteException e) {
            context.log("Framework Initialization Failed: Route dupliquée détectée -> " + e.getMessage());
            throw new RuntimeException("Erreur de mapping au démarrage : " + e.getMessage(), e);
        } catch (Exception e) {
            context.log("Framework Initialization Failed: " + e.getMessage());
            throw new RuntimeException("Erreur lors du chargement des contrôleurs : " + e.getMessage(), e);
        }
    }

    private Map<UrlMethode, Mapping> buildRoutes(String controllerPackage) throws Exception {
        Map<UrlMethode, Mapping> routes = new HashMap<>();
        List<String> controllerNames = Utilitaires.getClassByPackageAndAnnotation(
                Controllers.class,
                controllerPackage,
                ElementType.TYPE);

        if (controllerNames == null) {
            return routes;
        }

        for (String controllerName : controllerNames) {
            Class<?> controllerClass = Class.forName(controllerPackage + "." + controllerName);
            List<Method> annotatedMethods = Utilitaires.getMethodesAnnotees(controllerClass, UrlMapping.class);

            for (Method method : annotatedMethods) {
                UrlMapping annotation = method.getAnnotation(UrlMapping.class);
                String url = annotation.value();
                String httpMethod = annotation.METHOD().toUpperCase();
                UrlMethode routeKey = new UrlMethode(url, httpMethod);
                Mapping currentMapping = new Mapping(controllerName, method.getName());

                if (routes.containsKey(routeKey)) {
                    throw new DuplicateRouteException(url, httpMethod, routes.get(routeKey), currentMapping);
                }

                routes.put(routeKey, currentMapping);
            }
        }

        return routes;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute(ROUTES_ATTRIBUTE);
    }
}