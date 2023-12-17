package cn.mcmod.recipe_export.data;

public class OreItemData implements AbstractData {
    String oredict;
    String count;

    public OreItemData(String oredict) {
        this.oredict = oredict;
        this.count = "1";
    }
}