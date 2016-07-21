package com.waz.annotions.processing.builders;

import com.waz.annotions.processing.ProcessingException;
import com.waz.annotions.processing.types.AnnotatedClass;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class Builder<T extends AnnotatedClass> {

    protected final List<T> annotatedClasses;
    private boolean hasGenerated;

    public Builder() {
        annotatedClasses = new ArrayList<>();
        hasGenerated = false;
    }

    public void add(T annotatedClass) throws ProcessingException {
        if (annotatedClasses.contains(annotatedClass)) {
            throw new ProcessingException(annotatedClass.getAnnotatedType(),
                                          "Conflict: The class %s is annotated with @%s but %s that already exists",
                                          annotatedClass.getAnnotatedType().getQualifiedName().toString(),
                                          annotatedClass.getSimpleName(),
                                          annotatedClass.getAnnotatedType().getQualifiedName().toString());
        }
        annotatedClasses.add(annotatedClass);
    }

    public abstract void generate(Elements elementUtils, Types typeUtils, Filer filer) throws IOException;

    public boolean hasGenerated() {
        return hasGenerated;
    }

    public void onGenerationFinished() {
        hasGenerated = true;
    }

    public abstract void checkElement(Elements elementUtils, Types typeUtils, Element annotatedElement) throws ProcessingException;
}

