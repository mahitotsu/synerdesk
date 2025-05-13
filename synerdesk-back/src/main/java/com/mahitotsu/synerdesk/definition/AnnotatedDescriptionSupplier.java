package com.mahitotsu.synerdesk.definition;

import java.lang.reflect.Method;

import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;

import com.mahitotsu.synerdesk.definition.DefaultReturnControlToolDefinition.DescriptionSupplier;

public class AnnotatedDescriptionSupplier implements DescriptionSupplier {

    @Override
    public String describeActionGroup(final Class<?> toolApli) {
        return this.getDescription(AnnotationUtils.getAnnotation(toolApli, Description.class));
    }

    @Override
    public String describeFunction(final Method method) {
        return this.getDescription(AnnotationUtils.getAnnotation(method, Description.class));
    }

    @Override
    public String describeParameter(final MethodParameter parameter) {
        return this.getDescription(parameter.getParameterAnnotation(Description.class));
    }

    private String getDescription(final Description description) {
        return description == null ? "" : description.value();
    }
}
