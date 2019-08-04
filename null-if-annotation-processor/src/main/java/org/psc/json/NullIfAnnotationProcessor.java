package org.psc.json;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@SupportedAnnotationTypes("org.psc.json.NullIf")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class NullIfAnnotationProcessor extends AbstractProcessor {

    private static final Set<String> BASE_TYPES =
            Set.of(Boolean.class, Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class,
                    Double.class, Void.class, String.class).stream().map(e -> e.getTypeName()).collect(Collectors.toSet());

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        log.info("processing annotation {}", "");

        for (TypeElement annotation : annotations) {
            for (Element element : roundEnv.getElementsAnnotatedWith(annotation)) {

                if (!BASE_TYPES.contains(element.asType().toString())){
                    throw new RuntimeException("invalid type " + element.asType().toString() +" for annotation org.psc.json.NullIf");
                }

                log.info(element.asType().getClass().toString());
                log.info(element.asType().toString());
                log.info(element.getSimpleName().toString());
            }
        }
        return true;
    }
}
