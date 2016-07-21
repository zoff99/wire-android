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
package com.waz.annotions.processing.types;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Symbol;
import com.waz.annotations.Controller;
import com.waz.annotions.processing.ProcessingException;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;

public class ControllerAnnotatedClass extends CoreAnnotatedClass {
    private boolean requiresActivity;
    private boolean requiresGlobalLayoutView;
    private boolean customInit;

    public ControllerAnnotatedClass(TypeElement annotatedElement) throws ProcessingException {
        super(annotatedElement);
        checkValidClass(annotatedElement);
        findExtraAnnotationConfigs();
    }

    private void checkValidClass(TypeElement item) throws ProcessingException {
        if (!item.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ProcessingException(item, "The class %s is not public.",
                                          item.getQualifiedName().toString());
        }
    }

    private void findExtraAnnotationConfigs() throws ProcessingException {
        final Controller annotation = annotatedType.getAnnotation(Controller.class);
        if (annotation == null) {
            throw new ProcessingException(annotatedType, "Something weird happened!");
        }
        requiresActivity = annotation.requiresActivity();
        requiresGlobalLayoutView = annotation.requiresGlobalLayoutView();
        customInit = annotation.customInit();
    }

    public boolean isRequiresActivity() {
        return requiresActivity;
    }

    public boolean isRequiresGlobalLayoutView() {
        return requiresGlobalLayoutView;
    }

    public boolean isCustomInit() {
        return customInit;
    }

    public String getSimpleName() {
        return simpleClassName;
    }

    public TypeName getImplementationClass(Elements elementUtils) {
        return ClassName.get(elementUtils.getPackageOf(annotatedType).getQualifiedName().toString(), getInstanceNameSuffix());
    }

    public List<TypeName> getConstructorParams(Elements elementUtils) {
        final String implementationClassName = getImplementationClass(elementUtils).toString();
        final TypeElement implementationElement = elementUtils.getTypeElement(implementationClassName);
        final List<? extends Element> allMembers = elementUtils.getAllMembers(implementationElement);

        final List<TypeName> parameters = new ArrayList<>();
        for (Element element : allMembers) {
            if (element.getKind() == ElementKind.CONSTRUCTOR) {
                for (Symbol.VarSymbol param : ((Symbol.MethodSymbol) element).getParameters()) {
                    parameters.add(ClassName.get(param.asType()));
                }
                break;
            }
        }

        return parameters;
    }
}
