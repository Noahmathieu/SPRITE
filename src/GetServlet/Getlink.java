package GetServlet;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import javax.servlet.http.HttpServlet;Request;
import javax.servlet.http.HttpServletResponse;

public class Getlink extends HttpServlet {
    
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        requestController(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        requestController(request, response);
    }

    public void requestController(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String name = request.getParameter("name");
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
    }
}