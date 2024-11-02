package dev.faiths.module.player;

import com.google.gson.Gson;
import com.mojang.authlib.HttpAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.request.JoinMinecraftServerRequest;
import com.mojang.util.UUIDTypeAdapter;
import dev.faiths.event.Handler;
import dev.faiths.event.impl.TickUpdateEvent;
import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.utils.MSTimer;

import java.io.IOException;
import java.net.Proxy;
import java.util.Random;
import java.util.UUID;

import static dev.faiths.utils.IMinecraft.mc;

@SuppressWarnings("unused")
public class ModuleRatProtection extends CheatModule {
    public ModuleRatProtection() {
        super("RatProtection", Category.PLAYER);
    }

    public MSTimer ms = new MSTimer();

    public void lambda$onTick$0() {
        String var1 = String.valueOf((new Random()).nextInt());

        try {
            String var2 = this.method0(mc.getSession().getToken(), UUIDTypeAdapter.fromString(mc.getSession().getPlayerID()), var1);
            if (!var2.isEmpty()) {
                if (var2.startsWith("<")) {
                    ms.addTime(-5000L);
                }

                return;
            }

            ms.addTime(300L);
        } catch (Exception var3) {
        }

    }

    public String method0(String var1, UUID var2, String var3) throws IOException {
        YggdrasilAuthenticationService var4 = new YggdrasilAuthenticationService(Proxy.NO_PROXY, UUID.randomUUID().toString());
        JoinMinecraftServerRequest var5 = new JoinMinecraftServerRequest();
        var5.accessToken = var1;
        var5.selectedProfile = var2;
        var5.serverId = var3;
        return var4.performPostRequest(HttpAuthenticationService.constantURL("https://sessionserver.mojang.com/session/minecraft/join"), (new Gson()).toJson(var5), "application/json");
    }

    public final Handler<TickUpdateEvent> eventHandler = event -> {
        if (mc.theWorld != null && mc.thePlayer != null && ms.check(1000)) {
            new Thread(this::lambda$onTick$0).start();
        }
    };
}
