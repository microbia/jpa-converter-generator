package com.mzh.jpaconvertergenerator.processors;

import com.google.common.collect.ImmutableSet;
import com.mzh.jpaconvertergenerator.annotations.EnumConverter;
import com.mzh.jpaconvertergenerator.annotations.EnumCreator;
import com.mzh.jpaconvertergenerator.annotations.EnumValue;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.Set;

/**
 * Some description.
 *
 * @author ChenLingshu
 */
@SupportedAnnotationTypes({
        "com.mzh.jpaconvertergenerator.annotations.*"
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class EnumAttributeConverterProcessor extends AbstractProcessor {

    private Elements elementUtils;
    private Types typeUtils;
    private Logger logger;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        this.logger = new Logger(processingEnv.getMessager());
        logger.info("Entering EnumAttributeConverterProcessor.");
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
        typeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(EnumCreator.class.getCanonicalName(),
                EnumValue.class.getCanonicalName(),
                EnumConverter.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        if (SourceVersion.latest().compareTo(SourceVersion.RELEASE_8) > 0) {
            return SourceVersion.latest();
        } else {
            return SourceVersion.RELEASE_8;
        }
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> annotatedElement = roundEnv.getElementsAnnotatedWith(EnumConverter.class);
        annotatedElement.stream()
                .filter(TypeElement.class::isInstance)
                .map(TypeElement.class::cast)
                .forEach(this::generateAttributeConverter);
        return true;
    }

    private void generateAttributeConverter(TypeElement element) {
        ConverterContext converterContext = new ConverterContext(element, processingEnv);
        Filer filer = processingEnv.getFiler();
        try {
            converterContext.generate().writeTo(filer);
        } catch (IOException e) {
            logger.error(String.format("Error %s", e.getMessage()), element);
        }
    }

}
