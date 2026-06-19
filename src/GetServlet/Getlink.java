package GetServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import GetServlet.utils.Utilitaire;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Getlink extends HttpServlet {
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        requestController(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        requestController(request, response);
    }

    public void requestController(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>Hello!</h1>");
        out.println("<p>Path Info : " + pathInfo + "</p>");

        Utilitaire utilitaire = new Utilitaire();
        
        // List<Class<?>> classes = utilitaire.getClassesWithAnnotationUrlMapping(utilitaire.getAllClasses());
        // Map<String, String> urlMapping = utilitaire.getUrlMappingClasses(classes, pathInfo);

        List<Class<?>> classes = utilitaire
                .getClassesWithAnnotationController(utilitaire.getAllClassesByPackageName("itu"));
         
            Map<String, String> urlMapping = utilitaire.getUrlMappingClasses(classes, pathInfo);
         
        
        
        for (Class<?> clazz : utilitaire.getAllClasses()) {
            out.println("<p>Classe : " + clazz.getSimpleName() + " - Package : " + clazz.getPackage().getName() + "</p>");
        }
        
        if (!urlMapping.isEmpty()) {
            for (Map.Entry<String, String> entry : urlMapping.entrySet()) {
                out.println("<p>Classe : " + entry.getKey() + " - et : " + entry.getValue() + "</p>");
            }
        } else {
            List<Map<String, String>> urlMappingNoMatches = utilitaire.getUrlMappingNoMatchesUrl(classes);
            out.println("<p>Aucun mapping trouvé pour l'URL : " + pathInfo + "</p>");
                out.println("<p>Voici les mappings disponibles :</p>");
                for (Map<String, String> mapping : urlMappingNoMatches) {
                    for (Map.Entry<String, String> entry : mapping.entrySet()) {
                        out.println("<p>Classe : " + entry.getKey() + " - et : " + entry.getValue() + "</p>");
                }
            }
        }
        out.println("</body></html>");
    }
   
}


 // public void requestController(HttpServletRequest request, HttpServletResponse response)
    //         throws ServletException, IOException {
    //     String packageName = request.getParameter("package");
    //     response.setContentType("text/html");
    //     PrintWriter out = response.getWriter();
    //     out.println("<html><body>");
    //     out.println("<h1>Hello!</h1>");
    //     if (packageName == null || packageName.isEmpty()) {
    //         out.println("<p>Veuillez fournir un nom de package en paramètre.</p>");
    //         out.println("</body></html>");
    //         return;
    //     } else {
    //         out.println("<p>Nom du package : " + packageName + "</p>");
    //     Utilitaire utilitaire = new Utilitaire();
    //     List<Class<?>> classes = utilitaire.getClassesWithAnnotationController(utilitaire.getAllClassesByPackageName(packageName));
        
    //     for (Class<?> clazz : classes) {
    //         out.println("<p>Classe : " + clazz.getSimpleName() + " - Package : " + clazz.getPackage().getName() + "</p>");
    //     }
    // }
    //     out.println("</body></html>");
    // }