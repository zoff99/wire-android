package com.waz.annotions.processing.builders;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.waz.annotions.processing.types.ControllerAnnotatedClass;

import javax.lang.model.element.Modifier;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ControllerFactoryBuilder extends AbsFactoryBuilder<ControllerAnnotatedClass> {

    private static final String BASE_PREFIX = "Base$$";

    @Override
    protected TypeSpec modifyFactorySpec(TypeSpec.Builder builder) {
        return builder.addModifiers(Modifier.ABSTRACT).build();
    }

    @Override
    protected String getClassName() {
        return BASE_PREFIX + FACTORY_NAME_CONTROLLER;
    }

    @Override
    protected String getPackageName() {
        return PACKAGE_CONTROLLER;
    }

    @Override
    protected void addExtraFields(List<FieldSpec> fields) {
        fields.add(FieldSpec.builder(TypeName.BOOLEAN,
                                     "isTornDown",
                                     Modifier.PROTECTED)
                            .build());
        fields.add(FieldSpec.builder(ClassName.get("android.content", "Context"),
                                     "context",
                                     Modifier.PROTECTED).build());
    }

    @Override
    protected void addExtraMethods(Set<MethodSpec> methodSpecs) {
        methodSpecs.add(MethodSpec.constructorBuilder()
                                  .addModifiers(Modifier.PUBLIC)
                                  .addParameter(ClassName.get("android.content", "Context"), "context")
                                  .addCode(CodeBlock.builder()
                                                    .addStatement("$N = $N", "this.context", "context")
                                                    .addStatement("$N = false", "this.isTornDown")
                                                    .build())
                                  .build());

        methodSpecs.add(MethodSpec.methodBuilder("verifyLifecycle")
                                  .addModifiers(Modifier.PROTECTED, Modifier.FINAL)
                                  .returns(TypeName.VOID)
                                  .addCode(CodeBlock.builder()
                                                    .beginControlFlow("if ($N)", "isTornDown")
                                                    .addStatement("throw new $T($S)",
                                                                  IllegalStateException.class,
                                                                  "ControllerFactory is already torn down")
                                                    .endControlFlow()
                                                    .build())
                                  .build());

        generateAbstractInits(methodSpecs);
    }

    private void generateAbstractInits(Set<MethodSpec> methodSpecs) {
        for (ControllerAnnotatedClass annotatedClass : annotatedClasses) {
            if (annotatedClass.isCustomInit()) {
                methodSpecs.add(MethodSpec.methodBuilder(annotatedClass.getInitMethod())
                                          .addModifiers(Modifier.PROTECTED, Modifier.ABSTRACT)
                                          .returns(TypeName.VOID)
                                          .build());
            }
        }
    }

    @Override
    protected MethodSpec modifyStoreOrControllerGetter(MethodSpec.Builder builder,
                                                       ControllerAnnotatedClass annotatedClass) {
        final CodeBlock.Builder controlFlow = CodeBlock.builder()
                                                       .addStatement("$N()", "verifyLifecycle");

        if (annotatedClass.isCustomInit()) {
            controlFlow.addStatement("$N()", annotatedClass.getInitMethod());
        } else {
            final List<TypeName> typeParameters = annotatedClass.getConstructorParams(elementUtils);
            controlFlow.beginControlFlow("if ($N == null)", annotatedClass.getFieldName())
                       .add("$[$N = new $T(",
                            annotatedClass.getFieldName(),
                            annotatedClass.getImplementationClass(elementUtils));
            final Iterator<TypeName> iterator = typeParameters.iterator();
            while (iterator.hasNext()) {
                final TypeName arg = iterator.next();
                if ("android.content.Context".equals(arg.toString())) {
                    controlFlow.add("$N", "this.context");
                } else {
                    controlFlow.add("$N",
                                    String.format("get%s()",
                                                  arg.toString().substring(arg.toString().lastIndexOf(".") + 2)));
                }

                if (iterator.hasNext()) {
                    controlFlow.add(", ");
                }
            }
            controlFlow.add(");\n$]");
            controlFlow.endControlFlow();
        }
        controlFlow.addStatement("return $N", annotatedClass.getFieldName());

        return builder.addCode(controlFlow.build()).build();
    }

    @Override
    protected MethodSpec modifySetActivity(MethodSpec.Builder builder) {
        final CodeBlock.Builder coderBuilder = CodeBlock.builder();
        for (ControllerAnnotatedClass annotatedClass : annotatedClasses) {
            if (annotatedClass.isRequiresActivity()) {
                coderBuilder.addStatement("$N().setActivity($N)", annotatedClass.getGetterName(), "activity");
            }
        }
        return builder.addCode(coderBuilder.build()).build();
    }

    @Override
    protected MethodSpec modifySetGlobalLayout(MethodSpec.Builder builder) {
        final CodeBlock.Builder codeBuilder = CodeBlock.builder();
        for (ControllerAnnotatedClass annotatedClass : annotatedClasses) {
            if (annotatedClass.isRequiresGlobalLayoutView()) {
                codeBuilder.addStatement("$N().setGlobalLayout($N)",
                                         annotatedClass.getGetterName(),
                                         "globalLayoutView");
            }
        }
        return builder.addCode(codeBuilder.build()).build();
    }

    @Override
    protected MethodSpec modifyTearDown(MethodSpec.Builder builder) {
        final CodeBlock.Builder codeBuilder = CodeBlock.builder();
        codeBuilder.addStatement("$N = true", "this.isTornDown");
        for (ControllerAnnotatedClass annotatedClass : annotatedClasses) {
            codeBuilder.beginControlFlow("if ($N != null)", annotatedClass.getFieldName())
                       .addStatement("$N.tearDown()", annotatedClass.getFieldName())
                       .addStatement("$N = null", annotatedClass.getFieldName())
                       .endControlFlow();
        }
        codeBuilder.addStatement("$N = null", "this.context");
        return builder.addCode(codeBuilder.build()).build();
    }

    @Override
    protected MethodSpec modifyIsTornDown(MethodSpec.Builder builder) {
        return builder.addCode(CodeBlock.builder()
                                 .addStatement("return $N", "isTornDown")
                                 .build()).build();
    }
}

