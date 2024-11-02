package dev.faiths.command;

import com.google.common.collect.Lists;
import dev.faiths.command.impl.*;
import dev.faiths.utils.ClientUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandManager {
    private final List<AbstractCommand> commands = new ArrayList<>();
    private List<String> latestAutoComplete = new ArrayList<>();

    private final char prefix = '.';

    /**
     * Register all default commands
     */
    public void registerCommands() {
        this.commands.addAll(Lists.newArrayList(
                new ToggleCommand(),
                new ReloadCommand(),
                new BindCommand(),
                new HideCommand(),
                new ConfigCommand(),
                new SFCommand()));
    }

    /**
     * Execute command by given [input]
     */
    public void executeCommands(final String input) {
        for (final AbstractCommand command : commands) {
            String[] args = input.split(" ");

            if (args[0].equalsIgnoreCase(prefix + command.getCommand())) {
                try {
                    command.execute(args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }

            for (String alias : command.getAlias()) {
                if (!args[0].equalsIgnoreCase(prefix + alias))
                    continue;

                try {
                    command.execute(args);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return;
            }
        }

        ClientUtils.displayChatMessage("§cCommand not found. Type " + prefix + "help to view all commands.");
    }

    /**
     * Updates the [latestAutoComplete] array based on the provided [input].
     *
     * @param input text that should be used to check for auto completions.
     */
    public boolean autoComplete(final String input) {
        try {
            latestAutoComplete = getCompletions(input);
            return input.startsWith(String.valueOf(prefix)) && !latestAutoComplete.isEmpty();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the auto completions for [input].
     *
     * @param input text that should be used to check for auto completions.
     */
    private List<String> getCompletions(final String input) {
        if (!input.isEmpty() && input.charAt(0) == prefix) {
            final String[] args = input.split(" ");

            if (args.length > 1) {
                final AbstractCommand command = getCommand(args[0].substring(1));

                return command != null ? command.tabComplete(args) : null;
            } else {
                final String rawInput = input.substring(1);
                final List<String> completions = new ArrayList<>();
                for (final AbstractCommand cmd : commands) {
                    if (cmd.getCommand().startsWith(rawInput)
                            || Arrays.stream(cmd.getAlias()).anyMatch(alias -> alias.startsWith(rawInput))) {
                        completions.add(prefix + (cmd.getCommand().startsWith(rawInput) ? cmd.getCommand()
                                : Arrays.stream(cmd.getAlias()).filter(alias -> alias.startsWith(rawInput)).findFirst()
                                        .orElse("")));
                    }
                }
                return completions;
            }
        }
        return null;
    }

    public AbstractCommand getCommand(final String name) {
        for (final AbstractCommand cmd : commands) {
            if (cmd.getCommand().equalsIgnoreCase(name)
                    || Arrays.stream(cmd.getAlias()).anyMatch(alias -> alias.equalsIgnoreCase(name))) {
                return cmd;
            }
        }
        return null;
    }

    public void registerCommand(final AbstractCommand command) {
        commands.add(command);
    }

    public char getPrefix() {
        return this.prefix;
    }

    public List<String> getLatestAutoComplete() {
        return latestAutoComplete;
    }
}