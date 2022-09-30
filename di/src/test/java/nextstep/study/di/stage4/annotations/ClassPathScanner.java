package nextstep.study.di.stage4.annotations;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.reflections.Reflections;

public class ClassPathScanner {

    private static final Set<Class<? extends Annotation>> ANNOTATION_CLASSES = Set.of(Repository.class, Service.class);

    public static Set<Class<?>> getAllClassesInPackage(final String packageName) {
        Reflections reflections = new Reflections(packageName);
        return ANNOTATION_CLASSES.stream()
                .map(reflections::getTypesAnnotatedWith)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
    }
}
