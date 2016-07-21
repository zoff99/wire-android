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
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * For the sake of just the annotation processing, Core means Co(ntroller OR Sto)re. Also because Controllers and
 * Stores make up the Core of our app :)
 */
public abstract class CoreAnnotatedClass extends AnnotatedClass {

    private final String instanceNameSuffix;

    private String getterName;
    private String fieldName;
    private String initName;

    public CoreAnnotatedClass(TypeElement annotatedType) {
        super(annotatedType);
        instanceNameSuffix = removeIFromControllerInterfaceName(simpleClassName);
        createInstanceNames();
    }

    private String removeIFromControllerInterfaceName(String fullInterfaceName) {
        return fullInterfaceName.substring(1);
    }

    private void createInstanceNames() {
        getterName = String.format("get%s", getInstanceNameSuffix());
        initName = String.format("init%s", getInstanceNameSuffix());
        fieldName = getInstanceNameSuffix().substring(0, 1).toLowerCase() + getInstanceNameSuffix().substring(1);
    }

    public String getGetterName() {
        return getterName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getInitMethod() {
        return initName;
    }

    /**
     * @return the suffix that all concrete instances of our controllers take. I.e., it returns the XyzController part
     * from IXyzController
     */
    public String getInstanceNameSuffix() {
        return instanceNameSuffix;
    }

    /**
     * Gets all methods that the annotated interface contains or inherits, but only checks for first level extension
     * interfaces. I.e., if this class annotates the INavigationController interface, which also extends the
     * ViewPager.OnPageChangeListener interface, then we this doesn't currently check if ViewPager.OnPageChangeListener
     * further extends any other interfaces.
     * @return all methods in this interface and any extended interfaces
     */
    public Set<MethodSpec> getAllMethods() {
        Set<MethodSpec> methods = new HashSet<>();
        for (Element element : annotatedType.getEnclosedElements()) {
            if (element instanceof Symbol.MethodSymbol) {
                methods.add(buildMethodSpec((Symbol.MethodSymbol) element));
            }
        }

        //This does not check beyond first level inheritance, although that's currently not necessary.
        for (TypeMirror interf : annotatedType.getInterfaces()) {
            if (interf instanceof Type.ClassType) {
                Type.ClassType interfClass = (Type.ClassType) interf;
                for (Element element : interfClass.asElement().getEnclosedElements()) {
                    if (element instanceof Symbol.MethodSymbol) {
                        methods.add(buildMethodSpec((Symbol.MethodSymbol) element));
                    }
                }
            }
        }
        return methods;
    }

    private MethodSpec buildMethodSpec(Symbol.MethodSymbol element) {
        List<ParameterSpec> parameterSpecs = new ArrayList<>();
        for (Symbol.VarSymbol varSymbol : element.getParameters()) {
            parameterSpecs.add(buildParameterSpec(varSymbol));
        }

        TypeName returnType = ClassName.get((element).getReturnType());

        return MethodSpec.methodBuilder(element.getSimpleName().toString())
                         .addAnnotation(Override.class)
                         .addModifiers(Modifier.PUBLIC)
                         .addParameters(parameterSpecs)
                         .addStatement(buildStatementString(returnType), buildStatementArgs(returnType))
                         .returns(returnType)
                         .build();
    }

    private ParameterSpec buildParameterSpec(Symbol.VarSymbol varSymbol) {
        return ParameterSpec.builder(ClassName.get(varSymbol.asType()),
                                     varSymbol.toString(),
                                     new Modifier[] {})
                            .build();
    }

    // CHECKSTYLE:OFF
    private String buildStatementString(TypeName returnType) {
        if (returnType == TypeName.VOID) return "";
        if (!returnType.isPrimitive()) return "return null";
        return "return $L";
    }

    private Object[] buildStatementArgs(TypeName returnType) {
        Object arg = null;
        if (returnType == TypeName.VOID || !returnType.isPrimitive()) return new Object[] {};
        if (returnType == TypeName.BOOLEAN) arg = false;
        if (returnType == TypeName.BYTE) arg = 0;
        if (returnType == TypeName.SHORT) arg = 0;
        if (returnType == TypeName.INT) arg = 0;
        if (returnType == TypeName.LONG) arg = 0;
        if (returnType == TypeName.CHAR) arg = '\u0000';
        if (returnType == TypeName.FLOAT) arg = 0.0f;
        if (returnType == TypeName.DOUBLE) arg = 0.0d;
        return new Object[] {arg};
    }
    // CHECKSTYLE:ON

}
