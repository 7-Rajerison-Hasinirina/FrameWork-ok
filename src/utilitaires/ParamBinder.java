package utilitaires;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import annotations.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import models.MySession;

public class ParamBinder {

    public static Object[] bindParams(HttpServletRequest req, Method method) throws Exception {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            Class<?> type = p.getType();

            if (type.equals(HttpSession.class)) {
                args[i] = req.getSession();
                continue;
            }

            if (type.equals(MySession.class)) {
                args[i] = new MySession(req.getSession());
                continue;
            }

            if (type.equals(Part.class) || type.equals(File.class)) {
                RequestParam ann = p.getAnnotation(RequestParam.class);
                String paramName = ann != null ? ann.value() : p.getName();
                args[i] = bindUploadParameter(req, paramName, type, ann != null ? ann.required() : true);
                continue;
            }

            if (isSimpleType(type)) {
                RequestParam ann = p.getAnnotation(RequestParam.class);
                String paramName = ann != null ? ann.value() : p.getName();
                String raw = resolveRequestValue(req, paramName);

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

    private static String resolveRequestValue(HttpServletRequest req, String paramName) throws Exception {
        String value = req.getParameter(paramName);
        if (value != null) {
            return value;
        }

        if (!isMultipartRequest(req)) {
            return null;
        }

        Part part = req.getPart(paramName);
        if (part == null || part.getSize() == 0) {
            return null;
        }

        if (part.getSubmittedFileName() != null && !part.getSubmittedFileName().trim().isEmpty()) {
            return null;
        }

        return readPartAsText(part);
    }

    private static String readPartAsText(Part part) throws IOException {
        try (InputStream in = part.getInputStream(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }

    private static Object bindUploadParameter(HttpServletRequest req, String paramName, Class<?> targetType,
            boolean required) throws Exception {
        if (!isMultipartRequest(req)) {
            if (required) {
                throw new IllegalArgumentException(
                        "Le paramètre '" + paramName + "' doit être envoyé en multipart/form-data");
            }
            return null;
        }

        Part part = req.getPart(paramName);
        if (part == null || part.getSize() == 0) {
            if (required) {
                throw new IllegalArgumentException("Le fichier '" + paramName + "' est obligatoire");
            }
            return null;
        }

        if (targetType.equals(Part.class)) {
            return part;
        }

        if (targetType.equals(File.class)) {
            return UploadUtil.saveUploadedFile(part);
        }

        return part;
    }

    private static boolean isMultipartRequest(HttpServletRequest req) {
        String contentType = req.getContentType();
        return contentType != null && contentType.toLowerCase(Locale.ROOT).contains("multipart/form-data");
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

            if (field.getType().equals(Part.class) || field.getType().equals(File.class)) {
                Object value = bindUploadParameter(req, fieldName, field.getType(), false);
                if (value == null) {
                    continue;
                }

                Method setter = findSetter(type, fieldName);
                if (setter != null) {
                    setter.invoke(instance, value);
                } else {
                    field.setAccessible(true);
                    field.set(instance, value);
                }
                continue;
            }

            String raw = resolveRequestValue(req, fieldName);
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
