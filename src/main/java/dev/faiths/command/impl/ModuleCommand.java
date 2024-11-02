package dev.faiths.command.impl;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.item.Item;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;

import dev.faiths.command.AbstractCommand;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.Pair;
import dev.faiths.value.AbstractValue;
import dev.faiths.value.ValueBoolean;
import dev.faiths.value.ValueColor;
import dev.faiths.value.ValueFloat;
import dev.faiths.value.ValueInt;
import dev.faiths.value.ValueMode;
import dev.faiths.value.ValueMultiBoolean;

public class ModuleCommand extends AbstractCommand {
    private final CheatModule module;
    private final List<AbstractValue<?>> values;

    public ModuleCommand(CheatModule module, List<AbstractValue<?>> values) {
        super(module.getName().toLowerCase());
        this.module = module;
        this.values = values;
        if (values.isEmpty())
            throw new IllegalArgumentException("Values are empty!");
    }

    @Override
    public void execute(String[] args) {
        String valueNames = values
                .stream()
                .map(value -> value.getName().toLowerCase())
                .reduce((s1, s2) -> s1 + "/" + s2)
                .orElse("");

        String moduleName = module.getName().toLowerCase();

        if (args.length < 2) {
            chatSyntax(values.size() == 1 ? valueNames + " <value>" : moduleName + " <" + valueNames + ">");
            return;
        }

        AbstractValue<?> value = module.getValue(args[1]);

        if (value == null) {
            chatSyntax("<" + valueNames + ">");
            return;
        }

        if (value instanceof ValueBoolean) {
            boolean newValue = !((ValueBoolean) value).getValue();
            ((ValueBoolean) value).setValue(newValue);

            chat("§7" + module.getName() + " §8" + args[1] + "§7 was toggled " + (newValue ? "§8on§7" : "§8off§7")
                    + ".");
            playEdit();
        } else {
            if (args.length < 3) {
                if (value instanceof ValueInt || value instanceof ValueFloat || value instanceof ValueColor)
                    chatSyntax(args[1].toLowerCase() + " <value>");
                else if (value instanceof ValueMode)
                    chatSyntax(args[1].toLowerCase() + " <" + Arrays.stream(((ValueMode) value).getModes())
                            .map(String::toLowerCase).reduce((s1, s2) -> s1 + "/" + s2).orElse("") + ">");
                return;
            }

            try {
                if (value instanceof ValueInt)
                    ((ValueInt) value).setValue(Integer.parseInt(args[2]));
                else if (value instanceof ValueColor)
                    ((ValueColor) value).setValue(new Color(Integer.parseInt(args[2])));
                else if (value instanceof ValueFloat)
                    ((ValueFloat) value).setValue(Float.parseFloat(args[2]));
                else if (value instanceof ValueMultiBoolean) {
                    if (!((ValueMultiBoolean) value).contains(args[2])) {
                        chatSyntax(args[1] + " <" + Arrays.stream(((ValueMultiBoolean) value).getValue())
                                .map(Pair::getKey).reduce((s1, s2) -> s1 + "/" + s2).orElse("") + ">");
                        return;
                    }
                    ((ValueMultiBoolean) value).changeValue(new Pair<>(args[2],
                            args.length < 4 ? !Arrays.stream(((ValueMultiBoolean) value).getValue())
                                    .filter(pair -> pair.getKey().equals(args[2])).findFirst().orElseThrow(null)
                                    .getValue()
                                    : Boolean.parseBoolean(args[3])));
                    chat("§7" + moduleName + " §8" + args[2] + "§7 was set to §8"
                            + Arrays.stream(((ValueMultiBoolean) value).getValue())
                                    .filter(pair -> pair.getKey().equals(args[2])).findFirst().orElseThrow(null)
                                    .getValue()
                            + "§7.");
                    playEdit();
                    return;
                } else if (value instanceof ValueMode) {
                    if (!((ValueMode) value).contains(args[2])) {
                        chatSyntax(args[1].toLowerCase() + " <" + Arrays.stream(((ValueMode) value).getModes())
                                .map(String::toLowerCase).reduce((s1, s2) -> s1 + "/" + s2).orElse("") + ">");
                        return;
                    }

                    ((ValueMode) value).setValue(args[2]);
                }

                chat("§7" + module.getName() + " §8" + args[1] + "§7 was set to §8" + value.getValue() + "§7.");
                playEdit();
            } catch (NumberFormatException e) {
                chat("§8" + args[2] + "§7 cannot be converted to number!");
            }
        }
    }

    @Override
    public List<String> tabComplete(String[] args) {
        if (args.length == 0)
            return new ArrayList<>();

        switch (args.length) {
            case 1:
                return values.stream().map(value -> value.getName().toLowerCase())
                        .filter(valueName -> valueName.startsWith(args[0].toLowerCase())).collect(Collectors.toList());

            case 2:
                AbstractValue<?> valueObj = module.getValue(args[0]);
                if (valueObj instanceof ValueMode) {
                    for (AbstractValue<?> value : values) {
                        if (!value.getName().equalsIgnoreCase(args[0]))
                            continue;
                        if (value instanceof ValueMode)
                            return Arrays.stream(((ValueMode) value).getModes())
                                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                                    .collect(Collectors.toList());
                    }
                    return new ArrayList<>();
                } else if (valueObj instanceof ValueMultiBoolean) {
                    for (AbstractValue<?> value : values) {
                        if (!value.getName().equalsIgnoreCase(args[0]))
                            continue;
                        if (value instanceof ValueMultiBoolean)
                            return Arrays.stream(((ValueMultiBoolean) value).getValue()).map(Pair::getKey)
                                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                                    .collect(Collectors.toList());
                    }
                    return new ArrayList<>();
                } else
                    return new ArrayList<>();
            case 3:
                AbstractValue<?> valueObject = module.getValue(args[0]);
                if (valueObject instanceof ValueMultiBoolean) {
                    for (AbstractValue<?> value : values) {
                        if (!value.getName().equalsIgnoreCase(args[0]))
                            continue;
                        if (value instanceof ValueMultiBoolean)
                            return Lists.newArrayList("true", "false").stream()
                                    .filter(s -> s.toLowerCase().startsWith(args[2].toLowerCase()))
                                    .collect(Collectors.toList());
                    }
                    return new ArrayList<>();
                } else
                    return new ArrayList<>();
            default:
                return new ArrayList<>();
        }
    }
}