package utilitaires;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import annotations.RequestParam;
import jakarta.servlet.http.HttpServletRequest;

public class ParamBinder {

    public static Object[] bindParams(HttpServletRequest req, Method method) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            RequestParam ann = p.getAnnotation(RequestParam.class);
            String paramName = null;
            if (ann != null) {
                paramName = ann.value();
            } else {
                paramName = p.getName();
            }
            String raw = req.getParameter(paramName);

            if (raw == null) {
                if (ann != null && ann.required()) {
                    throw new IllegalArgumentException("Required parameter '" + paramName + "' is missing");
                }
                args[i] = null;
            } else {
                args[i] = ParamConverter.convert(raw, p.getType());
            }
        }
        return args;
    }
}
