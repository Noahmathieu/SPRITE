package GetServlet.utils;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static Properties props;
    static {
        props = new Properties();
        try (InputStream is = AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);

            } else {
               throw new RuntimeException("application.properties introuvable dans le classpath");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static String get(String key) {
    return props.getProperty(key);
}
}
