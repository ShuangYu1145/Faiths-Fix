package dev.faiths.module.fun;

import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.value.ValueMode;
import net.minecraft.util.ResourceLocation;


@SuppressWarnings("unused")
public class ModuleCape extends CheatModule {
    public ModuleCape() {
        super("Cape", Category.FUN,"披风");
    }


    public static  ValueMode capes = new ValueMode("Mode", new String[] { "yu" , "furry" , "huoying"}, "yu");

 //   public static final ResourceLocation cape = new ResourceLocation("client/cape/" + ModuleCape.capes.getValue() + ".png");


}
