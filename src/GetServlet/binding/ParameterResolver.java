package GetServlet.binding;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import GetServlet.annotation.PathVariable;
import GetServlet.annotation.RequestParam;
import GetServlet.utils.MapFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

public class ParameterResolver {

    public Object[] resolve(
            Method method,
            HttpServletRequest request,
            Map<String, String> pathVariables) throws Exception {

        Parameter[] parameters = method.getParameters();

        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {

            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();

            if (parameter.isAnnotationPresent(RequestParam.class)) {

                RequestParam annotation =
                        parameter.getAnnotation(RequestParam.class);

                String name = annotation.value();

                if (type == MapFile.class) {
                    args[i] = resolveFile(request, name);
                    continue;
                }

                if (List.class.isAssignableFrom(type)) {

                    Class<?> elementClass = getListElementClass(parameter);

                    if (elementClass == MapFile.class) {
                        args[i] = resolveFileList(request, name);
                    } else if (isSimpleType(elementClass)) {
                        args[i] = resolveList(
                                request.getParameterValues(name),
                                elementClass);
                    } else {
                        args[i] = createAndBindObjectList(
                                elementClass,
                                request,
                                name);
                    }

                } else {
                    String value = request.getParameter(name);
                    args[i] = convert(value, type);
                }

                continue;
            }

            if (parameter.isAnnotationPresent(PathVariable.class)) {

                PathVariable annotation =
                        parameter.getAnnotation(PathVariable.class);

                String name = annotation.value();

                String value = pathVariables.get(name);

                args[i] = convert(value, type);

                continue;
            }

            if (List.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException(
                        "Le paramètre List de la méthode " + method.getName()
                        + " doit être annoté avec @RequestParam(\"nom\").");
            }

            args[i] = createAndBindObject(type, request, "");
        }

        return args;
    }


    private List<Object> createAndBindObjectList(
            Class<?> elementClass,
            HttpServletRequest request,
            String listName) throws Exception {

        Pattern pattern = Pattern.compile(
                "^" + Pattern.quote(listName) + "\\[(\\d+)\\]\\..+$");

        TreeSet<Integer> indexes = new TreeSet<>();

        Enumeration<String> names = request.getParameterNames();
        while (names.hasMoreElements()) {
            Matcher matcher = pattern.matcher(names.nextElement());
            if (matcher.matches()) {
                indexes.add(Integer.parseInt(matcher.group(1)));
            }
        }

        List<Object> result = new ArrayList<>();

        for (int index : indexes) {
            String prefix = listName + "[" + index + "].";
            result.add(createAndBindObject(elementClass, request, prefix));
        }

        return result;
    }


    private Object createAndBindObject(
            Class<?> clazz,
            HttpServletRequest request,
            String prefix) throws Exception {

        Object object = clazz.getDeclaredConstructor().newInstance();

        for (Method method : clazz.getDeclaredMethods()) {

            String methodName = method.getName();

            if (!methodName.startsWith("set")) {
                continue;
            }

            if (method.getParameterCount() != 1) {
                continue;
            }

            String propertyName = methodName.substring(3);

            propertyName = Character.toLowerCase(propertyName.charAt(0))
                    + propertyName.substring(1);

            Class<?> parameterType = method.getParameterTypes()[0];

            if (parameterType == MapFile.class) {
                MapFile file = resolveFile(request, prefix + propertyName);
                if (file != null) {
                    method.invoke(object, file);
                }
                continue;
            }

            String value = request.getParameter(prefix + propertyName);

            if (value == null) {
                continue;
            }

            Object convertedValue = convert(value, parameterType);

            method.invoke(object, convertedValue);
        }

        return object;
    }


    private boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null
                && contentType.toLowerCase().startsWith("multipart/");
    }

    private MapFile resolveFile(
            HttpServletRequest request,
            String name) throws Exception {

        if (!isMultipart(request)) {
            return null;
        }

        return toMapFile(request.getPart(name));
    }

    private List<MapFile> resolveFileList(
            HttpServletRequest request,
            String name) throws Exception {

        List<MapFile> result = new ArrayList<>();

        if (!isMultipart(request)) {
            return result;
        }

        for (Part part : request.getParts()) {
            if (name.equals(part.getName())) {
                MapFile file = toMapFile(part);
                if (file != null) {
                    result.add(file);
                }
            }
        }

        return result;
    }

    private MapFile toMapFile(Part part) throws Exception {

        if (part == null) {
            return null;
        }

        String submitted = part.getSubmittedFileName();

        if (submitted == null || submitted.isBlank()) {
            return null;
        }

        int slash = Math.max(
                submitted.lastIndexOf('/'),
                submitted.lastIndexOf('\\'));
        String fileName = submitted.substring(slash + 1);

        String name = fileName;
        String extension = "";

        int dot = fileName.lastIndexOf('.');
        if (dot > 0) {
            name = fileName.substring(0, dot);
            extension = fileName.substring(dot + 1);
        }

        byte[] content;
        try (InputStream in = part.getInputStream()) {
            content = in.readAllBytes();
        }

        return new MapFile(name, extension,content);
    }


    private Class<?> getListElementClass(Parameter parameter) {

        Type genericType = parameter.getParameterizedType();

        if (!(genericType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "Le type générique de la liste est absent.");
        }

        Type elementType =
                ((ParameterizedType) genericType).getActualTypeArguments()[0];

        if (!(elementType instanceof Class<?>)) {
            throw new IllegalArgumentException(
                    "Impossible de déterminer le type des éléments.");
        }

        return (Class<?>) elementType;
    }

    private boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type.isPrimitive()
                || type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Boolean.class;
    }

    private List<Object> resolveList(
            String[] values,
            Class<?> elementType) {

        List<Object> result = new ArrayList<>();

        if (values == null) {
            return result;
        }

        for (String value : values) {
            result.add(convert(value, elementType));
        }

        return result;
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

        if (type == long.class
                || type == Long.class) {

            return Long.parseLong(value);
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