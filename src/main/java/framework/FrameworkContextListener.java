package framework;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;

import annotation.Controller;
import annotation.UrlMapping;
import util.Utilitaires;

public class FrameworkContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        try {
            ServletContext ctx = sce.getServletContext();

            String controllerPackage = ctx.getInitParameter("controllerPackage");

            FrameworkConfiguration config = new FrameworkConfiguration();
            config.setControllerPackage(controllerPackage);

            HashMap<UrlMethod, Mapping> mappings = new HashMap<>();

            List<String> controllers =
                    Utilitaires.getClassByPackageAndAnnotation(
                            Controller.class,
                            controllerPackage,
                            java.lang.annotation.ElementType.TYPE
                    );

            for (String c : controllers) {

                Class<?> clazz = Class.forName(controllerPackage + "." + c);

                List<Method> methods =
                        Utilitaires.getMethodesAnnotees(clazz, UrlMapping.class);

                for (Method m : methods) {

                    UrlMapping um = m.getAnnotation(UrlMapping.class);

                    UrlMethod key = new UrlMethod(
                            um.value(),
                            um.method()
                    );

                    Mapping map = new Mapping(c, m.getName());

                    mappings.put(key, map);
                }
            }

            config.setUrlMappings(mappings);

            ctx.setAttribute("frameworkConfiguration", config);

            System.out.println("Framework initialisé OK");

        } catch (Exception e) {
            throw new RuntimeException("Framework init failed", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}