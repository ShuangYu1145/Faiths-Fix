package dev.faiths.value;

import java.awt.Color;

import dev.faiths.utils.Valider;
import org.yaml.snakeyaml.Yaml;
public class ValueColor extends AbstractValue<Color> {
    private boolean isExpanded = false;
    private Valider visible;
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

    public <T extends AbstractValue<?>> T visible(final Valider visible) {
        this.visible = visible;
        return (T) this;
    }

    public boolean isVisible() {
        return this.visible == null || this.visible.validate();
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }
}
