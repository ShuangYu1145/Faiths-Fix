package dev.faiths.module;

import dev.faiths.Faiths;
import dev.faiths.event.Listener;
import dev.faiths.module.combat.ModuleGapple;
import dev.faiths.module.client.ModuleCNname;
import dev.faiths.ui.notifiction.NotificationType;
import dev.faiths.value.AbstractValue;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public abstract class CheatModule implements Listener {
    private String name;
    protected Category category;
    private String CNname;
    private int keyBind;
    private boolean state;
    private boolean isHidden = false;
    private final List<AbstractValue<?>> values = new ArrayList<>();
    private boolean valuesAdded = false;
    private boolean isExpanded = false;
    public float slide = 0F;
    public float slideStep = 0F;
    public float height = 0F;

    public CheatModule(final String name, final Category category, final String CNname) {
        this(name, category, Keyboard.KEY_NONE , CNname);
    }

    public CheatModule(final String name, final Category category, final int keyBind,final String CNname) {
        this.name = name;
        this.category = category;
        this.keyBind = keyBind;
        this.CNname = CNname;
    }

    public int getKeyBind() {
        return keyBind;
    }

    public void setKeyBind(int keyBind) {
        this.keyBind = keyBind;
    }

    public String getName() {
        return name;
    }

    public String getCNName() {
        String name1 = getName();
        if (Faiths.moduleManager.getModule(ModuleCNname.class).getState())
        {
            name1 = CNname;
        }
        return name1;
    }

    public String getSuffix() {
        return "";
    }

    public boolean suffixIsNotEmpty() {
        return !getSuffix().isEmpty();
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }

    public boolean isExpanded() {
        return isExpanded;
    }

    public void setExpanded(boolean isExpanded) {
        this.isExpanded = isExpanded;
    }

    public boolean getState() {
        return state;
    }

    public Category getCategory() {
        return category;
    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public void setState(boolean state) {
        if (this.state == state) return;
        this.state = state;
        if (!Faiths.INSTANCE.isInitializing()) {
            if (state) {
                onEnable();

                if (Faiths.moduleManager.getModule(ModuleCNname.class).getState())
                {
                    Faiths.notificationManager.pop("模块",  "开启 " + getCNName() + ".", 1000, NotificationType.SUCCESS);
                } else {
                    Faiths.notificationManager.pop("Enabled",  "Enabled " + getName() + ".", 1000, NotificationType.SUCCESS);
                }

            } else {
                onDisable();

                if (Faiths.moduleManager.getModule(ModuleCNname.class).getState())
                {
                    Faiths.notificationManager.pop("模块", "关闭 " + getCNName() + ".", 1000, NotificationType.ERROR);
                } else {
                    Faiths.notificationManager.pop("Disabled", "Disabled " + getName() + ".", 1000, NotificationType.ERROR);
          }

            }
            mc.thePlayer.playSound("random.click",0.5F,1F);
        }
    }

    public void toggle() {
        setState(!getState());
        if (!Faiths.INSTANCE.isInitializing())
            Faiths.configManager.saveConfig(Faiths.configManager.modulesConfig);
    }

    public List<AbstractValue<?>> getValues() {
        if (!valuesAdded) {
            Arrays.stream(this.getClass().getDeclaredFields()).forEach(field -> {
                field.setAccessible(true);
                try {
                    final Object object = field.get(this);
                    if (object instanceof AbstractValue) {
                        values.add((AbstractValue<?>) object);
                    }
                } catch (final Exception ignored) {

                }
            });
            valuesAdded = true;
        }

        return values;
    }

    public AbstractValue<?> getValue(final String valueName) {
        return this.values.stream().filter(value -> value.getName().toLowerCase().equals(valueName.toLowerCase())).findFirst().orElse(null);
    }

    @Override
    public boolean isAccessible() {
        return state;
    }

    public boolean isGapple() {
        return Faiths.moduleManager.getModule(ModuleGapple.class).getState();
    }
}