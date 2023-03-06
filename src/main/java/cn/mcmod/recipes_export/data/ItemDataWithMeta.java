package cn.mcmod.recipes_export.data;

public class ItemDataWithMeta implements AbstractData {
    String name;
    String count;
    String meta;

    public ItemDataWithMeta(String item, String meta) {
        this.name = item;
        this.count = "1";
        this.meta = meta;
    }

    public ItemDataWithMeta(String item, String count, String meta) {
        this.name = item;
        this.count = count;
        this.meta = meta;
    }
}
