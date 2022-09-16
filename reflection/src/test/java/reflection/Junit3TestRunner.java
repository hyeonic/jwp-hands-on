package reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class Junit3TestRunner {

    @Test
    void run() throws Exception {
        Class<Junit3Test> clazz = Junit3Test.class;
        Junit3Test instance = clazz.getDeclaredConstructor()
                .newInstance();

        // TODO Junit3Test에서 test로 시작하는 메소드 실행
        Method[] methods = clazz.getDeclaredMethods();
        Arrays.stream(methods)
                .filter(method -> method.getName().startsWith("test"))
                .forEach(method -> invoke(instance, method));
    }

    private Object invoke(final Junit3Test instance, final Method method) {
        try {
            return method.invoke(instance);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
