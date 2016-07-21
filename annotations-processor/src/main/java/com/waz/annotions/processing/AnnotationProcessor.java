/**
 * Wire
 * Copyright (C) 2016 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.annotions.processing;

import com.google.auto.service.AutoService;
import com.waz.annotations.Controller;
import com.waz.annotations.Store;
import com.waz.annotions.processing.builders.Builder;
import com.waz.annotions.processing.builders.ControllerFactoryBuilder;
import com.waz.annotions.processing.builders.CoreStubBuilder;
import com.waz.annotions.processing.builders.IControllerFactoryBuilder;
import com.waz.annotions.processing.builders.MockFactoryBuilder;
import com.waz.annotions.processing.types.AnnotatedClass;
import com.waz.annotions.processing.types.ControllerAnnotatedClass;
import com.waz.annotions.processing.types.StoreAnnotatedClass;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;
    private Map<String, List<Builder>> groupedClassesMap;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        groupedClassesMap = new HashMap<>();
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotations = new LinkedHashSet<>();
        annotations.add(Controller.class.getCanonicalName());
        annotations.add(Store.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            processAnnotation(roundEnv, Controller.class);
            processAnnotation(roundEnv, Store.class);
            for (List<Builder> classesList : groupedClassesMap.values()) {
                for (Builder classes : classesList) {
                    if (!classes.hasGenerated()) {
                        classes.generate(elementUtils, typeUtils, filer);
                        classes.onGenerationFinished();
                    }
                }
            }
            return true;
        } catch (ProcessingException e) {
            e.printStackTrace();
            error(e.getElement(), e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            error(null, e.getMessage());
        }
        return true;
    }

    private void processAnnotation(RoundEnvironment roundEnv, Class<? extends Annotation> annotation) throws ProcessingException {
        if (groupedClassesMap.containsKey(annotation.getName())) {
            return;
        }
        final List<Builder> groupedClassesList = getAnnotatedGroupedClassesList(annotation);
        for (Builder groupedClasses : groupedClassesList) {
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotation)) {
                groupedClasses.checkElement(elementUtils, typeUtils, annotatedElement);
                groupedClasses.add(getAnnotatedClass(annotation, (TypeElement) annotatedElement));
            }
        }
        groupedClassesMap.put(annotation.getName(), groupedClassesList);

    }

    private AnnotatedClass getAnnotatedClass(Class<? extends Annotation> annotation, TypeElement typeElement) throws ProcessingException {
        AnnotatedClass annotatedClass;
        if (annotation.equals(Controller.class)) {
            annotatedClass = new ControllerAnnotatedClass(typeElement);
        } else if (annotation.equals(Store.class)) {
            annotatedClass = new StoreAnnotatedClass(typeElement);
        } else {
            throw new ProcessingException(null, "Annotation @%s not found", annotation.getSimpleName());
        }
        return annotatedClass;
    }

    private List<Builder> getAnnotatedGroupedClassesList(Class<? extends Annotation> annotation) throws ProcessingException {
        List<Builder> groupedClassesList = new ArrayList<>();
        if (annotation.equals(Controller.class)) {
            groupedClassesList.add(new ControllerFactoryBuilder());
            groupedClassesList.add(new IControllerFactoryBuilder());
            groupedClassesList.add(new MockFactoryBuilder<ControllerAnnotatedClass>());
            groupedClassesList.add(new CoreStubBuilder());
        } else if (annotation.equals(Store.class)) {
            groupedClassesList.add(new CoreStubBuilder());
            groupedClassesList.add(new MockFactoryBuilder<StoreAnnotatedClass>());
        } else {
            throw new ProcessingException(null, "Annotation @%s not found", annotation.getSimpleName());
        }
        return groupedClassesList;
    }

    private void error(Element e, String msg) {
        messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }
}
