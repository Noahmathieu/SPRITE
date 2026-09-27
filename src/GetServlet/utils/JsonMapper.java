package GetServlet.utils;

import java.lang.reflect.*;
import java.util.*;

public class JsonMapper {

    public static String toJson(Object obj) throws IllegalAccessException {
        if (obj == null) return "null";

        if (obj instanceof String) return "\"" + obj + "\"";
        if (obj instanceof Number || obj instanceof Boolean) return obj.toString();

        if (obj instanceof List<?> list) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                sb.append(toJson(list.get(i)));
                if (i < list.size() - 1) sb.append(",");
            }
            return sb.append("]").toString();
        }

        // Objet "classique" -> on lit ses champs via reflection
        StringBuilder sb = new StringBuilder("{");
        Field[] fields = obj.getClass().getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            field.setAccessible(true); // pour lire les champs private
            sb.append("\"").append(field.getName()).append("\":");
            sb.append(toJson(field.get(obj)));
            if (i < fields.length - 1) sb.append(",");
        }
        return sb.append("}").toString();
    }

    // Version simplifiée : à compléter avec un vrai parseur JSON
    public static <T> T fromJson(String json, Class<T> clazz) {
        // TODO: parser le JSON et remplir les champs via reflection
        throw new UnsupportedOperationException("À implémenter");
    }
}