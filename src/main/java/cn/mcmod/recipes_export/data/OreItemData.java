package cn.mcmod.recipes_export.data;

public class OreItemData implements AbstractData{
    String oredict;
    String count;

    public OreItemData(String oredict) {
        this.oredict = oredict;
        this.count = "1";
    }

    public String getOredict() {
        return oredict;
    }

    public void setOredict(String oredict) {
        this.oredict = oredict;
    }

    public String getCount() {
        return count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
