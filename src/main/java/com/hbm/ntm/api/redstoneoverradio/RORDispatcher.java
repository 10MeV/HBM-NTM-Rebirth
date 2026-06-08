package com.hbm.ntm.api.redstoneoverradio;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public final class RORDispatcher {
    private final List<String> info;
    private final Map<String, Supplier<String>> values;
    private final Map<String, RORFunction> functions;

    private RORDispatcher(List<String> info, Map<String, Supplier<String>> values, Map<String, RORFunction> functions) {
        this.info = List.copyOf(info);
        this.values = Map.copyOf(values);
        this.functions = Map.copyOf(functions);
    }

    public static Builder builder() {
        return new Builder();
    }

    public String[] getFunctionInfo() {
        return info.toArray(String[]::new);
    }

    public String provideValue(String name) {
        Supplier<String> supplier = values.get(name);
        return supplier == null ? null : supplier.get();
    }

    public String runFunction(String name, String[] params) {
        RORFunction function = functions.get(name);
        return function == null ? null : function.run(params == null ? new String[0] : params);
    }

    @FunctionalInterface
    public interface RORFunction {
        String run(String[] params);
    }

    public static final class Builder {
        private final List<String> info = new ArrayList<>();
        private final Map<String, Supplier<String>> values = new LinkedHashMap<>();
        private final Map<String, RORFunction> functions = new LinkedHashMap<>();

        public Builder value(String name, Supplier<String> supplier) {
            String fullName = ROR.value(name);
            info.add(fullName);
            values.put(fullName, supplier);
            return this;
        }

        public Builder function(String name, RORFunction function, String... parameterInfo) {
            String fullName = ROR.function(name);
            functions.put(fullName, function);
            if (parameterInfo == null || parameterInfo.length == 0) {
                info.add(fullName);
            } else {
                for (String params : parameterInfo) {
                    info.add(ROR.functionInfo(name, params));
                }
            }
            return this;
        }

        public RORDispatcher build() {
            return new RORDispatcher(info, values, functions);
        }
    }
}
