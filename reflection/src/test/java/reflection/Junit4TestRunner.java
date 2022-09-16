package reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class Junit4TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit4Test> clazz = Junit4Test.class;
        Junit4Test instance = clazz.getDeclaredConstructor()
                .newInstance();

        // TODO Junit4Test에서 @MyTest 애노테이션이 있는 메소드 실행
        Method[] methods = clazz.getDeclaredMethods();
        Arrays.stream(methods)
                .filter(this::containsAnnotation)
                .forEach(method -> invoke(instance, method));
    }

    private boolean containsAnnotation(final Method method) {
        return method.isAnnotationPresent(MyTest.class);
    }

    private Object invoke(final Junit4Test instance, final Method method) {
        try {
            return method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
