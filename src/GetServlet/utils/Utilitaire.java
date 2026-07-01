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


    public Map<UrlMethod, Method> getUrlMappingClasses(List<Class<?>> listClasseWithAnnotation, UrlMethod urlMethod) throws Exception {
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
                if (urlMethod.equals(new UrlMethod(url, urlMapping.method()))) {
                    urlMaps.add(urlMapping);
                    urlMappingClasses.put(urlMethod, method);
                }
            }

        }
          if (urlMaps.size() > 1) {
              throw new Exception("Plusieurs méthodes avec le même mapping d'URL et méthode HTTP trouvées.");
                
            }
        return urlMappingClasses;
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
}
