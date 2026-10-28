package utilitaires;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Utilitaires {

    public static List<String> getClassByPackageAndAnnotation(Class<? extends Annotation> annotation,
            String packageName, ElementType type) throws Exception {
        if (!typeElementValide(annotation, type)) {
            return null;
        }

        List<String> classNames = new ArrayList<>();
        List<Class<?>> classesInPackage = getClassByPackage(packageName);
        List<Class<?>> annotatedClasses = filterByAnnotation(classesInPackage, annotation);

        for (Class<?> currentClass : annotatedClasses) {
            classNames.add(currentClass.getSimpleName());
        }

        return classNames;
    }

    public static List<Class<?>> getClassByPackage(String packageName) throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace('.', '/');
        URL resource = classLoader.getResource(packagePath);

        if (resource == null) {
            throw new IllegalArgumentException("Aucun répertoire trouvé pour le package " + packageName);
        }

        File directory = new File(resource.getFile());
        List<Class<?>> classes = new ArrayList<>();

        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (!file.getName().endsWith(".class")) {
                continue;
            }

            String className = packageName + '.' + file.getName().substring(0, file.getName().length() - 6);
            classes.add(Class.forName(className));
        }

        return classes;
    }

    public static List<Class<?>> filterByAnnotation(List<Class<?>> classes, Class<? extends Annotation> annotation) {
        List<Class<?>> matchingClasses = new ArrayList<>();
        for (Class<?> currentClass : classes) {
            if (currentClass.isAnnotationPresent(annotation)) {
                matchingClasses.add(currentClass);
            }
        }
        return matchingClasses;
    }

    public static boolean typeElementValide(Class<? extends Annotation> annotation, ElementType type) {
        Target target = annotation.getAnnotation(Target.class);
        if (target == null) {
            return false;
        }

        for (ElementType targetType : target.value()) {
            if (type.equals(targetType)) {
                return true;
            }
        }

        return false;
    }

    public static List<Method> getMethodesAnnotees(Class<?> classe, Class<? extends Annotation> annotation) {
        List<Method> annotatedMethods = new ArrayList<>();
        for (Method method : classe.getDeclaredMethods()) {
            if (method.isAnnotationPresent(annotation)) {
                annotatedMethods.add(method);
            }
        }
        return annotatedMethods;
    }
}