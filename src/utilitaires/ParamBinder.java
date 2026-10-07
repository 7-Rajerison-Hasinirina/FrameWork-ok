package utilitaires;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import annotations.RequestParam;
import jakarta.servlet.http.HttpServletRequest;

public class ParamBinder {

    public static Object[] bindParams(HttpServletRequest req, Method method) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            Class<?> type = p.getType();

            if (isSimpleType(type)) {
                RequestParam ann = p.getAnnotation(RequestParam.class);
                String paramName = ann != null ? ann.value() : p.getName();
                String raw = req.getParameter(paramName);

                if (raw == null) {
                    if (ann != null && ann.required()) {
                        throw new IllegalArgumentException("Required parameter '" + paramName + "' is missing");
                    }
                    args[i] = null;
                } else {
                    args[i] = ParamConverter.convert(raw, type);
                }
            } else {
                args[i] = bindObject(req, type);
            }
        }
        return args;
    }

    private static boolean isSimpleType(Class<?> type) {
        return type.isPrimitive()
                || type.equals(String.class)
                || Number.class.isAssignableFrom(type)
                || type.equals(Boolean.class)
                || type.equals(Character.class)
                || type.equals(java.util.Date.class);
    }

    private static Object bindObject(HttpServletRequest req, Class<?> type) throws Exception {
        Object instance = type.getDeclaredConstructor().newInstance();

        for (Field field : getAllFields(type)) {
            String fieldName = field.getName();
            String raw = req.getParameter(fieldName);
            if (raw == null) {
                continue;
            }

            Object value = ParamConverter.convert(raw, field.getType());
            Method setter = findSetter(type, fieldName);
            if (setter != null) {
                setter.invoke(instance, value);
            } else {
                field.setAccessible(true);
                field.set(instance, value);
            }
        }

        return instance;
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = type;
        while (current != null) {
            for (Field field : current.getDeclaredFields()) {
                fields.add(field);
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private static Method findSetter(Class<?> type, String fieldName) {
        String capitalized = Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String setterName = "set" + capitalized;
        for (Method method : type.getDeclaredMethods()) {
            if (method.getName().equals(setterName) && method.getParameterCount() == 1) {
                return method;
            }
        }
        return null;
    }
}
