package GetServlet.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MySessionManager {

    private final ConcurrentMap<String, MySession> sessions =
            new ConcurrentHashMap<>();

    public MySession getSession(String id) {
        return sessions.get(id);
    }

    public MySession createSession(String id) {
        return sessions.computeIfAbsent(id, MySession::new);
    }

    public void removeSession(String id) {
        sessions.remove(id);
    }
}