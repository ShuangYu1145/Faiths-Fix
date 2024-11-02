package dev.faiths.command.impl;

import dev.faiths.Faiths;
import dev.faiths.command.AbstractCommand;
import dev.faiths.module.CheatModule;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HideCommand extends AbstractCommand {
    
    public HideCommand() {
        super("hide", "hidden");
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
            
            module.setHidden(!module.isHidden());
            chat((module.isHidden() ? "Hid" : "Displayed") + " module §8" + module.getName() + "§3.");
            return;
        }

        chatSyntax("hied <module>");
    }

    @Override
    public List<String> tabComplete(final String[] args) {
        if (args.length == 0) return new ArrayList<>();
        String moduleName = args[1];
        return Faiths.INSTANCE.moduleManager.getModules().stream()
                .map(CheatModule::getName)
                .filter(name -> name.toLowerCase().startsWith(moduleName.toLowerCase())).collect(Collectors.toList());
    }
}