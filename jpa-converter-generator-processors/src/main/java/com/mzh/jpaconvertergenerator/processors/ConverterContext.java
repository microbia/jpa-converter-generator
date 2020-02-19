package com.mzh.jpaconvertergenerator.processors;

import com.google.common.base.Preconditions;
import com.mzh.jpaconvertergenerator.annotations.EnumCreator;
import com.mzh.jpaconvertergenerator.annotations.EnumValue;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Some description.
 *
 * @author ChenLingshu
 */
class ConverterContext {

    private final TypeElement enumClazz;
    private final Logger logger;
    private final Types typeUtils;

    ConverterContext(TypeElement enumClazz, ProcessingEnvironment environment) {
        Preconditions.checkNotNull(environment);
        this.enumClazz = enumClazz;
        typeUtils = environment.getTypeUtils();
        this.logger = new Logger(environment.getMessager());
    }

    private static PackageElement getPackageElement(TypeElement enumClazz) {
        Element element = enumClazz;
        while (element.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
            element = element.getEnclosingElement();
        }
        return (PackageElement) element.getEnclosingElement();
    }

    JavaFile generate() {
        PackageElement packageElement = getPackageElement(enumClazz);
        ExecutableElement creatorExecutable = getMethodWithAnnotated(EnumCreator.class);
        ExecutableElement toValueExecutable = getMethodWithAnnotated(EnumValue.class);
        checkCreatorAndValue(creatorExecutable, toValueExecutable);

        TypeMirror enumType = getBoxedType(creatorExecutable.getReturnType());
        TypeMirror valueType = getBoxedType(toValueExecutable.getReturnType());

        TypeSpec converter = StuffFactory.converter(enumClazz, enumType, valueType,
                toValueExecutable, creatorExecutable);

        return JavaFile.builder(packageElement.toString(), converter)
                .build();
    }

    private void checkCreatorAndValue(ExecutableElement creator, ExecutableElement toValue) {
        // parameters.size == 1
        if (creator.getParameters().size() != 1) {
            logger.error("Size of parameters should be 1.", creator);
        }

        // parameters.
        if (toValue.getParameters().size() != 0) {
            logger.error("Size of parameters should be 0.", toValue);
        }

        // return = parameter
        TypeMirror creatorParameter = getBoxedType(creator.getParameters().get(0).asType());
        TypeMirror valueReturnType = getBoxedType(toValue.getReturnType());
        if (!typeUtils.isSameType(creatorParameter, valueReturnType)) {
            logger.warn(String.format("%s %s", creatorParameter, valueReturnType));
            logger.error("Creator parameter and Value returning must be SAME type", this.enumClazz);
        }
    }

    private ExecutableElement getMethodWithAnnotated(Class<? extends Annotation> annotationClazz) {
        List<? extends Element> enclosedElements = enumClazz.getEnclosedElements();
        List<ExecutableElement> annotatedMethods = enclosedElements.stream()
                .filter(ExecutableElement.class::isInstance)
                .map(ExecutableElement.class::cast)
                .filter(element -> element.getAnnotation(annotationClazz) != null)
                .collect(Collectors.toList());

        if (annotatedMethods.size() > 1) {
            logger.error(String.format("Not unique %s annotated method found.", annotationClazz.getSimpleName()),
                    this.enumClazz);
        } else if (annotatedMethods.size() == 0) {
            logger.error(String.format("Method annotate by %s is not found", annotationClazz.getSimpleName()),
                    this.enumClazz);
        }

        return annotatedMethods.get(0);
    }

    private TypeMirror getBoxedType(TypeMirror origin) {
        if (origin.getKind().isPrimitive()) {
            return typeUtils.boxedClass((PrimitiveType) origin).asType();
        }
        return origin;
    }

}
