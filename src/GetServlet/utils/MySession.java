package GetServlet.utils;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import jakarta.servlet.http.HttpSession;

public class MySession {
    private Map<String, Object> attribut = new HashMap<>();
    private HttpSession httpSession;

    public MySession(HttpSession session){
        this.httpSession = session;

        if(session!=null){
            Enumeration<String> cle = httpSession.getAttributeNames();
            while(cle.hasMoreElements()){
                String key = cle.nextElement();
                Object value = session.getAttribute(key);
                
                attribut.put(key,value);
            }
        }
    }

    public void setAttribute(String cle, Object valeur) {
        attribut.put(cle, valeur);
        if(httpSession != null){
            httpSession.setAttribute(cle, valeur);
        }
    }

    public Object getAttribute(String cle) {
        return attribut.get(cle);
    }

    public Object removeAttribute(String cle) {
        Object val = attribut.remove(cle);
        if(httpSession != null){
            httpSession.removeAttribute(cle);
        }
        return val;
    }
}
