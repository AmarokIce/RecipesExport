package cn.mcmod.recipes_export.data;

public class ItemData implements AbstractData {
    String name;
    String count;

    public ItemData(String item) {
        this.name = item;
        this.count = "1";
    }

    public ItemData(String item, String count) {
        this.name = item;
        this.count = count;
    }
}
