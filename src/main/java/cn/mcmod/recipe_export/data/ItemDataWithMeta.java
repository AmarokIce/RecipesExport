package cn.mcmod.recipe_export.data;

public class ItemDataWithMeta implements AbstractData {
    String name;
    String count;
    String meta;

    public ItemDataWithMeta(String item, String meta) {
        this.name = item;
        this.count = "1";
        this.meta = meta;
    }
}
