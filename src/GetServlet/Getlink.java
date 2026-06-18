package GetServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

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
        String packageName = request.getParameter("package");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>Hello!</h1>");
        if (packageName == null || packageName.isEmpty()) {
            out.println("<p>Veuillez fournir un nom de package en paramètre.</p>");
            out.println("</body></html>");
            return;
        } else {
        Utilitaire utilitaire = new Utilitaire();
        List<Class<?>> classes = utilitaire.getClassesWithAnnotationController(utilitaire.getAllClassesByPackageName(packageName));
        
        for (Class<?> clazz : classes) {
            out.println("<p>Classe : " + clazz.getSimpleName() + " - Package : " + clazz.getPackage().getName() + "</p>");
        }
    }
        out.println("</body></html>");
    }
}