package dev.faiths.value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import dev.faiths.Faiths;
import dev.faiths.utils.Pair;

public class ValueMultiBoolean extends AbstractValue<Pair<String, Boolean>[]> {
    private boolean isExpanded = false;

    @SuppressWarnings("unchecked")
    public ValueMultiBoolean(String name, Pair<String, Boolean>... value) {
        super(name, value);
    }

    public void changeValue(final String key, final Boolean newValue) {
        for (final Pair<String, Boolean> pair : getValue()) {
            if (pair.getKey().equals(key)) {
                pair.setValue(newValue);
                if (!Faiths.INSTANCE.isInitializing()) Faiths.configManager.saveConfig(Faiths.configManager.modulesConfig);
                break;
            }
        }
    }

    public void changeValue(final Pair<String, Boolean> newValue) {
        for (final Pair<String, Boolean> pair : getValue()) {
            if (pair.getKey().equals(newValue.getKey())) {
                pair.setValue(newValue.getValue());
                if (!Faiths.INSTANCE.isInitializing()) Faiths.configManager.saveConfig(Faiths.configManager.modulesConfig);
                break;
            }
        }
    }

    public boolean isEnabled(final String name) {
        for (final Pair<String, Boolean> pair : getValue()) {
            if (pair.getKey().equals(name)) {
                return pair.getValue();
            }
        }
        return false;
    }

    public boolean contains(String name) {
        return Arrays.stream(this.getValue()).anyMatch(pair -> name.equalsIgnoreCase(pair.getKey()));
    }


    @Override
    public Map<String, Boolean> toYML() {
        final Map<String, Boolean> data = new HashMap<>();
        Arrays.stream(this.getValue()).forEach(value -> {
            data.put(value.getKey(), value.getValue());
        });
        return data;
    }

    @Override
    public void fromYML(String yaml) {
        final LinkedHashMap<String, Object> data = new Yaml().load(yaml);
        data.entrySet().forEach(entry -> {
            final String[] splited = entry.getKey().split("=");
            this.changeValue(splited[0], Boolean.valueOf(splited[1]));
        });
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

}
