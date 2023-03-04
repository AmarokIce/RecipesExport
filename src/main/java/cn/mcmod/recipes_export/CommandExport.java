package cn.mcmod.recipes_export;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

import javax.xml.crypto.Data;
import java.util.Map;
import java.util.Objects;

public class CommandExport extends CommandBase {
    @Override
    public String getName() {
        return "recipes_export";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "recipes_export <modid>";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        DataUtil.errorList.clear();

        if (DataUtil.modList.contains(args[0])) findRecipes(args[0]);
        else sender.sendMessage(new TextComponentString("Cannot find this modid, Maybe you wanna to key ：" + DataUtil.StringChecker(args[0])));

        sender.sendMessage(new TextComponentString("Successful!"));
        if (!DataUtil.errorList.isEmpty()) sender.sendMessage(new TextComponentString("There are " + DataUtil.errorList.size() + " oredict recipe has error: " + DataUtil.errorList.toString()));
    }

    private void findRecipes(String modid) {
        JsonHelper.init();
        for (IRecipe recipe : CraftingManager.REGISTRY) {
            if (!DataUtil.getModinFromRegisterName(Objects.requireNonNull(recipe.getRegistryName()).toString()).equals(modid))
                continue;
            JsonHelper.putRecipe(recipe);
        }

        Map<ItemStack, ItemStack> smeltMap = FurnaceRecipes.instance().getSmeltingList();
        for (ItemStack item : smeltMap.keySet()) {
            if (DataUtil.getModinFromRegisterName(item.getItem().getRegistryName().toString()).equals(modid) || DataUtil.getModinFromRegisterName(smeltMap.get(item).getItem().getRegistryName().toString()).equals(modid)) {
                JsonHelper.addSmeltingRecipes(item, smeltMap.get(item));
            }
        }

        JsonHelper.finish(modid);
    }
}
