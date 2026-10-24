package GetServlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import GetServlet.listener.SpringBeanInjector;
import GetServlet.models.MethodAndViews;
import GetServlet.utils.UrlMethod;
import GetServlet.utils.Utilitaire;
import GetServlet.annotation.UrlMapping;
import GetServlet.annotation.ApiRest;
import GetServlet.utils.Router;
import GetServlet.utils.RouteInfo;
import GetServlet.utils.JsonMapper;
import GetServlet.utils.AuthSessionConfig;
import GetServlet.binding.ParameterResolver;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.MultipartConfig;

// import GetServlet.utils.MySessionManager;

@MultipartConfig(
    fileSizeThreshold = 1024 * 1024,     
    maxFileSize = 10 * 1024 * 1024,  
    maxRequestSize = 50 * 1024 * 1024
)

public class Getlink extends HttpServlet {
    public List<Class<?>> classes;

    private String suffix;
    private String prefix;
    private ApplicationContext springContext;
    private Router router;
      
@Override
public void init() throws ServletException {
    Utilitaire utilitaire = new Utilitaire();

    this.classes = utilitaire.getClassesWithAnnotationController(
        utilitaire.getAllClassesByPackageName("itu")
    );

    this.router = new Router();
    this.router.registerController(this.classes);

    System.out.println("Classes avec l'annotation @Controller :");

    this.suffix = getServletContext().getInitParameter("suffix");
    this.prefix = getServletContext().getInitParameter("prefix");

    String contextConfigLocation =
        getServletContext().getInitParameter("contextConfigLocation");

    if (contextConfigLocation == null || contextConfigLocation.isBlank()) {
        contextConfigLocation = "applicationContext.xml";
    }

    if (contextConfigLocation.startsWith("classpath:")) {
        contextConfigLocation =
            contextConfigLocation.substring("classpath:".length());
    }

    this.springContext =
            new ClassPathXmlApplicationContext(contextConfigLocation);
}
    @Override
    public void destroy() {
        if (this.springContext instanceof ClassPathXmlApplicationContext context) {
            context.close();
        }
    }
    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        requestController(request, response);
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        requestController(request, response);
    }
    
    public void requestController(HttpServletRequest request,
                              HttpServletResponse response)
        throws ServletException, IOException {

    String method = request.getMethod().toUpperCase();
    String pathInfo = request.getPathInfo();

    UrlMethod urlMethod = new UrlMethod(pathInfo, method);
    Utilitaire utilitaire = new Utilitaire();

    try {

        Map<UrlMethod, Method> urlMapping =
                utilitaire.getUrlMappingClasses(classes, urlMethod);

        if (!urlMapping.isEmpty()) {

            for (Map.Entry<UrlMethod, Method> entry :
                    urlMapping.entrySet()) {

                try {

                    Method m = entry.getValue();
                    Class<?> clazz = m.getDeclaringClass();
                    UrlMapping mapping =m.getAnnotation(UrlMapping.class);

                    String urlPattern = mapping.value();
                    Object instance =clazz.getDeclaredConstructor().newInstance();
                    SpringBeanInjector.injectDependencies(instance,springContext);

                    Map<String, String> pathVariables = utilitaire.extractPathVariables( urlPattern,pathInfo);
                    System.out.println(
                            "Path variables = " + pathVariables
                    );

                    ParameterResolver resolver = new ParameterResolver();
                    AuthSessionConfig authSessionConfig = new AuthSessionConfig();
                    if (!authSessionConfig.checkAuthSession(
                            m,
                            request,
                            response
                    )) {
                        return;
                    }


                    Object[] args =resolver.resolve(m, request, pathVariables);
                    Object result = m.invoke(instance, args);

                    if (m.isAnnotationPresent(ApiRest.class)) {
                        response.setContentType("application/json");
                        response.getWriter().write(JsonMapper.toJson(result));
                        return;
                    }

                    if (result instanceof MethodAndViews) {

                        MethodAndViews methodAndViews =(MethodAndViews) result;

                        String views = prefix+ methodAndViews.getView()+ suffix;

                        Map<String, Object> model = methodAndViews.getModel();

                        for (Map.Entry<String, Object> modelEntry : model.entrySet()) {

                            request.setAttribute(
                                    modelEntry.getKey(),
                                    modelEntry.getValue()
                            );
                        }

                        request.getRequestDispatcher(views)
                               .forward(request, response);
                    }

                } catch (NoSuchMethodException e) {

                    System.out.println(
                            "Constructeur introuvable"
                    );

                } catch (InvocationTargetException e) {

                    System.out.println(
                            "Erreur dans la méthode : "
                            + e.getCause().getMessage()
                    );

                } catch (IllegalAccessException e) {

                    System.out.println(
                            "Méthode inaccessible"
                    );

                } catch (InstantiationException e) {

                    System.out.println(
                            "Impossible d'instancier la classe"
                    );
                }
            }
            return;
        }


        RouteInfo routeInfo = router.findRoute(pathInfo, method);

        if (routeInfo == null) {

            response.setStatus(HttpServletResponse.SC_NOT_FOUND);

            response.getWriter().write(
                    "Aucun mapping trouvé pour l'URL : "
                    + pathInfo
                    + " avec la méthode : "
                    + method
            );

            return;
        }

        try {

            Object result;

            Class<?>[] paramTypes =routeInfo.getMethod().getParameterTypes();

            if (paramTypes.length == 0) {
                result =routeInfo.getMethod().invoke(routeInfo.getControllerInstance());
            }
            else {
                

                String requestBody =request.getReader().lines().reduce("",(accumulator, actual) ->accumulator + actual);

                JsonMapper jsonMapper = new JsonMapper();

                Object param = jsonMapper.fromJson(requestBody,paramTypes[0]);

                result = routeInfo.getMethod().invoke(routeInfo.getControllerInstance(),param);
            }

            response.setContentType(
                    "application/json"
            );

            response.getWriter().write(
                    JsonMapper.toJson(result)
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().write(
                    "Erreur lors de l'invocation de la méthode : "
                    + e.getMessage()
            );
        }

    } catch (Exception e) {

        System.out.println(
                "Erreur lors de la récupération du mapping d'URL : "
                + e.getMessage()
        );
    }
}
}