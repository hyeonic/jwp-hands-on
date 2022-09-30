package nextstep.study.di.stage3.context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * 스프링의 BeanFactory, ApplicationContext에 해당되는 클래스
 */
class DIContainer {

    private final Set<Object> beans;

    public DIContainer(final Set<Class<?>> classes) {
        this.beans = toInstances(classes);
        for (Object bean : beans) {
            injectPossibleFields(bean);
        }
    }

    private void injectPossibleFields(@Nullable final Object bean) {
        Class<?> beanClass = bean.getClass();
        for (Field field : beanClass.getDeclaredFields()) {
            inject(bean, field);
        }
    }

    private void inject(final Object bean, final Field field) {
        try {
            Object fieldValue = getBean(field.getType());
            if (fieldValue != null) {
                field.setAccessible(true);
                field.set(bean, fieldValue);
                field.setAccessible(false);
            }
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("존재하지 않는 field입니다.");
        }
    }

    private Set<Object> toInstances(final Set<Class<?>> classes) {
        return classes.stream()
                .map(this::toInstance)
                .collect(Collectors.toSet());
    }

    private Object toInstance(final Class<?> aClass) {
        try {
            Constructor<?> constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getBean(final Class<T> aClass) {
        return (T) beans.stream()
                .filter(bean -> aClass.isAssignableFrom(bean.getClass()))
                .findFirst()
                .orElse(null);
    }
}
