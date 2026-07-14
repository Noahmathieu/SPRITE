package GetServlet.listener;

import java.lang.reflect.Field;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import GetServlet.annotation.Inject;

public class SpringBeanInjector {

    public static void injectDependencies(Object instance, ApplicationContext context) {
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                try {
                    Object bean = context.getBean(field.getType());
                    field.set(instance, bean);
                } catch (BeansException | IllegalAccessException e) {
                    System.out.println("Impossible d'injecter le champ : " + field.getName());
                }
            }
        }
    }
}