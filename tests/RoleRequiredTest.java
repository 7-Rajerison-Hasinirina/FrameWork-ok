import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import annotations.AuthRequired;
import annotations.RequiresRole;
import controllers.FrontServletController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import models.MySession;

public class RoleRequiredTest {
    public static void main(String[] args) throws Exception {
        testAdminAllowed();
        testUserDeniedForAdminAction();
        testManagerAllowed();
        testNoSessionDenied();
        System.out.println("RoleRequiredTest OK");
    }

    private static void testAdminAllowed() throws Exception {
        HttpServletRequest request = buildRequest(true, "alice", "admin");
        FrontServletController servlet = new FrontServletController();
        Method method = AdminController.class.getDeclaredMethod("adhesion");
        if (!method.isAnnotationPresent(RequiresRole.class)) {
            throw new AssertionError("La méthode adhesion doit être protégée par @RequiresRole");
        }

        Method authCheck = FrontServletController.class.getDeclaredMethod("hasRequiredRole", HttpServletRequest.class, String[].class);
        authCheck.setAccessible(true);
        Object result = authCheck.invoke(servlet, request, new String[] { "admin", "manager" });
        if (!(Boolean) result) {
            throw new AssertionError("Un admin doit avoir accès à adhesion");
        }
    }

    private static void testUserDeniedForAdminAction() throws Exception {
        HttpServletRequest request = buildRequest(true, "bob", "user");
        FrontServletController servlet = new FrontServletController();
        Method authCheck = FrontServletController.class.getDeclaredMethod("hasRequiredRole", HttpServletRequest.class, String[].class);
        authCheck.setAccessible(true);
        Object result = authCheck.invoke(servlet, request, new String[] { "admin", "manager" });
        if ((Boolean) result) {
            throw new AssertionError("Un user ne doit pas avoir accès à adhesion");
        }
    }

    private static void testManagerAllowed() throws Exception {
        HttpServletRequest request = buildRequest(true, "charlie", "manager");
        FrontServletController servlet = new FrontServletController();
        Method authCheck = FrontServletController.class.getDeclaredMethod("hasRequiredRole", HttpServletRequest.class, String[].class);
        authCheck.setAccessible(true);
        Object result = authCheck.invoke(servlet, request, new String[] { "manager" });
        if (!(Boolean) result) {
            throw new AssertionError("Un manager doit avoir accès à modification");
        }
    }

    private static void testNoSessionDenied() throws Exception {
        HttpServletRequest request = buildRequest(false, null, null);
        FrontServletController servlet = new FrontServletController();
        Method authCheck = FrontServletController.class.getDeclaredMethod("hasRequiredRole", HttpServletRequest.class, String[].class);
        authCheck.setAccessible(true);
        Object result = authCheck.invoke(servlet, request, new String[] { "admin" });
        if ((Boolean) result) {
            throw new AssertionError("Sans session, aucun rôle ne doit être autorisé");
        }
    }

    private static HttpServletRequest buildRequest(boolean hasSession, String user, String role) {
        Map<String, Object> attributes = new HashMap<>();
        if (user != null) {
            attributes.put("user", user);
        }
        if (role != null) {
            attributes.put("role", role);
        }

        final boolean[] sessionState = { hasSession };
        HttpSession session = (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[] { HttpSession.class },
                (proxy, methodRef, methodArgs) -> {
                    String methodName = methodRef.getName();
                    switch (methodName) {
                        case "getAttribute":
                            return attributes.get(methodArgs[0]);
                        case "setAttribute":
                            attributes.put((String) methodArgs[0], methodArgs[1]);
                            return null;
                        case "getAttributeNames":
                            return Collections.enumeration(attributes.keySet());
                        case "removeAttribute":
                            attributes.remove(methodArgs[0]);
                            return null;
                        case "invalidate":
                            attributes.clear();
                            return null;
                        default:
                            return null;
                    }
                });

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[] { HttpServletRequest.class },
                (proxy, methodRef, methodArgs) -> {
                    String methodName = methodRef.getName();
                    switch (methodName) {
                        case "getSession":
                            if (methodArgs == null || methodArgs.length == 0) {
                                return sessionState[0] ? session : null;
                            }
                            boolean create = (boolean) methodArgs[0];
                            if (create && !sessionState[0]) {
                                sessionState[0] = true;
                            }
                            return sessionState[0] ? session : null;
                        case "getContextPath":
                            return "";
                        default:
                            return null;
                    }
                });
    }

    public static class AdminController {
        @AuthRequired(value = "user", loginPage = "/login.jsp")
        @RequiresRole({ "admin", "manager" })
        public void adhesion() {
        }
    }
}
