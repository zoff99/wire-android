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

import javax.lang.model.element.TypeElement;

public abstract class AnnotatedClass {

    protected String fullClassName;
    protected TypeElement annotatedType;
    protected String simpleClassName;

    /**
     * Created a an object representing general information about a type (class or interface) that has been
     * given an annotation.
     * @param annotatedType a TypeElement representing the type with the annotation.
     */
    public AnnotatedClass(TypeElement annotatedType) {
        this.annotatedType = annotatedType;
        simpleClassName = annotatedType.getSimpleName().toString();
        fullClassName = annotatedType.getQualifiedName().toString();
    }

    /**
     * @return The simple class name of the type that has the annotation
     */
    public String getSimpleName() {
        return simpleClassName;
    }

    /**
     * @return The TypeElement representing the type with the annotation
     */
    public TypeElement getAnnotatedType() {
        return annotatedType;
    }

    /**
     * @return the fully qualified name of the type with the annotation
     */
    public String getFullClassName() {
        return fullClassName;
    }
}
