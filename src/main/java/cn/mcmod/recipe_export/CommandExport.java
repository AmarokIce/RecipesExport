package cn.mcmod.recipe_export;

import com.google.common.collect.Lists;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class CommandExport extends CommandBase {
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

        DataUtil.recipeExcept(outputList, modid);
        sender.addChatMessage(new ChatComponentText("完成！"));
    }

    private void checkRecipeWithModid(List<IRecipe> outputList, List<IRecipe> original, String modid) {
        for (IRecipe recipe : original) {
            try {
                if (checkRecipe(recipe, modid)) outputList.add(recipe);
            } catch (Exception e) {
                RecipeExport.LOGGER.error(e);
            }
        }
    }

    private boolean checkRecipe(IRecipe recipe, String modid) {
        return DataUtil.getModidByItem(recipe.getRecipeOutput().getItem()).toLowerCase().contains(modid);
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] tree) {
        return DataUtil.MODID_LIST;
    }
}
