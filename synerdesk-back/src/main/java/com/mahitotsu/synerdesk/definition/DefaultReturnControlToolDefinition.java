package com.mahitotsu.synerdesk.definition;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.services.bedrockagentruntime.model.ActionGroupExecutor;
import software.amazon.awssdk.services.bedrockagentruntime.model.AgentActionGroup;
import software.amazon.awssdk.services.bedrockagentruntime.model.CustomControlMethod;
import software.amazon.awssdk.services.bedrockagentruntime.model.FunctionDefinition;
import software.amazon.awssdk.services.bedrockagentruntime.model.FunctionInvocationInput;
import software.amazon.awssdk.services.bedrockagentruntime.model.FunctionSchema;
import software.amazon.awssdk.services.bedrockagentruntime.model.ParameterDetail;
import software.amazon.awssdk.services.bedrockagentruntime.model.ParameterType;

public abstract class DefaultReturnControlToolDefinition<T> implements ReturnControlToolDefinition<T> {

    public static interface DescriptionSupplier {

        String describeActionGroup(Class<?> toolApli);

        String describeFunction(Method method);

        String describeParameter(MethodParameter parameter);
    }

    protected DefaultReturnControlToolDefinition(@NonNull final Class<T> toolApi) {
        this.toolApi = toolApi;
    }

    protected DefaultReturnControlToolDefinition(@NonNull final Class<T> toolApi, final String actionGroupName) {
        this.toolApi = toolApi;
        this.actionGroupName = actionGroupName;
    }

    private Class<T> toolApi;

    @Autowired
    private ConversionService converter;

    @Autowired
    private DescriptionSupplier descriptionSupplier;

    private String actionGroupName;

    private AgentActionGroup agentActionGroup;

    private final Map<String, Method> methodMap = new HashMap<>();

    private final Map<Method, Map<String, MethodParameter>> parameterListMap = new HashMap<>();

    private ParameterType getParameterType(final Class<?> argType) {

        if (argType == int.class || argType == long.class || argType == short.class
                || BigInteger.class.equals(argType) || Integer.class.equals(argType)
                || Long.class.isAssignableFrom(argType) || Short.class.equals(argType)) {
            return ParameterType.INTEGER;
        } else if (argType == double.class || argType == float.class || BigDecimal.class.equals(argType)
                || Double.class.equals(argType) || Short.class.equals(argType)) {
            return ParameterType.NUMBER;
        } else if (argType == boolean.class || Boolean.class.equals(argType)) {
            return ParameterType.BOOLEAN;
        } else if (argType.isArray() && this.converter.canConvert(String.class, argType.getComponentType())) {
            return ParameterType.ARRAY;
        } else if (String.class.equals(argType) || this.converter.canConvert(String.class, argType)) {
            return ParameterType.STRING;
        } else {
            throw new IllegalArgumentException(
                    "The specified parameter type is not supported. type: " + argType.getCanonicalName());
        }
    }

    @PostConstruct
    public void afterPropertiesSet() {

        if (this.toolApi.isInterface() == false) {
            throw new IllegalArgumentException("The toolAPi must be an interface.");
        }

        if (this.actionGroupName == null) {
            this.actionGroupName = this.getClass().getSimpleName();
        }

        final Collection<FunctionDefinition> functions = Arrays.stream(this.toolApi.getMethods()).map(m -> {
            final int numOfArgs = m.getParameterCount();
            final Map<String, MethodParameter> methodParameters = new HashMap<>();
            final Map<String, ParameterDetail> parameterMap = new HashMap<>();
            for (int i = 0; i < numOfArgs; i++) {
                final MethodParameter mp = new MethodParameter(m, i);
                final ParameterDetail pd = ParameterDetail.builder()
                        .description(this.descriptionSupplier.describeParameter(mp))
                        .required(mp.hasParameterAnnotation(Nullable.class) == false)
                        .type(this.getParameterType(mp.getParameterType()))
                        .build();
                final String parameterName = mp.getParameterName() != null ? mp.getParameterName()
                        : "arg" + String.format("%02d", i);
                parameterMap.put(parameterName, pd);
                methodParameters.put(parameterName, mp);
            }
            final FunctionDefinition fd = FunctionDefinition.builder()
                    .name(m.getName())
                    .description(this.descriptionSupplier.describeFunction(m))
                    .parameters(parameterMap)
                    .build();
            this.methodMap.put(fd.name(), m);
            this.parameterListMap.put(m, methodParameters);
            return fd;
        }).toList();

        this.agentActionGroup = AgentActionGroup.builder()
                .actionGroupName(this.actionGroupName)
                .description(this.descriptionSupplier.describeActionGroup(this.toolApi))
                .actionGroupExecutor(ActionGroupExecutor.fromCustomControl(CustomControlMethod.RETURN_CONTROL))
                .functionSchema(FunctionSchema.builder().functions(functions).build())
                .build();
    }

    @Override
    public AgentActionGroup getAgentActionGroup() {
        return this.agentActionGroup;
    }

    @Override
    public Object invokeFunction(final ToolInstanceLookup toolInstanceLookup,
            final FunctionInvocationInput functionInvocationInput) throws Exception {

        final String actionGroup = functionInvocationInput.actionGroup();
        final String functionName = functionInvocationInput.function();
        final Map<String, String> parameterMap = functionInvocationInput.parameters().stream()
                .collect(Collectors.toMap(p -> p.name(), p -> p.value()));

        final T instance = toolInstanceLookup.getToolInstance(actionGroup, this.toolApi);
        final Method method = this.getMethod(functionName);
        final Object[] args = this.getArguments(method, parameterMap);

        return method.invoke(instance, args);
    }

    private Method getMethod(final String functionName) {
        return this.methodMap.get(functionName);
    }

    private Object[] getArguments(final Method method, final Map<String, String> parameterMap) {

        final Map<String, MethodParameter> mpMap = this.parameterListMap.get(method);
        if (mpMap == null) {
            return new Object[] {};
        }

        final List<Object> argList = new ArrayList<>(mpMap.size());
        mpMap.entrySet().stream().forEach(entry -> {
            final Object value = this.converter.convert(parameterMap.get(entry.getKey()),
                    entry.getValue().getParameterType());
            argList.add(value);
        });

        return argList.toArray();
    }
}
