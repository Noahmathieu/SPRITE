package GetServlet.utils;

import java.util.HashMap;
import java.util.Map;

public class MySession {

    private final String id;

    private final Map<String, Object> attributes = new HashMap<>();

    public MySession(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }
}