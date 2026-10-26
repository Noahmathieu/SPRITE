package GetServlet.binding;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import GetServlet.annotation.PathVariable;
import GetServlet.annotation.RequestParam;
import GetServlet.utils.MapFile;
import GetServlet.utils.AppConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import GetServlet.utils.MySession;
// import GetServlet.utils.MySessionManager;
import jakarta.servlet.http.HttpSession;

public class ParameterResolver {

// public ParameterResolver(String sessionAttributeName) {
//     this.sessionAttributeName = sessionAttributeName;
// }

private String getUploadDirectory(HttpServletRequest request) {
    String dir = AppConfig.get("upload.directory");

    if (dir == null || dir.isBlank()) {
        throw new IllegalStateException(
                "La clé 'upload.directory' est absente de application.properties.");
    }
    return dir;
}

    public Object[] resolve(Method method,HttpServletRequest request,Map<String, String> pathVariables) throws Exception {

        Parameter[] parameters = method.getParameters();

        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {

            Parameter parameter = parameters[i];
            Class<?> type = parameter.getType();

            if (parameter.isAnnotationPresent(RequestParam.class)) {

                RequestParam annotation = parameter.getAnnotation(RequestParam.class);

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
                        args[i] = resolveList(request.getParameterValues(name),elementClass);
                    } else {
                        args[i] = createAndBindObjectList( elementClass,request,name);
                    }

                } else {
                    String value = request.getParameter(name);
                    args[i] = convert(value, type);
                }

                continue;
            }

            if (parameter.isAnnotationPresent(PathVariable.class)) {

                PathVariable annotation =parameter.getAnnotation(PathVariable.class);

                String name = annotation.value();

                String value = pathVariables.get(name);

                args[i] = convert(value, type);

                continue;
            }
            if (type == MySession.class) {
                        Map<String, MySession> session =(Map<String, MySession>) request.getServletContext().getAttribute("session");
                        if (session == null) {
                            session = new ConcurrentHashMap<>();
                            request.getServletContext().setAttribute("session", session);
                        }

                        HttpSession httpSession = request.getSession(true);
                        String idSession = httpSession.getId();
                        MySession mysession;
                        if (session.containsKey(idSession)) {
                            mysession = session.get(idSession);
                        } else {
                            mysession = new MySession(httpSession);
                            session.put(idSession, mysession);
                        }
                        args[i] = mysession;
                        continue;
        
            }
            if (List.class.isAssignableFrom(type)) {
                throw new IllegalArgumentException("Le paramètre List de la méthode " + method.getName()+ " doit être annoté avec @RequestParam(\"nom\").");
            }
            args[i] = createAndBindObject(type, request, "");
        }

        return args;
    }


    private List<Object> createAndBindObjectList(
            Class<?> elementClass,
            HttpServletRequest request,
            String listName) throws Exception {

        Pattern pattern = Pattern.compile("^" + Pattern.quote(listName) + "\\[(\\d+)\\]\\..+$");

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


   private Object createAndBindObject(Class<?> clazz,HttpServletRequest request,String prefix) throws Exception {

       Object object = clazz.getDeclaredConstructor().newInstance();

       for (Method method : clazz.getMethods()) { 

           String methodName = method.getName();

           if (!methodName.startsWith("set") || methodName.length() == 3) {
               continue;
           }
           if (method.getParameterCount() != 1) {
               continue;
           }

           String propertyName = Character.toLowerCase(methodName.charAt(3))+ methodName.substring(4);

           Class<?> parameterType = method.getParameterTypes()[0];

           if (parameterType == MapFile.class) {
               MapFile file = resolveFile(request, prefix + propertyName);
               if (file != null) {
                   method.invoke(object, file);
               }
               continue;
           }

           if (List.class.isAssignableFrom(parameterType)) {
               Class<?> elementClass = getListElementClassOrNull(method.getParameters()[0]);
               if (elementClass == null) {
                   continue;
               }
               String listName = prefix + propertyName;
               List<?> list;
               if (elementClass == MapFile.class) {
                   list = resolveFileList(request, listName);
               } else if (isSimpleType(elementClass)) {
                   list = resolveList(request.getParameterValues(listName), elementClass);
               } else {
                   list = createAndBindObjectList(elementClass, request, listName);
               }
               if (!list.isEmpty()) {
                   method.invoke(object, list);
               }
               continue;
           }

           if (!isSimpleType(parameterType)) {
               String nestedPrefix = prefix + propertyName + ".";
               if (hasParametersWithPrefix(request, nestedPrefix)) {
                   method.invoke(object, createAndBindObject(parameterType, request, nestedPrefix));
               }
               continue;
           }

           String value = request.getParameter(prefix + propertyName);
           if (value == null || value.isEmpty()) {
               continue;
           }
           method.invoke(object, convert(value, parameterType));
       }

       return object;
   }
private Class<?> getListElementClassOrNull(Parameter parameter) {
    Type genericType = parameter.getParameterizedType();
    if (!(genericType instanceof ParameterizedType)) {
        return null;
    }
    Type elementType = ((ParameterizedType) genericType).getActualTypeArguments()[0];
    return (elementType instanceof Class<?>) ? (Class<?>) elementType : null;
}
    private boolean hasParametersWithPrefix(HttpServletRequest request, String prefix) {
    Enumeration<String> names = request.getParameterNames();
    while (names.hasMoreElements()) {
        if (names.nextElement().startsWith(prefix)) return true;
    }
    return false;
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

        return toMapFile(request.getPart(name), request);
    }

    private List<MapFile> resolveFileList(HttpServletRequest request,String name) throws Exception {
        List<MapFile> result = new ArrayList<>();

        if (!isMultipart(request)) {
            return result;
        }

        for (Part part : request.getParts()) {
            if (name.equals(part.getName())) {
                MapFile file = toMapFile(part,request);
                if (file != null) {
                    result.add(file);
                }
            }
        }

        return result;
    }

    private MapFile toMapFile(Part part, HttpServletRequest request) throws Exception {

        if (part == null) {
            return null;
        }

        String submitted = part.getSubmittedFileName();

        if (submitted == null || submitted.isBlank()) {
            return null;
        }

        int slash = Math.max(submitted.lastIndexOf('/'),submitted.lastIndexOf('\\'));
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
        Path dir = Paths.get(getUploadDirectory(request));
        Files.createDirectories(dir);

        String fullName = extension.isEmpty() ? name : name + "." + extension;
        Path target = dir.resolve(fullName).normalize();

        if (!target.startsWith(dir.normalize())) {
            throw new SecurityException("Nom de fichier invalide : " + fullName);
        }

    Files.write(target, content);

        return new MapFile(name, extension,content);
    }


    private Class<?> getListElementClass(Parameter parameter) {

        Type genericType = parameter.getParameterizedType();

        if (!(genericType instanceof ParameterizedType)) {
            throw new IllegalArgumentException(
                    "Le type générique de la liste est absent.");
        }

        Type elementType =((ParameterizedType) genericType).getActualTypeArguments()[0];

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
            || type == Float.class
            || type == Short.class
            || type == Byte.class
            || type == Boolean.class
            || type == java.math.BigDecimal.class
            || type == java.time.LocalDate.class
            || type == java.time.LocalDateTime.class
            || type == java.time.LocalTime.class
            || type == java.sql.Date.class
            || type.isEnum();
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

@SuppressWarnings({"unchecked", "rawtypes"})
private Object convert(String value, Class<?> type) {

    if (value == null || (value.isEmpty() && type != String.class)) {
        return defaultFor(type);
    }

    if (type == String.class) return value;

    if (type == int.class     || type == Integer.class) return Integer.parseInt(value);
    if (type == long.class    || type == Long.class)    return Long.parseLong(value);
    if (type == double.class  || type == Double.class)  return Double.parseDouble(value);
    if (type == float.class   || type == Float.class)   return Float.parseFloat(value);
    if (type == short.class   || type == Short.class)   return Short.parseShort(value);
    if (type == byte.class    || type == Byte.class)    return Byte.parseByte(value);
    if (type == boolean.class || type == Boolean.class) return Boolean.parseBoolean(value);
    if (type == char.class)                             return value.charAt(0);

    if (type == java.math.BigDecimal.class)    return new java.math.BigDecimal(value);
    if (type == java.time.LocalDate.class)     return java.time.LocalDate.parse(value);
    if (type == java.time.LocalDateTime.class) return java.time.LocalDateTime.parse(value);
    if (type == java.time.LocalTime.class)     return java.time.LocalTime.parse(value);
    if (type == java.sql.Date.class)           return java.sql.Date.valueOf(value);

    if (type.isEnum()) return Enum.valueOf((Class<Enum>) type, value);

    return value;
}
    private Object defaultFor(Class<?> type) {
    if (!type.isPrimitive())   return null;
    if (type == boolean.class) return false;
    if (type == char.class)    return '\0';
    if (type == byte.class)    return (byte) 0;
    if (type == short.class)   return (short) 0;
    if (type == int.class)     return 0;
    if (type == long.class)    return 0L;
    if (type == float.class)   return 0f;
    return 0d;                 // double
}
}