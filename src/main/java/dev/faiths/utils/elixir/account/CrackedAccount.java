package dev.faiths.utils.elixir.account;

import dev.faiths.utils.elixir.compat.Session;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

public final class CrackedAccount
extends MinecraftAccount {
    private String name = "Player";

    public CrackedAccount() {
        super("Cracked");
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
        loadHeadResource(name);
    }

    @Override
    public Session getSession() {
        byte[] bytes = this.getName().getBytes(StandardCharsets.UTF_8);
        String uuid = UUID.nameUUIDFromBytes(bytes).toString();
        return new Session(this.getName(), uuid, "-", "legacy");
    }

    @Override
    public void update() {
    }

    @Override
    public void toRawYML(final Map<String, String> data) {
        data.put("name", this.getName());
    }

    @Override
    public void fromRawYML(final Map<String, String> data) {
        this.setName(data.get("name"));
    }
}

