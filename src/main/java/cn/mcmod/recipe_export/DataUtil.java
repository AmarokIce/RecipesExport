package cn.mcmod.recipe_export;

import cn.mcmod.recipe_export.data.AbstractData;
import cn.mcmod.recipe_export.data.ItemDataWithMeta;
import cn.mcmod.recipe_export.data.OreItemData;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DataUtil {
    static final List<String> MODID_LIST = Lists.newArrayList();
    static final List<JsonData> DATA_LIST = Lists.newArrayList();

    public static void init() {
        RecipeExport.LOGGER.info("========== Get ModID Start ==========");

        Loader.instance().getModList().forEach(DataUtil::addModidToList);

        RecipeExport.LOGGER.info("========== Get ModID End ==========");
    }

    private static void addModidToList(ModContainer container) {
        MODID_LIST.add(container.getModId());
    }

    static void recipeExcept(List<IRecipe> recipes, String modid) {
        DataUtil.DATA_LIST.clear();

        recipes.forEach(DataUtil::recipeExceptData);
        jsonExport(modid);

        DataUtil.DATA_LIST.clear();
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

    static String getModidByItem(Item item) {
        String itemKey = Item.itemRegistry.getNameForObject(item);
        return itemKey.substring(0, itemKey.indexOf(":"));
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
            throw new RuntimeException("检查食谱时抛出异常：" + recipes.getRecipeOutput().getDisplayName());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<Integer, AbstractData> ShapelessRecipesHelper(ShapelessRecipes recipes) {
        Map<Integer, AbstractData> input = Maps.newHashMap();

        for (int i = 0; i < recipes.recipeItems.size(); i++) {
            final int key = i + 1;
            if (recipes.recipeItems.get(i) == null) continue;
            ItemStack item = ((List<ItemStack>) recipes.recipeItems).get(i);
            input.put(key, new ItemDataWithMeta(Item.itemRegistry.getNameForObject(item.getItem()), Integer.toString(item.getItemDamage())));
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

    private static final class JsonData implements AbstractData {
        String type;
        String name;
        Map<Integer, AbstractData> input;
        Map<Integer, AbstractData> output;

        public JsonData(String type, String name, Map<Integer, AbstractData> input, Map<Integer, AbstractData> output) {
            this.name = name;
            this.type = type;
            this.input = input;
            this.output = output;
        }
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
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(str.getBytes());
            outputStream.close();
            DATA_LIST.clear();

            RecipeExport.LOGGER.info("完成写入！");
        } catch (IOException e) {
            RecipeExport.LOGGER.error("写入文件失败！", e);
        }
    }
}
