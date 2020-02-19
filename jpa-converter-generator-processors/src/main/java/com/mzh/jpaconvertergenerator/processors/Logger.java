package com.mzh.jpaconvertergenerator.processors;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Logger wrapper processor messager.
 *
 * @author ChenLingshu
 */
public class Logger {

    private final Messager messager;

    public Logger(Messager messager) {
        this.messager = messager;
    }

    public void warn(String message) {
        this.getMessager().printMessage(Diagnostic.Kind.WARNING, message);
    }

    public void error(String message, Element element) {
        this.getMessager().printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    public void info(String message) {
        this.getMessager().printMessage(Diagnostic.Kind.NOTE, message);
    }

    private Messager getMessager() {
        return this.messager;
    }

}
