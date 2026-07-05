package framework;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.HashMap;

public class FrontControllerServlet extends HttpServlet {

    private FrameworkConfiguration configuration;

    @Override
    public void init() throws ServletException {

        configuration = (FrameworkConfiguration)
                getServletContext().getAttribute("frameworkConfiguration");

        if (configuration == null) {
            throw new ServletException(
                    "Framework non initialisé (FrameworkConfiguration manquant)"
            );
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String url = request.getRequestURI();
        String context = request.getContextPath();
        String path = url.substring(context.length());

        String methodHttp = request.getMethod().toUpperCase();

        UrlMethod key = new UrlMethod(path, methodHttp);

        HashMap<UrlMethod, Mapping> mappings =
                configuration.getUrlMappings();

        Mapping mapping = mappings.get(key);

        if (mapping == null) {

            out.println("<h3>URL inconnue : " + path + "</h3>");

            out.println("<b>URLs disponibles :</b><br>");

            for (UrlMethod k : mappings.keySet()) {
                out.println(k.getMethodeHttp() + " " + k.getUrl() + "<br>");
            }

            return;
        }

        try {

            Class<?> controllerClass =
                    Class.forName(configuration.getControllerPackage()
                            + "." + mapping.getNomClasse());

            Object controller = controllerClass.getDeclaredConstructor().newInstance();

            Method method = controllerClass.getDeclaredMethod(mapping.getNomMethode());

            Object result = method.invoke(controller);

            out.println("<h3>OK INVOCATION</h3>");
            out.println("URL : " + path + "<br>");
            out.println("HTTP : " + methodHttp + "<br>");
            out.println("Controller : " + mapping.getNomClasse() + "<br>");
            out.println("Method : " + mapping.getNomMethode() + "<br>");

            if (result != null) {
                out.println("Result : " + result);
            }

        } catch (Exception e) {
            out.println("<h3>ERROR</h3>");
            out.println(e.getMessage());
        }
    }
}