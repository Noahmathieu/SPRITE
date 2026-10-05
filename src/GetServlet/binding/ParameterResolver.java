package GetServlet.binding;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

import GetServlet.annotation.PathVariable;
import GetServlet.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;


public class ParameterResolver {

    public Object[] resolve(
        Method method,
        HttpServletRequest request,
        Map<String, String> pathVariables) {

    Parameter[] parameters = method.getParameters();

    Object[] args = new Object[parameters.length];

    for (int i = 0; i < parameters.length; i++) {

        Parameter parameter = parameters[i];

        // @RequestParam
        if (parameter.isAnnotationPresent(RequestParam.class)) {

            RequestParam annotation =
                    parameter.getAnnotation(RequestParam.class);

            String name = annotation.value();

            String value = request.getParameter(name);

            args[i] = convert(value, parameter.getType());
        }

        // @PathVariable
        if (parameter.isAnnotationPresent(PathVariable.class)) {

            PathVariable annotation =
                    parameter.getAnnotation(PathVariable.class);

            String name = annotation.value();

            String value = pathVariables.get(name);

            args[i] = convert(value, parameter.getType());
        }
    }

    return args;
}

    private Object convert(String value, Class<?> type) {

        if (type == String.class) {
            return value;
        }

        if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        }

        if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        }

        if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        }

        return value;
    }
}