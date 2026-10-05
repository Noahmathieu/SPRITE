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
            Map<String, String> pathVariables) throws Exception {

        Parameter[] parameters = method.getParameters();

        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {

            Parameter parameter = parameters[i];



            if (parameter.isAnnotationPresent(RequestParam.class)) {

                RequestParam annotation =
                        parameter.getAnnotation(RequestParam.class);

                String name = annotation.value();

                String value = request.getParameter(name);

                args[i] = convert(
                        value,
                        parameter.getType()
                );

                continue;
            }



            if (parameter.isAnnotationPresent(PathVariable.class)) {

                PathVariable annotation =
                        parameter.getAnnotation(PathVariable.class);

                String name = annotation.value();

                String value = pathVariables.get(name);

                args[i] = convert(
                        value,
                        parameter.getType()
                );

                continue;
            }


            args[i] = createAndBindObject(
                    parameter.getType(),
                    request
            );
        }

        return args;
    }




    private Object createAndBindObject(
            Class<?> clazz,
            HttpServletRequest request) throws Exception {

        // 1. Créer l'objet
        Object object =
                clazz.getDeclaredConstructor().newInstance();

        Method[] methods =
                clazz.getDeclaredMethods();

        for (Method method : methods) {

            String methodName = method.getName();

            if (!methodName.startsWith("set")) {
                continue;
            }

            if (method.getParameterCount() != 1) {
                continue;
            }

            String propertyName =
                    methodName.substring(3);

            propertyName =
                    Character.toLowerCase(
                            propertyName.charAt(0)
                    )
                    + propertyName.substring(1);

            String value =
                    request.getParameter(propertyName);

            if (value == null) {
                continue;
            }

            Class<?> parameterType =
                    method.getParameterTypes()[0];

            
            Object convertedValue =
                    convert(
                            value,
                            parameterType
                    );

            method.invoke(
                    object,
                    convertedValue
            );
        }

        return object;
    }




    private Object convert(
            String value,
            Class<?> type) {

        if (value == null) {
            return null;
        }

        if (type == String.class) {
            return value;
        }

        if (type == int.class
                || type == Integer.class) {

            return Integer.parseInt(value);
        }

        if (type == double.class
                || type == Double.class) {

            return Double.parseDouble(value);
        }

        if (type == boolean.class
                || type == Boolean.class) {

            return Boolean.parseBoolean(value);
        }

        return value;
    }
}