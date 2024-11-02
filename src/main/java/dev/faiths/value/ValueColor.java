package dev.faiths.value;

import java.awt.Color;

import org.yaml.snakeyaml.Yaml;
public class ValueColor extends AbstractValue<Color> {
    private boolean isExpanded = false;
    public ValueColor(final String name, final Color value) {
        super(name, value);
    }

    @Override
    public Integer toYML() {
        return this.getValue().getRGB();
    }

    @Override
    public void fromYML(String yaml) {
        final Integer value = new Yaml().load(yaml);
        this.setValue(new Color(value));
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }
}
