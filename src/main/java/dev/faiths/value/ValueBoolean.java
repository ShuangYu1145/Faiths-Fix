package dev.faiths.value;

import org.yaml.snakeyaml.Yaml;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class ValueBoolean extends AbstractValue<Boolean> {

    public ValueBoolean(String name, Boolean value) {
        super(name, value);
    }

    @Override
    public Boolean toYML() {
        return getValue();
    }

    @Override
    public void fromYML(final String yaml) {
        Boolean value = new Yaml().load(yaml);
        this.setValue(value);
    }

}
