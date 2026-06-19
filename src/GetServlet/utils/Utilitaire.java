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

    // public List<Class<?>> getClassesWithAnnotationUrlMapping(List<Class<?>> listClasse) {
    //     List<Class<?>> listClasses = new ArrayList<>();
    //     for (Class<?> clazz : listClasse) {
    //         if (clazz.isAnnotationPresent(UrlMapping.class)) {
    //             listClasses.add(clazz);
    //         }
    //     }
    //     return listClasses;
    // }

    public Map<String, String> getUrlMappingClasses(List<Class<?>> listClasseWithAnnotation, String urlMap) {
        Map<String, String> urlMappingClasses = new HashMap<>();
        for (Class<?> clazz : listClasseWithAnnotation) {
            Method[] methods = clazz.getDeclaredMethods();

            for (Method method : methods) {
                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                String url = urlMapping.value();
                if (url.equals(urlMap)) {
                    urlMappingClasses.put("url", url);
                    urlMappingClasses.put("class", clazz.getSimpleName());
                    urlMappingClasses.put("method", method.getName());
                }
            }

        }
        return urlMappingClasses;
    }
    
    public List<Map<String, String>> getUrlMappingNoMatchesUrl(List<Class<?>> listClasseWithAnnotation) {
        List<Map<String, String>> urlMappingClassesList = new ArrayList<>();
        for (Class<?> clazz : listClasseWithAnnotation) {
            Method[] methods = clazz.getDeclaredMethods();
            
            for (Method method : methods) {
                UrlMapping urlMapping = method.getAnnotation(UrlMapping.class);
                    String url = urlMapping.value();
                    
                        Map<String, String> urlMappingClasses = new HashMap<>();
                        urlMappingClasses.put("url", url);
                        urlMappingClasses.put("class", clazz.getSimpleName());
                        urlMappingClasses.put("method", method.getName());
                        urlMappingClassesList.add(urlMappingClasses);
            }       
            
        }
        return urlMappingClassesList;
    }
}
