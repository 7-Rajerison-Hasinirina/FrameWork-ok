package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import models.Mapping;
import models.ModelView;
import models.UrlMethode;
import com.google.gson.Gson;
import annotations.ApiRest;
import annotations.AuthRequired;
import annotations.RequiresRole;
import utilitaires.ParamBinder;

public class FrontServletController extends HttpServlet {

    @SuppressWarnings("unchecked")
    protected void processRequest(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        Map<UrlMethode, Mapping> urlMappings = (Map<UrlMethode, Mapping>) getServletContext().getAttribute("routes");

        if (urlMappings == null) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            res.setContentType("text/plain;charset=UTF-8");
            res.getWriter().println("Erreur Interne: La table de routage n'a pas été initialisée.");
            return;
        }

        String methode = req.getMethod().toUpperCase();
        String url = req.getRequestURI();
        String contexte = req.getContextPath();
        String urlRecherchee = url.substring(contexte.length());

        Mapping mapping = urlMappings.get(new UrlMethode(urlRecherchee, methode));

        if (mapping == null) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.setContentType("text/html;charset=UTF-8");
            res.getWriter().println(
                    "<h3>URL indéfinie ou méthode HTTP non supportée : " + urlRecherchee + " [" + methode + "]</h3>");
            return;
        }

        try {
            res.setContentType("text/html;charset=UTF-8");
            WebApplicationContext springContext = WebApplicationContextUtils
                    .getRequiredWebApplicationContext(getServletContext());
            String controllerPackage = getServletContext().getInitParameter("Controllers");

            Class<?> controllerClass = Class.forName(controllerPackage + "." + mapping.getNomClasse());
            Object controller = springContext.getBean(controllerClass);

            Method method = null;
            Object retour = null;

            Method[] declared = controllerClass.getDeclaredMethods();
            Exception lastBindEx = null;
            for (Method m : declared) {
                if (!m.getName().equals(mapping.getNomMethode()))
                    continue;
                try {
                    AuthRequired authRequired = getAuthRequirement(controllerClass, m);
                    if (authRequired != null && !isAuthenticated(req, authRequired.value())) {
                        handleAuthenticationFailure(req, res, authRequired);
                        return;
                    }

                    RequiresRole requiresRole = getRequiresRole(controllerClass, m);
                    if (requiresRole != null && !hasRequiredRole(req, requiresRole.value())) {
                        handleRoleFailure(req, res, requiresRole);
                        return;
                    }

                    Object[] args = ParamBinder.bindParams(req, m);
                    method = m;
                    retour = method.invoke(controller, args);
                    break;
                } catch (IllegalArgumentException iae) {
                    lastBindEx = iae;
                    res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    res.setContentType("text/plain;charset=UTF-8");
                    res.getWriter().println("Paramètre requis manquant ou invalide: " + iae.getMessage());
                    return;
                }
            }

            if (method == null) {
                throw new ServletException("Aucune méthode compatible trouvée pour : " + mapping.getNomMethode(),
                        lastBindEx);
            }

            if (method.isAnnotationPresent(ApiRest.class)) {
                res.setContentType("application/json;charset=UTF-8");
                PrintWriter out = res.getWriter();

                if (retour instanceof String) {
                    out.print((String) retour);
                } else if (retour != null) {
                    Gson gson = new Gson();
                    out.print(gson.toJson(retour));
                }
            } else {

                if (retour instanceof ModelView) {
                    ModelView mv = (ModelView) retour;
                    String prefix = getServletContext().getInitParameter("prefix");
                    String suffix = getServletContext().getInitParameter("suffix");

                    if (prefix == null)
                        prefix = "";
                    if (suffix == null)
                        suffix = "";

                    for (Map.Entry<String, Object> entry : mv.getData().entrySet()) {
                        req.setAttribute(entry.getKey(), entry.getValue());
                    }

                    String viewPath = prefix + mv.getUrl() + suffix;
                    RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
                    dispatcher.forward(req, res);

                } else if (retour != null) {
                    res.setContentType("text/html;charset=UTF-8");
                    res.getWriter().println(retour.toString());
                }
            }
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'exécution de l'action : " + mapping.getNomClasse() + "."
                    + mapping.getNomMethode(), e);
        }
    }

    protected AuthRequired getAuthRequirement(Class<?> controllerClass, Method method) {
        if (method.isAnnotationPresent(AuthRequired.class)) {
            return method.getAnnotation(AuthRequired.class);
        }
        if (controllerClass.isAnnotationPresent(AuthRequired.class)) {
            return controllerClass.getAnnotation(AuthRequired.class);
        }
        return null;
    }

    protected RequiresRole getRequiresRole(Class<?> controllerClass, Method method) {
        if (method.isAnnotationPresent(RequiresRole.class)) {
            return method.getAnnotation(RequiresRole.class);
        }
        if (controllerClass.isAnnotationPresent(RequiresRole.class)) {
            return controllerClass.getAnnotation(RequiresRole.class);
        }
        return null;
    }

    protected boolean isAuthenticated(HttpServletRequest req, String attributeName) {
        if (req == null || attributeName == null || attributeName.trim().isEmpty()) {
            return false;
        }

        HttpSession session = req.getSession(false);
        return session != null && session.getAttribute(attributeName) != null;
    }

    protected boolean hasRequiredRole(HttpServletRequest req, String[] roles) {
        if (req == null || roles == null || roles.length == 0) {
            return false;
        }

        HttpSession session = req.getSession(false);
        if (session == null) {
            return false;
        }

        Object roleObject = session.getAttribute("role");
        if (!(roleObject instanceof String)) {
            return false;
        }

        String currentRole = ((String) roleObject).trim();
        for (String role : roles) {
            if (role != null && role.equalsIgnoreCase(currentRole)) {
                return true;
            }
        }
        return false;
    }

    protected void handleAuthenticationFailure(HttpServletRequest req, HttpServletResponse res,
            AuthRequired authRequired)
            throws IOException {
        if (res == null) {
            return;
        }

        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String loginPage = authRequired != null ? authRequired.loginPage() : null;
        String message = authRequired != null ? authRequired.message()
                : "Vous devez vous connecter pour accéder à cette ressource.";

        if (req != null && loginPage != null && !loginPage.trim().isEmpty()) {
            res.sendRedirect(req.getContextPath() + loginPage);
            return;
        }

        res.setContentType("text/html;charset=UTF-8");
        res.getWriter().println("<h3>" + message + "</h3>");
    }

    protected void handleRoleFailure(HttpServletRequest req, HttpServletResponse res, RequiresRole requiresRole)
            throws IOException {
        if (res == null) {
            return;
        }

        res.setStatus(HttpServletResponse.SC_FORBIDDEN);

        String deniedPage = requiresRole != null ? requiresRole.accessDeniedPage() : null;
        String message = requiresRole != null ? requiresRole.message() : "Accès refusé pour ce rôle.";

        if (req != null && deniedPage != null && !deniedPage.trim().isEmpty()) {
            res.sendRedirect(req.getContextPath() + deniedPage);
            return;
        }

        res.setContentType("text/html;charset=UTF-8");
        res.getWriter().println("<h3>" + message + "</h3>");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    protected void envoyer(HttpServletRequest req, HttpServletResponse res, String path)
            throws ServletException, IOException {
        RequestDispatcher requestDispatcher = req.getRequestDispatcher(path);
        requestDispatcher.forward(req, res);
    }
}