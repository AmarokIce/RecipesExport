package cn.mcmod.recipes_export;

import com.google.common.collect.Maps;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.registry.GameRegistry;

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

        if (args[0].equals("ALL") || DataUtil.modList.contains(args[0])) findRecipes(args[0]);
        else sender.sendMessage(new TextComponentString("Cannot find this modid, Maybe you wanna to key ：" + DataUtil.StringChecker(args[0])));

        sender.sendMessage(new TextComponentString("Successful!"));
        if (!DataUtil.errorList.isEmpty()) sender.sendMessage(new TextComponentString("There are " + DataUtil.errorList.size() + " oredict recipe has error: " + DataUtil.errorList.toString()));
    }

    private void findRecipes(String modid) {
        if (!modid.equals("ALL")) {
            JsonHelper helper = new JsonHelper();
            for (IRecipe recipe : CraftingManager.REGISTRY) {
                if (!DataUtil.getModidFromRegisterName(Objects.requireNonNull(recipe.getRegistryName()).toString()).equals(modid))
                    continue;
                try {
                    helper.putRecipe(recipe);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }

            Map<ItemStack, ItemStack> smeltMap = FurnaceRecipes.instance().getSmeltingList();
            for (ItemStack item : smeltMap.keySet()) {
                if ((DataUtil.getModidFromRegisterName(item.getItem().getRegistryName().toString()).equals(modid) || DataUtil.getModidFromRegisterName(smeltMap.get(item).getItem().getRegistryName().toString()).equals(modid))) {
                    helper.addSmeltingRecipes(item, smeltMap.get(item));
                }
            }

            helper.finish(modid);

            helper = null; // delete the helper.
        } else {
            Map<String, JsonHelper> helperList = Maps.newHashMap();

            for (IRecipe recipe : CraftingManager.REGISTRY) {
                String id = DataUtil.getModidFromRegisterName(recipe.getRegistryName().toString());
                try {
                    if (!helperList.containsKey(id)) helperList.put(id, new JsonHelper());
                    helperList.get(id).putRecipe(recipe);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }

            Map<ItemStack, ItemStack> smeltMap = FurnaceRecipes.instance().getSmeltingList();
            for (ItemStack item : smeltMap.keySet()) {
                String id = DataUtil.getModidFromRegisterName(Item.REGISTRY.getNameForObject(item.getItem()).toString());
                if (!helperList.containsKey(id)) helperList.put(id, new JsonHelper());
                helperList.get(id).addSmeltingRecipes(item, smeltMap.get(item));
            }

            for (String id : helperList.keySet()) {
                helperList.get(id).finish(id);
            }

            helperList.clear(); // delete the helper.
        }
    }
}
