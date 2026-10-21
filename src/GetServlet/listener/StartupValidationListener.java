package GetServlet.listener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import GetServlet.utils.Utilitaire;
import GetServlet.utils.MySession;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class StartupValidationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Map<String, MySession> sessions = new ConcurrentHashMap<>();
        sce.getServletContext().setAttribute("session", sessions);

        Utilitaire utilitaire = new Utilitaire();
        try {
            List<Class<?>> classes = utilitaire.getClassesWithAnnotationController(
                    utilitaire.getAllClassesByPackageName("itu"));
            utilitaire.validateUniqueUrlMappings(classes);
            sce.getServletContext().log("Validation des mappings terminée avec succès.");
        } catch (Exception e) {
            String message = "Erreur de déploiement: " + e.getMessage();
            sce.getServletContext().log(message, e);
            System.err.println(message);
            e.printStackTrace(System.err);
            throw new RuntimeException(message, e);
        }
    }
}