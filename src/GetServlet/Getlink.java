package GetServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import GetServlet.utils.UrlMethod;
import GetServlet.utils.Utilitaire;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class Getlink extends HttpServlet {
    public List<Class<?>> classes;
    
    
    public void init() throws ServletException {
        Utilitaire utilitaire = new Utilitaire();
         this.classes = utilitaire
                .getClassesWithAnnotationController(utilitaire.getAllClassesByPackageName("itu"));
    }

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

        String method = request.getMethod().toUpperCase();
        
        String pathInfo = request.getPathInfo();
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>Hello!</h1>");
        out.println("<p>Path Info : " + pathInfo + "</p>");

         UrlMethod urlMethod = new UrlMethod(pathInfo, method);
         Utilitaire utilitaire = new Utilitaire();
         Map<UrlMethod, Method> urlMapping = utilitaire.getUrlMappingClasses(classes, urlMethod);

         if (!urlMapping.isEmpty()) {
             for (Map.Entry<UrlMethod, Method> entry : urlMapping.entrySet()) {

                 out.println("<p>Url : " + entry.getKey().getUrl() + "   /Methode Http : " + entry.getKey().getMethod()
                         + "   /Method : " + entry.getValue().getName() + "   /Classe du Method : "
                         + entry.getValue().getDeclaringClass().getSimpleName() + "</p>");
                         try {
                                Method m = entry.getValue();
                                Class<?> clazz = m.getDeclaringClass();
                                Object instance = clazz.getDeclaredConstructor().newInstance();

                                Class<?>[] params = m.getParameterTypes();

                                if (params.length == 0) {
                                    m.invoke(instance);
                                } else if (params.length == 2) {
                                    m.invoke(instance, request, response);
                                }

                            } catch (NoSuchMethodException e) {
                                out.println("<p>Constructeur introuvable</p>");
                            } catch (InvocationTargetException e) {
                                out.println("<p>Erreur dans la méthode : " + e.getCause().getMessage() + "</p>");
                            } catch (IllegalAccessException e) {
                                out.println("<p>Méthode inaccessible</p>");
                            } catch (InstantiationException e) {
                                out.println("<p>Impossible d'instancier la classe</p>");
                            }
             }

         } else {
             List<Map<UrlMethod, Method>> urlMappingNoMatches = utilitaire.getUrlMappingNoMatchesUrl(classes);
             out.println("<p>Aucun mapping trouvé pour l'URL : " + pathInfo + "   avec la method : " + method + "</p>");
                 out.println("<p>Voici les mappings disponibles :</p>");
                 for (Map<UrlMethod, Method> mapping : urlMappingNoMatches) {
                     for (Map.Entry<UrlMethod, Method> entry : mapping.entrySet()) {
                         out.println("<p>Url : " + entry.getKey().getUrl() + "   /Methode Http : " + entry.getKey().getMethod() + "   / Method : " + entry.getValue().getName() + "   /Classe du Method : " + entry.getValue().getDeclaringClass().getSimpleName() + "</p>");
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
            // List<Class<?>> classes = utilitaire.getClassesWithAnnotationUrlMapping(utilitaire.getAllClasses());
        // Map<String, String> urlMapping = utilitaire.getUrlMappingClasses(classes, pathInfo);
        // for (Class<?> clazz : utilitaire.getAllClasses()) {
        //                 out.println("<p>Url : " + entry.getKey() + " Method : " + entry.getValue().getName() + " Classe du Method : " + entry.getValue().getDeclaringClass().getSimpleName() + "</p>");
        // }