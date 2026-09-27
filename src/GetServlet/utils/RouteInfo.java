package GetServlet.utils;

import java.lang.reflect.Method;

public class RouteInfo {
    private final Object controllerInstance;
    private final Method method;

    public RouteInfo(Object controllerInstance, Method method) {
        this.controllerInstance = controllerInstance;
        this.method = method;
    }

    public Object getControllerInstance() { return controllerInstance; }
    public Method getMethod() { return method; }
}