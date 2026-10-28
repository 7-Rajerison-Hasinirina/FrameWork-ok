package controllers;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Map;

import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import annotations.ApiRest;
import annotations.AuthRequired;
import annotations.RequiresRole;
import com.google.gson.Gson;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import models.Mapping;
import models.ModelView;
import models.UrlMethode;
import utilitaires.ParamBinder;

public class FrontServletController extends HttpServlet {

    @SuppressWarnings("unchecked")
    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        Map<UrlMethode, Mapping> routes = (Map<UrlMethode, Mapping>) getServletContext().getAttribute("routes");

        if (routes == null) {
            writeInternalError(res, "Erreur Interne: La table de routage n'a pas été initialisée.");
            return;
        }

        String httpMethod = req.getMethod().toUpperCase();
        String requestUri = req.getRequestURI();
        String contextPath = req.getContextPath();
        String routeUrl = requestUri.substring(contextPath.length());
        Mapping mapping = routes.get(new UrlMethode(routeUrl, httpMethod));

        if (mapping == null) {
            writeNotFound(res, routeUrl, httpMethod);
            return;
        }

        try {
            executeMappedAction(req, res, mapping);
        } catch (Exception e) {
            throw new ServletException("Erreur lors de l'exécution de l'action : " + mapping.getNomClasse() + "."
                    + mapping.getNomMethode(), e);
        }
    }

    private void executeMappedAction(HttpServletRequest req, HttpServletResponse res, Mapping mapping) throws Exception {
        res.setContentType("text/html;charset=UTF-8");
        WebApplicationContext springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
        String controllerPackage = getServletContext().getInitParameter("Controllers");

        Class<?> controllerClass = Class.forName(controllerPackage + "." + mapping.getNomClasse());
        Object controller = springContext.getBean(controllerClass);

        Method matchedMethod = findMatchingMethod(controllerClass, mapping.getNomMethode());
        if (matchedMethod == null) {
            throw new ServletException("Aucune méthode compatible trouvée pour : " + mapping.getNomMethode());
        }

        AuthRequired authRequired = getAuthRequirement(controllerClass, matchedMethod);
        if (authRequired != null && !isAuthenticated(req, authRequired.value())) {
            handleAuthenticationFailure(req, res, authRequired);
            return;
        }

        RequiresRole requiresRole = getRequiresRole(controllerClass, matchedMethod);
        if (requiresRole != null && !hasRequiredRole(req, requiresRole.value())) {
            handleRoleFailure(req, res, requiresRole);
            return;
        }

        try {
            Object[] args = ParamBinder.bindParams(req, matchedMethod);
            Object result = matchedMethod.invoke(controller, args);
            renderResult(req, res, matchedMethod, result);
        } catch (IllegalArgumentException iae) {
            res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            res.setContentType("text/plain;charset=UTF-8");
            res.getWriter().println("Paramètre requis manquant ou invalide: " + iae.getMessage());
        }
    }

    private Method findMatchingMethod(Class<?> controllerClass, String methodName) {
        Method[] methods = controllerClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private void renderResult(HttpServletRequest req, HttpServletResponse res, Method method, Object result)
            throws IOException, ServletException {
        if (method.isAnnotationPresent(ApiRest.class)) {
            renderJsonResponse(res, result);
            return;
        }

        if (result instanceof ModelView) {
            renderModelView(req, res, (ModelView) result);
            return;
        }

        if (result != null) {
            res.setContentType("text/html;charset=UTF-8");
            res.getWriter().println(result.toString());
        }
    }

    private void renderModelView(HttpServletRequest req, HttpServletResponse res, ModelView modelView)
            throws ServletException, IOException {
        for (Map.Entry<String, Object> entry : modelView.getData().entrySet()) {
            req.setAttribute(entry.getKey(), entry.getValue());
        }

        String prefix = getServletContext().getInitParameter("prefix");
        String suffix = getServletContext().getInitParameter("suffix");
        if (prefix == null) {
            prefix = "";
        }
        if (suffix == null) {
            suffix = "";
        }

        String viewPath = prefix + modelView.getUrl() + suffix;
        RequestDispatcher dispatcher = req.getRequestDispatcher(viewPath);
        dispatcher.forward(req, res);
    }

    private void renderJsonResponse(HttpServletResponse res, Object result) throws IOException {
        res.setContentType("application/json;charset=UTF-8");
        PrintWriter out = res.getWriter();

        if (result instanceof String) {
            out.print((String) result);
            return;
        }

        if (result != null) {
            Gson gson = new Gson();
            out.print(gson.toJson(result));
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
            AuthRequired authRequired) throws IOException {
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

    private void writeInternalError(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        res.setContentType("text/plain;charset=UTF-8");
        res.getWriter().println(message);
    }

    private void writeNotFound(HttpServletResponse res, String routeUrl, String httpMethod) throws IOException {
        res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        res.setContentType("text/html;charset=UTF-8");
        res.getWriter().println(
                "<h3>URL indéfinie ou méthode HTTP non supportée : " + routeUrl + " [" + httpMethod + "]</h3>");
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