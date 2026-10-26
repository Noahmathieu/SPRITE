package GetServlet.utils;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import jakarta.servlet.http.HttpSession;

import GetServlet.annotation.Auth;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthSessionConfig {
public boolean checkAuthSession(Method method, HttpServletRequest request,
        HttpServletResponse response) throws IOException {

    if (!method.isAnnotationPresent(Auth.class)) {
        return true;
    }

    Auth auth = method.getAnnotation(Auth.class);
    Object roleConfig = request.getServletContext().getAttribute("role");
    Object authConfig = request.getServletContext().getAttribute("auth");

    HttpSession session = request.getSession(false);

    if (auth.role().length > 0) {

        if (roleConfig == null || roleConfig.toString().isBlank()) {
            response.sendError(500, "La configuration du rôle est introuvable.");
            return false;
        }

        if (session == null) {
            response.sendError(401, "Accès refusé : veuillez vous authentifier.");
            return false;
        }

        Object userRole = session.getAttribute(roleConfig.toString());

        if (userRole != null) {
            for (String role : auth.role()) {
                if (role.equals(userRole.toString())) {
                    return true;
                }
            }
        }

        response.sendError(403, "Accès refusé : rôle non autoriisé.");
        return false;
    }

    if (authConfig == null || authConfig.toString().isBlank()) {
        response.sendError(500, "La configuration auth est introuvable dans le contexte.");
        return false;
    }

    if (session == null) {
        response.sendError(401, "Accès refusé : veuillez vous authentifier.");
        return false;
    }

    Object authValue = session.getAttribute(authConfig.toString());

    boolean authorized = authValue instanceof Boolean
            ? (Boolean) authValue
            : authValue instanceof String
                    ? !((String) authValue).isBlank()
                    : authValue != null;

    if (!authorized) {
        response.sendError(401, "Accès refusé : veuillez vous authentifier.");
        return false;
    }

    return true;
}
}
