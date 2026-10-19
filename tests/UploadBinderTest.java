import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import annotations.RequestParam;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import utilitaires.ParamBinder;

public class UploadBinderTest {
    public static void main(String[] args) throws Exception {
        Method method = UploadController.class.getDeclaredMethod("save", String.class, Part.class);

        HttpServletRequest request = (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[] { HttpServletRequest.class },
                (proxy, methodRef, methodArgs) -> {
                    String methodName = methodRef.getName();
                    switch (methodName) {
                        case "getContentType":
                            return "multipart/form-data; boundary=abc";
                        case "getParameter":
                            return null;
                        case "getPart":
                            if (methodArgs[0].equals("nom")) {
                                return textPart("nom", "Math");
                            }
                            if (methodArgs[0].equals("fichier")) {
                                return filePart("fichier", "document.pdf", "application/pdf", new byte[] { 1, 2, 3 });
                            }
                            return null;
                        case "getParameterMap":
                            return Collections.emptyMap();
                        default:
                            return null;
                    }
                });

        Object[] values = ParamBinder.bindParams(request, method);
        if (values.length != 2) {
            throw new AssertionError("Le binder doit produire 2 arguments");
        }
        if (!(values[0] instanceof String) || !"Math".equals(values[0])) {
            throw new AssertionError("Le champ texte multipart n’a pas été récupéré correctement");
        }
        if (values[1] == null) {
            throw new AssertionError("Le fichier uploadé doit être détecté par le framework");
        }
        System.out.println("UploadBinderTest OK");
    }

    private static Part textPart(String name, String value) {
        return (Part) Proxy.newProxyInstance(
                Part.class.getClassLoader(),
                new Class<?>[] { Part.class },
                (proxy, methodRef, methodArgs) -> {
                    String methodName = methodRef.getName();
                    switch (methodName) {
                        case "getName":
                            return name;
                        case "getSubmittedFileName":
                            return null;
                        case "getSize":
                            return (long) value.getBytes(StandardCharsets.UTF_8).length;
                        case "getInputStream":
                            return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
                        case "getHeader":
                            return null;
                        case "getHeaders":
                            return Collections.emptyList();
                        case "getHeaderNames":
                            return Collections.emptyList();
                        default:
                            return null;
                    }
                });
    }

    private static Part filePart(String name, String fileName, String contentType, byte[] content) {
        return (Part) Proxy.newProxyInstance(
                Part.class.getClassLoader(),
                new Class<?>[] { Part.class },
                (proxy, methodRef, methodArgs) -> {
                    String methodName = methodRef.getName();
                    switch (methodName) {
                        case "getName":
                            return name;
                        case "getSubmittedFileName":
                            return fileName;
                        case "getContentType":
                            return contentType;
                        case "getSize":
                            return (long) content.length;
                        case "getInputStream":
                            return new ByteArrayInputStream(content);
                        case "getHeader":
                            return null;
                        case "getHeaders":
                            return Collections.emptyList();
                        case "getHeaderNames":
                            return Collections.emptyList();
                        default:
                            return null;
                    }
                });
    }

    public static class UploadController {
        public void save(@RequestParam("nom") String nom, @RequestParam("fichier") Part fichier) {
        }
    }
}
