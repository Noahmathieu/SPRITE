package GetServlet.utils;

import java.io.IOException;
import java.lang.reflect.Method;

import GetServlet.annotation.Auth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthSessionConfig {
    public boolean checkAuthSession(Method method,HttpServletRequest request,HttpServletResponse response) throws IOException {
        if (!method.isAnnotationPresent(Auth.class)) {
            return true;
        }

        Object authConfig = request.getServletContext().getAttribute("auth");
        String sessionAttributeName = authConfig != null ? authConfig.toString() : null;

        if (sessionAttributeName == null || sessionAttributeName.isBlank()) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,"La configuration auth est introuvable dans le contexte.");
            return false;
        }
        Object authValue = null;
        if (request.getSession(false) != null) {
            authValue = request.getSession(false).getAttribute(sessionAttributeName);
        }
        boolean authorized = false;
        if (authValue instanceof Boolean) {
            authorized = (Boolean) authValue;
        } else if (authValue instanceof String) {
            authorized = !((String) authValue).isBlank();
        } else {
            authorized = authValue != null;
        }
        if (!authorized) {
            response.sendError( HttpServletResponse.SC_UNAUTHORIZED,"Acces refuse: faites des authentification.");
            return false;
        }
        return true;
    }
}
