package cn.mcmod.recipes_export;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;

import java.util.*;

public class DataUtil {
    public static final List<String> modList = new ArrayList<>();
    public static final List<String> errorList = new ArrayList<>();

    public DataUtil() {
        RecipesMain.LOGGER.info("=== Start read all modid. ===");
        for (ModContainer container : Loader.instance().getModList())
            modList.add(container.getModId());

        RecipesMain.LOGGER.info("=== Read mod list finish. ===");
        RecipesMain.LOGGER.info(modList.size() + " Mods get!");
    }

    public static String getModidFromRegisterName(String str) {
        return str.substring(0, str.indexOf(":"));
    }

    /**
     * @param str1 First String.
     * @param str2 Second String.
     * @return The String repeat char size.
     */
    public static int StringCounter(String str1, String str2) {
        char[] charStr1 = str1.toCharArray();
        List<Character> list = new ArrayList<>();
        for (char value : charStr1)
            if (!list.contains(value)) list.add(value);

        int j = 0;
        char[] charStr2 = str2.toCharArray();
        List<Character> list2 = new ArrayList<>();
        for (char c : charStr2) {
            if (!list2.contains(c) && list.contains(c)) {
                list2.add(c);
                j++;
            }
        }

        return j;
    }

    /**
     * @param modid The modid that cannot find in list.
     * @return An installation mod's modid close the modid.
     */
    public static String StringChecker(String modid) {
        Map<String, Integer> map = new HashMap<>();

        for (String str : modList)
            map.put(str, StringCounter(str, modid));

        List<Map.Entry<String,Integer>> list = new ArrayList<>(map.entrySet());
        list.sort((i, o) -> (o.getValue() - i.getValue()));
        return list.get(0).getKey();
    }
}
