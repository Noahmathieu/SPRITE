package GetServlet.utils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import GetServlet.annotation.Controller;
import GetServlet.annotation.UrlMapping;

public class Utilitaire {

        public List<Class<?>> getAllClasses() {
        
              Reflections reflections = new Reflections(new ConfigurationBuilder()
            .setUrls(ClasspathHelper.forClassLoader())
        );
        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

        List<Class<?>> listClasses = new ArrayList<>();
        for (Class<?> clazz : classes) {
            System.out.println("Classe : " + clazz.getSimpleName() + " - Package : " + clazz.getPackage().getName());
            listClasses.add(clazz);
        }
        return listClasses;
    }

    public List<Class<?>> getAllClassesByPackageName(String packageName) {

        Reflections reflections = new Reflections(packageName, new SubTypesScanner(false));

        Set<Class<?>> classes = reflections.getSubTypesOf(Object.class);

        List<Class<?>> listClasses = new ArrayList<>();
        for (Class<?> clazz : classes) {
            System.out.println("Classe : " + clazz.getSimpleName() + " - Package : " + clazz.getPackage().getName());
            listClasses.add(clazz);
        }
        return listClasses;
    }
    
    public List<Class<?>> getClassesWithAnnotationController(List<Class<?>> listClasse) {
        List<Class<?>> listClasses = new ArrayList<>();
        for (Class<?> clazz : listClasse) {
            if (clazz.isAnnotationPresent(Controller.class)) {
                listClasses.add(clazz);
            }
        }
        return listClasses;
    }

    public void validateUniqueUrlMappings(List<Class<?>> listClasseWithAnnotation) throws Exception {
        Map<UrlMethod, Method> seenMappings = new HashMap<>();

        for (Class<?> clazz : listClasseWithAnnotation) {
            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {
                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                if (urlMapping == null) {
                    continue;
                }

                UrlMethod urlMethod = new UrlMethod(urlMapping.value(), urlMapping.method());
                Method previousMethod = seenMappings.putIfAbsent(urlMethod, method);

                if (previousMethod != null) {
                    throw new Exception("Plusieurs méthodes avec le même mapping d'URL et méthode HTTP trouvées : "
                            + urlMethod.getUrl() + " [" + urlMethod.getMethod() + "]");
                }
            }
        }
    }

    // public List<Class<?>> getClassesWithAnnotationUrlMapping(List<Class<?>> listClasse) {
    //     List<Class<?>> listClasses = new ArrayList<>();
    //     for (Class<?> clazz : listClasse) {
    //         if (clazz.isAnnotationPresent(UrlMapping.class)) {
    //             listClasses.add(clazz);
    //         }
    //     }
    //     return listClasses;
    // }


    public Map<UrlMethod, Method> getUrlMappingClasses(
            List<Class<?>> listClasseWithAnnotation,
            UrlMethod urlMethod) throws Exception {

        Map<UrlMethod, Method> urlMappingClasses = new HashMap<>();
        List<UrlMapping> urlMaps = new ArrayList<>();

        for (Class<?> clazz : listClasseWithAnnotation) {

            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {

                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);

                if (urlMapping == null) {
                    continue;
                }

                String url = urlMapping.value();

                // Vérification du mapping
                if (matchUrl(url, urlMethod.getUrl())
                        && urlMapping.method().equalsIgnoreCase(urlMethod.getMethod())) {

                    urlMaps.add(urlMapping);

                    urlMappingClasses.put(urlMethod, method);
                }
            }
        }

        if (urlMaps.size() > 1) {
            throw new Exception(
                "Plusieurs méthodes avec le même mapping d'URL et méthode HTTP trouvées."
            );
        }

    return urlMappingClasses;
}

    public String knowApiOrUrlMapping() {
        return "UrlMapping";
    }
    
    public List<Map<UrlMethod, Method>> getUrlMappingNoMatchesUrl(List<Class<?>> listClasseWithAnnotation) {
        List<Map<UrlMethod, Method>> urlMappingClassesList = new ArrayList<>();
        for (Class<?> clazz : listClasseWithAnnotation) {
            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {
                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                if (urlMapping == null) {
                    continue;
                }
                String url = urlMapping.value();

                Map<UrlMethod, Method> urlMappingClasses = new HashMap<>();
                urlMappingClasses.put(new UrlMethod(url, urlMapping.method()), method);
                urlMappingClassesList.add(urlMappingClasses);
            }

        }
        return urlMappingClassesList;
    }

    private boolean matchUrl(String pattern, String url) {

        String regex = pattern.replaceAll("\\{[^/]+\\}", "[^/]+");

        return url.matches(regex);
    }
public Map<String, String> extractPathVariables(String pattern, String url) {

    Map<String, String> variables = new HashMap<>();

    String[] patternParts = pattern.split("/");
    String[] urlParts = url.split("/");

    for (int i = 0; i < patternParts.length; i++) {

        String patternPart = patternParts[i];

        if (patternPart.startsWith("{") && patternPart.endsWith("}")) {

            String variableName =
                    patternPart.substring(1, patternPart.length() - 1);

            String variableValue = urlParts[i];

            variables.put(variableName, variableValue);
        }
    }

    return variables;
}
}
