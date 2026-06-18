package GetServlet.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import GetServlet.annotation.Controller; 

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
}
