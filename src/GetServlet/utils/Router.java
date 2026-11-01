package GetServlet.utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import GetServlet.annotation.ApiRest;

public class Router {

    private Map<UrlMethod, RouteInfo> routes = new HashMap<UrlMethod, RouteInfo>();

    private ControllerRegistry controllerRegistry;

    public Router(ControllerRegistry controllerRegistry) {
        this.controllerRegistry = controllerRegistry;
    }

    public void registerController(List<Class<?>> controllerClasses) {

        for (Class<?> clazz : controllerClasses) {

            try {

                Object controllerInstance =controllerRegistry.getController(clazz);

                for (Method method : clazz.getDeclaredMethods()) {

                    if (method.isAnnotationPresent(ApiRest.class)) {

                        ApiRest annotation = method.getAnnotation(ApiRest.class);

                        if (annotation.path() == null || annotation.path().trim().isEmpty()) {
                            continue;
                        }

                        UrlMethod key = new UrlMethod(annotation.path(),annotation.method());
                        RouteInfo value = new RouteInfo(controllerInstance,method);

                        routes.put(key, value);
                        System.out.println("Route enregistrée : "+ annotation.method()+ " "+ annotation.path());
                    }
                }

            } catch (Exception e) {

                System.out.println("Impossible d'enregistrer "+ clazz.getName()+ " : "+ e.getMessage());
            }
        }
    }

    public RouteInfo findRoute(String url, String httpMethod) {

        return routes.get(new UrlMethod(url, httpMethod));
    }
}