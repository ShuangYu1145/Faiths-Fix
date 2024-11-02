package dev.faiths.config.impl;

import dev.faiths.config.AbstractConfig;
import dev.faiths.utils.ClientUtils;
import dev.faiths.utils.elixir.account.CrackedAccount;
import dev.faiths.utils.elixir.account.MinecraftAccount;
import dev.faiths.utils.elixir.manage.AccountSerializer;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AccountsConfig extends AbstractConfig {
    private final List<MinecraftAccount> accounts = new ArrayList<>();

    public AccountsConfig(File file) {
        super(file);
    }

    @Override
    public void load() {
        clearAccounts();
        final Yaml yaml = new Yaml();
        try {
            InputStream inputStream = new FileInputStream(getFile());
            List<Map<String, String>> data = yaml.load(inputStream);
            inputStream.close();
            if (data != null) {
                for (Map<String, String> account : data) {
                    accounts.add(AccountSerializer.INSTANCE.fromYML(account));
                }
            }
        } catch (IOException e) {
            ClientUtils.LOGGER.error(e);
        }
    }

    @Override
    public void save() {
        final Yaml yaml = new Yaml();
        final DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.AUTO);
        options.setPrettyFlow(true);

        try {
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(Files.newOutputStream(getFile().toPath()), StandardCharsets.UTF_8));
            final List<Map<String, String>> data = new ArrayList<>();

            for (final MinecraftAccount minecraftAccount : accounts) {
                data.add(AccountSerializer.INSTANCE.toYML(minecraftAccount));
            }

            yaml.dump(data, writer);
            writer.close();
        } catch (IOException exception) {
            ClientUtils.LOGGER.error(exception);
        }
    }

    public void addCrackedAccount(String name) {
        CrackedAccount crackedAccount = new CrackedAccount();
        crackedAccount.setName(name);

        if (!accountExists(crackedAccount)) accounts.add(crackedAccount);
    }

    public void addAccount(MinecraftAccount account) {
        accounts.add(account);
    }

    public void removeAccount(int selectedSlot) {
        accounts.remove(selectedSlot);
    }

    public List<MinecraftAccount> getAccounts() {
        return accounts;
    }

    public void removeAccount(MinecraftAccount account) {
        accounts.remove(account);
    }

    public boolean accountExists(MinecraftAccount newAccount) {
        for (MinecraftAccount account : accounts) {
            if (account.getClass() == newAccount.getClass() && account.getName().equals(newAccount.getName())) {
                return true;
            }
        }
        return false;
    }

    public void clearAccounts() {
        accounts.clear();
    }
}