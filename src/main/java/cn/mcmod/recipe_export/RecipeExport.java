package cn.mcmod.recipe_export;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Mod(modid = RecipeExport.MODID, useMetadata = true)
public final class RecipeExport {
    public static final String MODID = "recipe_export";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void commonInit(FMLInitializationEvent event) {
        Loader.instance().getModList().forEach(container -> MODID_LIST.add(container.getModId()));
    }

    @Mod.EventHandler
    public void serverStartingEvent(FMLServerStartingEvent event) {
        event.registerServerCommand(new CommandExport());
    }

    private static class CommandExport extends CommandBase {
        @Override
        public String getCommandName() {
            return "recipes_export";
        }

        @Override
        public String getCommandUsage(ICommandSender p_71518_1_) {
            return "/recipes_export <modid/all>";
        }

        @Override @SuppressWarnings("unchecked")
        public void processCommand(ICommandSender sender, String[] tree) {
            if (tree.length < 1) return;
            List<IRecipe> list = (List<IRecipe>) CraftingManager.getInstance().getRecipeList();
            List<IRecipe> outputList = Lists.newArrayList();
            String modid = tree[0].toLowerCase();

            if (!modid.contains("all")) {
                checkRecipeWithModid(outputList, list, modid);
            } else outputList.addAll(list);

            recipeExcept(outputList, modid);
            sender.addChatMessage(new ChatComponentText("完成！"));
        }

        private void checkRecipeWithModid(List<IRecipe> outputList, List<IRecipe> original, String modid) {
            for (IRecipe recipe : original) {
                if (GameRegistry.findUniqueIdentifierFor(recipe.getRecipeOutput().getItem()).modId.toLowerCase().contains(modid)) outputList.add(recipe);
            }
        }

        @Override
        public List<String> addTabCompletionOptions(ICommandSender sender, String[] tree) {
            return MODID_LIST;
        }
    }

    static final List<String> MODID_LIST = Lists.newArrayList();
    static final List<JsonData> DATA_LIST = Lists.newArrayList();

    static void recipeExcept(List<IRecipe> recipes, String modid) {
        DATA_LIST.clear();

        recipes.forEach(RecipeExport::recipeExceptData);
        jsonExport(modid);

        DATA_LIST.clear();
    }

    private static void recipeExceptData(IRecipe recipe) {
        try {
            Map<Integer, AbstractData> map = null;
            String type = null;

            if (recipe instanceof ShapedRecipes) {
                type = "minecraft:crafting_shaped";
                map = ShapedRecipesHelper((ShapedRecipes) recipe);
            } else if (recipe instanceof ShapelessRecipes) {
                type = "minecraft:crafting_shapeless";
                map = ShapelessRecipesHelper((ShapelessRecipes) recipe);
            } else if (recipe instanceof ShapedOreRecipe) {
                type = "minecraft:crafting_shaped";
                map = ShapedRecipesHelper((ShapedOreRecipe) recipe);
            } else if (recipe instanceof ShapelessOreRecipe) {
                type = "minecraft:crafting_shapeless";
                map = ShapelessRecipesHelper((ShapelessOreRecipe) recipe);
            }

            if (type == null || map == null) return;

            Map<Integer, AbstractData> output = Maps.newHashMap();
            String itemName = Item.itemRegistry.getNameForObject(recipe.getRecipeOutput().getItem());
            output.put(1, new ItemDataWithMeta(itemName, Integer.toString(recipe.getRecipeOutput().getItemDamage())));

            JsonData data = new JsonData(type, itemName, map, output);
            DATA_LIST.add(data);
        } catch (Exception e) {
            RecipeExport.LOGGER.error("无法检查食谱：" + recipe.getRecipeOutput().getDisplayName());
        }
    }

    private static Map<Integer, AbstractData> ShapedRecipesHelper(ShapedRecipes recipes) {
        Map<Integer, AbstractData> input = Maps.newHashMap();

        int w = recipes.recipeWidth;
        for (int i = 0; i < recipes.recipeItems.length; i++) {
            final int key = (i / w) * 3 + (i % w) + 1;
            if (recipes.recipeItems[i] == null) continue;
            ItemStack item = recipes.recipeItems[i];
            input.put(key, new ItemDataWithMeta(Item.itemRegistry.getNameForObject(item.getItem()), Integer.toString(item.getItemDamage())));
        }

        return input;
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, AbstractData> ShapedRecipesHelper(ShapedOreRecipe recipes) {
        Map<Integer, AbstractData> input = Maps.newHashMap();

        try {
            Field fieldWidth = recipes.getClass().getDeclaredField("width");
            fieldWidth.setAccessible(true);
            int w = fieldWidth.getInt(recipes);
            for (int i = 0; i < recipes.getInput().length; i++) {
                final int key = (i / w) * 3 + (i % w) + 1;

                if (recipes.getInput()[i] instanceof ItemStack) {
                    ItemStack item = (ItemStack) recipes.getInput()[i];
                    input.put(key, new ItemDataWithMeta(Item.itemRegistry.getNameForObject(item.getItem()), Integer.toString(item.getItemDamage())));
                }
                else if (recipes.getInput()[i] instanceof ArrayList) {
                    ArrayList<ItemStack> ore = (ArrayList<ItemStack>) recipes.getInput()[i];
                    String oreName = OreDictionary.getOreName(OreDictionary.getOreIDs(ore.get(0))[0]);
                    input.put(key, new OreItemData(oreName));
                }
            }

            return input;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("检查合成配方时抛出异常：" + recipes.getRecipeOutput().getDisplayName());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, AbstractData> ShapelessRecipesHelper(ShapelessRecipes recipes) {
        Map<Integer, AbstractData> input = Maps.newHashMap();

        for (int i = 0; i < recipes.recipeItems.size(); i++) {
            if (recipes.recipeItems.get(i) == null) continue;
            ItemStack item = ((List<ItemStack>) recipes.recipeItems).get(i);
            input.put(i + 1, new ItemDataWithMeta(Item.itemRegistry.getNameForObject(item.getItem()), Integer.toString(item.getItemDamage())));
        }

        return input;
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, AbstractData> ShapelessRecipesHelper(ShapelessOreRecipe recipes) {
        Map<Integer, AbstractData> input = Maps.newHashMap();

        for (int i = 0; i < recipes.getInput().size(); i++) {
            final int key = i + 1;
            if (recipes.getInput().get(i) instanceof ItemStack) {
                ItemStack item = (ItemStack) recipes.getInput().get(i);
                input.put(key, new ItemDataWithMeta(Item.itemRegistry.getNameForObject(item.getItem()), Integer.toString(item.getItemDamage())));
            }
            else if (recipes.getInput().get(i) instanceof ArrayList) {
                ArrayList<ItemStack> ore = (ArrayList<ItemStack>) recipes.getInput().get(i);
                String oreName = OreDictionary.getOreName(OreDictionary.getOreIDs(ore.get(0))[0]);
                input.put(key, new OreItemData(oreName));
            }
        }

        return input;
    }

    @SuppressWarnings("all")
    private static void jsonExport(String modid) {
        Gson gson = new Gson();
        String str = gson.toJson(DATA_LIST);
        File file = new File(System.getProperty("user.dir") + File.separator + "export", modid + "_recipe.json");
        try {
            if (!file.exists() || !file.isFile()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            Files.write(file.toPath(), str.getBytes());
            DATA_LIST.clear();

            RecipeExport.LOGGER.info("完成写入！");
        } catch (IOException e) { RecipeExport.LOGGER.error("写入文件失败！", e); }
    }

    private static final class JsonData implements AbstractData {
        final String type, name;
        final Map<Integer, AbstractData> input, output;

        private JsonData(String type, String name, Map<Integer, AbstractData> input, Map<Integer, AbstractData> output) {
            this.name = name;
            this.type = type;
            this.input = input;
            this.output = output;
        }
    }

    private static final class ItemDataWithMeta implements AbstractData {
        final String name, count, meta;

        private ItemDataWithMeta(String item, String meta) {
            this.name = item;
            this.count = "1";
            this.meta = meta;
        }
    }

    private static final class OreItemData implements AbstractData {
        final String oredict, count;

        private OreItemData(String oredict) {
            this.oredict = oredict;
            this.count = "1";
        }
    }

    private interface AbstractData {
    }
}
