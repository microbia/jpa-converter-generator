package com.mzh.jpaconvertergenerator.processors;

import com.mzh.jpaconvertergenerator.annotations.EnumConverter;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Some description.
 *
 * @author ChenLingshu
 */
public final class StuffFactory {

    private StuffFactory() {

    }

    public static AnnotationSpec converterAnnotation(EnumConverter enumConverter) {
        return AnnotationSpec.builder(Converter.class)
                .addMember("autoApply", String.valueOf(enumConverter.autoApply()))
                .build();
    }

    public static ParameterizedTypeName typeName(TypeMirror enumType, TypeMirror codeType) {
        return ParameterizedTypeName.get(
                ClassName.get(AttributeConverter.class),
                ClassName.get(enumType),
                ClassName.get(codeType)
        );
    }

    public static MethodSpec serializer(TypeMirror enumType, TypeMirror codeType, ExecutableElement serializeMethod) {
        return MethodSpec.methodBuilder("convertToDatabaseColumn")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.get(codeType))
                .addParameter(ParameterSpec.builder(TypeName.get(enumType), "attribute").build())
                .beginControlFlow("if ($L == null)", "attribute")
                .addStatement("return null")
                .endControlFlow()
                .addStatement("return $N.$L()", "attribute", serializeMethod.getSimpleName())
                .build();
    }


    public static MethodSpec deserializer(TypeMirror enumType, TypeMirror codeType,
                                          TypeElement enumClazz, ExecutableElement deserializeMethod) {
        return MethodSpec.methodBuilder("convertToEntityAttribute")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(TypeName.get(enumType))
                .addParameter(ParameterSpec.builder(TypeName.get(codeType), "code").build())
                .beginControlFlow("if ($L == null)", "code")
                .addStatement("return null")
                .endControlFlow()
                .addStatement("return $T.$L($L)", enumClazz, deserializeMethod.getSimpleName(), "code")
                .build();
    }

    public static TypeSpec converter(TypeElement enumClazz, TypeMirror enumType, TypeMirror codeType,
                                     ExecutableElement value, ExecutableElement creator) {
        EnumConverter enumConverter = enumClazz.getAnnotation(EnumConverter.class);
        AnnotationSpec converterAnnotation = StuffFactory.converterAnnotation(enumConverter);
        String converterName = String.format("%sEnumConverter", enumClazz.getSimpleName());

        return TypeSpec.classBuilder(converterName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addAnnotation(converterAnnotation)
                .addSuperinterface(StuffFactory.typeName(enumType, codeType))
                .addMethod(StuffFactory.serializer(enumType, codeType, value))
                .addMethod(StuffFactory.deserializer(enumType, codeType, enumClazz, creator))
                .build();
    }

}
