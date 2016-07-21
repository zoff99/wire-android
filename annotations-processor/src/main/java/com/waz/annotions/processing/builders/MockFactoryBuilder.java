package com.waz.annotions.processing.builders;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.waz.annotions.processing.types.CoreAnnotatedClass;

public class MockFactoryBuilder<T extends CoreAnnotatedClass> extends AbsFactoryBuilder<T> {

    protected static final String PREFIX_STUB = "Stub";
    protected static final String PREFIX_MOCK = "Mock";

    protected static final String PACKAGE_CONTROLLER_STUB = PACKAGE_CONTROLLER + ".stub";
    protected static final String PACKAGE_STORE_STUB = PACKAGE_STORE + ".stub";

    protected static final String PACKAGE_MOCK = "com.waz.zclient.mock";
    protected static final String MOCK_LOC = "app/build/generated/source/apt/androidTest/dev/debug/";

    protected String stubFactoryName;
    protected String mockFactoryName;

    @Override
    protected void extraConfig() {
        stubFactoryName = PREFIX_STUB + factoryName;
        mockFactoryName = PREFIX_MOCK + factoryName;
    }

    @Override
    protected String getSaveLocation() {
        return MOCK_LOC;
    }

    @Override
    protected String getClassName() {
        return mockFactoryName;
    }

    @Override
    protected String getPackageName() {
        return PACKAGE_MOCK;
    }

    @Override
    protected JavaFile modifyJavaFile(JavaFile.Builder builder) {
        return builder.addStaticImport(ClassName.get("org.mockito", "Mockito"), "spy")
                      .build();
    }

    @Override
    protected FieldSpec modifyField(FieldSpec.Builder builder, T annotatedClass) {
        return builder.initializer("spy($T.class)", ClassName.get(getStubPackage(), PREFIX_STUB + annotatedClass.getInstanceNameSuffix())).build();
    }

    private String getStubPackage() {
        return isControllerFactory() ? PACKAGE_CONTROLLER_STUB : PACKAGE_STORE_STUB;
    }
}
