package dev.faiths.value;

import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ValueMode extends AbstractValue<String> {
    private final String[] modes;
    public ValueMode(String name, String[] modes, String value) {
        super(name, value);
        this.modes = modes;
    }

    @Override
    public String toYML() {
        return this.getValue();
    }

    @Override
    public void fromYML(final String yaml) {
        final Object loadedObject = new Yaml().load(yaml);
        final String value = new StringBuilder().append(loadedObject).toString();
        this.setValue(value);
    }

    public boolean contains(String name) {
        return Arrays.stream(this.getModes()).anyMatch(mode -> name.equalsIgnoreCase(mode));
    }

    public boolean is(String sb) {
        return this.getValue().equalsIgnoreCase(sb);
    }

    public String[] getModes() {
        return modes;
    }
}
