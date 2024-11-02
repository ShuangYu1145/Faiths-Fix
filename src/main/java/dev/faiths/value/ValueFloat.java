package dev.faiths.value;

import org.yaml.snakeyaml.Yaml;

import java.util.HashMap;
import java.util.Map;

public class ValueFloat extends AbstractValue<Float> {
    private final Float minimum;
    private final Float maximum;
    public ValueFloat(String name, Float value, Float minimum, Float maximum) {
        super(name, value);
        this.minimum = minimum;
        this.maximum = maximum;
    }

    public Float getMinimum() {
        return minimum;
    }

    public Float getMaximum() {
        return maximum;
    }

    @Override
    public String toYML() {
        return this.getValue() + "F";
    }

    @Override
    public void fromYML(String yaml) {
        final Float value = Float.valueOf(new Yaml().load(yaml));
        this.setValue(value);
    }
}
