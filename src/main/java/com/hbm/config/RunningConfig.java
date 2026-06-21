package com.hbm.config;

import java.io.File;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Minimal legacy RunningConfig facade. The old hbm*.json writer is intentionally
 * not restored; modern Forge config owns persisted values.
 */
@Deprecated(forRemoval = false)
public class RunningConfig {
    public static File getConfig(String name) {
        return new File("config", name);
    }

    public static void readConfig(File config, HashMap<String, ConfigWrapper> configMap) {
    }

    public static void writeConfig(File config, HashMap<String, ConfigWrapper> configMap, String info) {
    }

    public static class ConfigWrapper<T> {
        public T value;
        private final Supplier<T> getter;
        private final Consumer<T> setter;

        public ConfigWrapper(T value) {
            this(value, null, null);
        }

        public ConfigWrapper(T value, Supplier<T> getter) {
            this(value, getter, null);
        }

        public ConfigWrapper(T value, Supplier<T> getter, Consumer<T> setter) {
            this.value = value;
            this.getter = getter;
            this.setter = setter;
        }

        public T get() {
            if (getter != null) {
                try {
                    value = getter.get();
                } catch (IllegalStateException ignored) {
                    // Forge config may not be loaded yet while legacy facade classes initialize.
                }
            }
            return value;
        }

        public void set(T value) {
            this.value = value;
            if (setter != null) {
                setter.accept(value);
            }
        }

        @SuppressWarnings("unchecked")
        public void update(String param) {
            Object parsed = null;
            Object current = value;
            if (current instanceof String) parsed = param;
            if (current instanceof Float) parsed = Float.parseFloat(param);
            if (current instanceof Double) parsed = Double.parseDouble(param);
            if (current instanceof Integer) parsed = Integer.parseInt(param);
            if (current instanceof Boolean) parsed = Boolean.parseBoolean(param);
            if (parsed != null) {
                set((T) parsed);
            }
        }
    }
}
