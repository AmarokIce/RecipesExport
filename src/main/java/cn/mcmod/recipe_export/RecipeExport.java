package cn.mcmod.recipe_export;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod(modid = RecipeExport.MODID)
public class RecipeExport {
    public static final String MODID = "recipe_export";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void commonInit(FMLInitializationEvent event) {
        DataUtil.init();
    }

    @Mod.EventHandler
    public void serverStartingEvent(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandExport());
    }
}
