package GetServlet.listener;

import java.util.List;

import GetServlet.utils.Utilitaire;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class StartupValidationListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
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