package dev.faiths.module.misc;

import dev.faiths.module.Category;
import dev.faiths.module.CheatModule;
import dev.faiths.value.ValueMode;


@SuppressWarnings("unused")
public class ModuleCape extends CheatModule {
    public ModuleCape() {
        super("Cape", Category.Misc,"披风");
    }


    public static  ValueMode capes = new ValueMode("Mode", new String[] { "yu" , "furry" , "huoying"}, "yu");

 //   public static final ResourceLocation cape = new ResourceLocation("client/cape/" + ModuleCape.capes.getValue() + ".png");


}
