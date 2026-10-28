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
            args[i] = bindParameter(req, parameters[i]);
        }

        return args;
    }

    private static Object bindParameter(HttpServletRequest req, Parameter parameter) throws Exception {
        Class<?> parameterType = parameter.getType();

        if (parameterType.equals(HttpSession.class)) {
            return req.getSession();
        }

        if (parameterType.equals(MySession.class)) {
            return MySession.fromRequest(req);
        }

        if (parameterType.equals(Part.class) || parameterType.equals(File.class)) {
            RequestParam annotation = parameter.getAnnotation(RequestParam.class);
            String paramName = annotation != null ? annotation.value() : parameter.getName();
            return bindUploadParameter(req, paramName, parameterType,
                    annotation != null ? annotation.required() : true);
        }

        if (isSimpleType(parameterType)) {
            RequestParam annotation = parameter.getAnnotation(RequestParam.class);
            String paramName = annotation != null ? annotation.value() : parameter.getName();
            String rawValue = resolveRequestValue(req, paramName);

            if (rawValue == null) {
                if (annotation != null && annotation.required()) {
                    throw new IllegalArgumentException("Required parameter '" + paramName + "' is missing");
                }
                return null;
            }

            return ParamConverter.convert(rawValue, parameterType);
        }

        return bindObject(req, parameterType);
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
        try (InputStream inputStream = part.getInputStream();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[4096];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            return outputStream.toString(StandardCharsets.UTF_8.name());
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
                injectValue(instance, type, fieldName, value);
                continue;
            }

            String rawValue = resolveRequestValue(req, fieldName);
            if (rawValue == null) {
                continue;
            }

            Object value = ParamConverter.convert(rawValue, field.getType());
            injectValue(instance, type, fieldName, value);
        }

        return instance;
    }

    private static void injectValue(Object instance, Class<?> type, String fieldName, Object value) throws Exception {
        Method setter = findSetter(type, fieldName);
        if (setter != null) {
            setter.invoke(instance, value);
            return;
        }

        Field field = getField(type, fieldName);
        if (field != null) {
            field.setAccessible(true);
            field.set(instance, value);
        }
    }

    private static Field getField(Class<?> type, String fieldName) {
        Class<?> currentType = type;
        while (currentType != null) {
            for (Field field : currentType.getDeclaredFields()) {
                if (field.getName().equals(fieldName)) {
                    return field;
                }
            }
            currentType = currentType.getSuperclass();
        }
        return null;
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
