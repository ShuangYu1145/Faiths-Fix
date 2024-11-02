package dev.faiths.command.impl;

import dev.faiths.Faiths;
import dev.faiths.command.AbstractCommand;
import dev.faiths.module.CheatModule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ToggleCommand extends AbstractCommand {
    
    public ToggleCommand() {
        super("toggle", "t");
    }

    /**
     * Execute commands with provided [args]
     */
    @Override
    public void execute(final String[] args) {
        if (args.length > 1) {
            final CheatModule module = Faiths.moduleManager.getModule(args[1]);

            if (module == null) {
                chat("Module '" + args[1] + "' not found.");
                return;
            }

            if (args.length > 2) {
                String newState = args[2].toLowerCase();

                if (newState.equals("on") || newState.equals("off")) {
                    module.setState(newState.equals("on"));
                } else {
                    chatSyntax("toggle <module> [on/off]");
                    return;
                }
            } else {
                module.setState(!module.getState());
            }

            chat((module.getState() ? "Enabled" : "Disabled") + " module §8" + module.getName() + "§3.");
            return;
        }

        chatSyntax("toggle <module> [on/off]");
    }

    @Override
    public List<String> tabComplete(final String[] args) {
        if (args.length == 0) return new ArrayList<>();
        String moduleName = args[1];
        return Faiths.moduleManager.getModules().stream()
                .map(CheatModule::getName)
                .filter(name -> name.toLowerCase().startsWith(moduleName.toLowerCase())).collect(Collectors.toList());
    }
}