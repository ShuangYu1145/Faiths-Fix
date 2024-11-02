package dev.faiths.value;

import dev.faiths.Faiths;
import dev.faiths.utils.Valider;

public abstract class AbstractValue<T> {
    private final String name;
    private T value;
    private Valider visible;

    public AbstractValue(final String name, final T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public <T extends AbstractValue<?>> T visible(final Valider visible) {
        this.visible = visible;
        return (T) this;
    }

    public boolean isVisible() {
        return this.visible == null || this.visible.validate();
    }

    public void setValue(T value) {
        this.value = value;
        if (!Faiths.INSTANCE.isInitializing()) Faiths.configManager.saveConfig(Faiths.configManager.modulesConfig);
    }

    public abstract Object toYML();
    public abstract void fromYML(final String yaml);
}
