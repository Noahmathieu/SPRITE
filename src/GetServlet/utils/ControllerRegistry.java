package GetServlet.utils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import GetServlet.listener.SpringBeanInjector;

import org.springframework.context.ApplicationContext;

public class ControllerRegistry {

    private final Map<Class<?>, Object> controllers;

    public ControllerRegistry() {
        this.controllers = new ConcurrentHashMap<Class<?>, Object>();
    }



    public void registerControllers(List<Class<?>> classes,ApplicationContext springContext) throws Exception {

        for (Class<?> clazz : classes) {

            if (controllers.containsKey(clazz)) {
                continue;
            }

            Constructor<?> constructor = clazz.getDeclaredConstructor();

            Object instance = constructor.newInstance();

            SpringBeanInjector.injectDependencies(instance, springContext);

            controllers.put(clazz, instance);
        }
    }

    public Object getController(Class<?> clazz) {

        Object instance = controllers.get(clazz);

        if (instance == null) {
            throw new IllegalArgumentException(
                "Aucun contrôleur enregistré pour : " + clazz.getName()
            );
        }

        return instance;
    }
}
