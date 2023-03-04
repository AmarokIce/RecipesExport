package cn.mcmod.recipes_export;

import cn.mcmod.recipes_export.data.AbstractData;
import cn.mcmod.recipes_export.data.ItemData;
import cn.mcmod.recipes_export.data.OreItemData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

public class JsonHelper {

    /* Recipes Export */
    static Map<String, List<JsonData>> list = new HashMap<>();

    public static void init() {
        list.put("recipes", new ArrayList<>());
    }

    public static void putRecipe(IRecipe recipe) {
        try {
            Map<Integer, AbstractData> map = new HashMap<>();
            String name = recipe.getRegistryName().toString();
            String type = "";
            RecipesMain.LOGGER.debug("Now export item: " + recipe.getRecipeOutput().getDisplayName());
            if (recipe instanceof ShapedRecipes) {
                type = "minecraft:crafting_shaped";
                map = JsonHelper.ShapedRecipesHelper((ShapedRecipes) recipe);
            } else if (recipe instanceof ShapedOreRecipe) {
                type = "minecraft:crafting_shaped";
                map = JsonHelper.ShapedRecipesHelper((ShapedOreRecipe) recipe);
            } else if (recipe instanceof ShapelessRecipes) {
                type = "minecraft:crafting_shapeless";
                map = JsonHelper.ShapelessRecipesHelper((ShapelessRecipes) recipe);
            } else if (recipe instanceof ShapelessOreRecipe) {
                type = "minecraft:crafting_shapeless";
                map = JsonHelper.ShapelessRecipesHelper((ShapelessOreRecipe) recipe);
            }

            Map<String, AbstractData> output = new HashMap<>();
            output.put("1", new ItemData(recipe.getRecipeOutput().getItem().getRegistryName().toString(), Integer.toString(recipe.getRecipeOutput().getCount())));

            JsonData jsonData = new JsonData(type, name, map, output);
            list.get("recipes").add(jsonData);
        } catch (NullPointerException e) {
            e.printStackTrace();
            DataUtil.errorList.add(recipe.getRecipeOutput().getDisplayName());
        }
    }

    private static Map<Integer, AbstractData> ShapedRecipesHelper(ShapedRecipes recipes) {
        Map<Integer, AbstractData> input = new HashMap<>();
        int w = recipes.recipeWidth;
        for (int i = 0; i < recipes.recipeItems.size(); i++) {
            final int key = (i / w) * 3 + (i % w) + 1;
            if (recipes.recipeItems.get(i).getClass() == Ingredient.class)
                input.put(key, new ItemData(Objects.requireNonNull(recipes.recipeItems.get(i).getMatchingStacks()[0].getItem().getRegistryName()).toString()));
            else if (recipes.recipeItems.get(i).getClass() == OreIngredient.class) {
                String ore = getOreDict((OreIngredient) recipes.recipeItems.get(i));
                if (ore != null) input.put(key, new OreItemData(ore));
                else throw new NullPointerException("Cannot find the oredict in a recipes when export then!");
            }
        }

        return input;
    }

    private static Map<Integer, AbstractData> ShapedRecipesHelper(ShapedOreRecipe recipes) {
        Map<Integer, AbstractData> input = new HashMap<>();
        int w = recipes.getRecipeWidth();
        for (int i = 0; i < recipes.getIngredients().size(); i++) {
            final int key = (i / w) * 3 + (i % w) + 1;
            if (recipes.getIngredients().get(i).getClass() == Ingredient.class)
                input.put(key, new ItemData(Objects.requireNonNull(recipes.getIngredients().get(i).getMatchingStacks()[0].getItem().getRegistryName()).toString()));
            else if (recipes.getIngredients().get(i).getClass() == OreIngredient.class) {
                String ore = getOreDict((OreIngredient) recipes.getIngredients().get(i));
                if (ore != null) input.put(key, new OreItemData(ore));
                else throw(new NullPointerException("On get item oredict have some error: " + recipes.getRecipeOutput().getDisplayName()));
            }
        }

        return input;
    }

    private static Map<Integer, AbstractData> ShapelessRecipesHelper(ShapelessRecipes recipes) {
        Map<Integer, AbstractData> input = new HashMap<>();
        for (int i = 0; i < recipes.recipeItems.size(); i++) {
            final int key = i + 1;
            if (recipes.recipeItems.get(i).getClass() == Ingredient.class)
                input.put(key, new ItemData(Objects.requireNonNull(recipes.recipeItems.get(i).getMatchingStacks()[0].getItem().getRegistryName()).toString()));
            else if (recipes.recipeItems.get(i).getClass() == OreIngredient.class) {
                String ore = getOreDict((OreIngredient) recipes.recipeItems.get(i));
                if (ore != null) input.put(key, new OreItemData(ore));
                else throw(new NullPointerException("On get item oredict have some error: " + recipes.getRecipeOutput().getDisplayName()));
            }
        }

        return input;
    }

    private static Map<Integer, AbstractData> ShapelessRecipesHelper(ShapelessOreRecipe recipes) {
        Map<Integer, AbstractData> input = new HashMap<>();
        for (int i = 0; i < recipes.getIngredients().size(); i++) {
            final int key = i + 1;
            if (recipes.getIngredients().get(i).getClass() == Ingredient.class)
                input.put(key, new ItemData(Objects.requireNonNull(recipes.getIngredients().get(i).getMatchingStacks()[0].getItem().getRegistryName()).toString()));
            else if (recipes.getIngredients().get(i).getClass() == OreIngredient.class) {
                String ore = getOreDict((OreIngredient) recipes.getIngredients().get(i));
                if (ore != null) input.put(key, new OreItemData(ore));
                else throw new NullPointerException("Cannot find the oredict in a recipes when export then!");
            }
        }

        return input;
    }

    private static String getOreDict(OreIngredient ingre) {
        if (ingre.getMatchingStacks().length < 1) return null;
        int[] oreIDs = OreDictionary.getOreIDs(ingre.getMatchingStacks()[0]);
        if (oreIDs.length == 1) return OreDictionary.getOreName(oreIDs[0]);
        else for (int oreID : oreIDs)
            if (OreDictionary.getOres(OreDictionary.getOreName(oreID)).stream().allMatch(ingre))
                return OreDictionary.getOreName(oreID);

        return ingre.getMatchingStacks()[0].getItem().getRegistryName().toString();
    }


    /* Smelting Export */
    public static void addSmeltingRecipes(ItemStack input, ItemStack output) {
        String type = "minecraft:smelting";
        String name = output.getItem().getRegistryName().toString();

        Map<String, AbstractData> inputMap = new HashMap<>();
        Map<String, AbstractData> outputMap = new HashMap<>();
        inputMap.put("1", new ItemData(input.getItem().getRegistryName().toString()));
        outputMap.put("1", new ItemData(output.getItem().getRegistryName().toString(), Integer.toString(output.getCount())));

        list.get("recipes").add(new JsonData(type, name, inputMap, outputMap));
    }

    public static void finish(String modid) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            File file = new File(System.getProperty("user.dir") + File.separator + "export", modid + "_recipe.json");
            if (!file.getParentFile().isDirectory()) file.getParentFile().mkdirs();
            if (!file.isFile() || !file.exists()) file.createNewFile();

            String str = gson.toJson(list);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(str.getBytes());
            outputStream.close();
            list.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class JsonData implements AbstractData {
        String type;
        String name;
        Map<Integer, AbstractData> input;
        Map<Integer, AbstractData> output;

        public JsonData(String type, String name, Map input, Map output) {
            this.name = name;
            this.type = type;
            this.input = input;
            this.output = output;
        }
    }
}
