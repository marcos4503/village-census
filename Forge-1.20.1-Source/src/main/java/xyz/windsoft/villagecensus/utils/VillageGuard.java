package xyz.windsoft.villagecensus.utils;

import java.util.UUID;

public class VillageGuard {

    //Public variables
    public String name = "";
    public UUID uuid = UUID.randomUUID();
    public String weaponType = ""; //<- Can be "melee", "ranged" or "none"
    public String weaponLocalizedName = "";
    public float weaponPercent = 0.0f;
    public String shieldLocalizedName = "";
    public float shieldPercent = 0.0f;
    public String potionLocalizedName = "";
    public int potionCount = 0;
    public String foodLocalizedName = "";
    public int foodCount = 0;
    public String helmetLocalizedName = "";
    public float helmetPercent = 0.0f;
    public String chestplateLocalizedName = "";
    public float chestplatePercent = 0.0f;
    public String leggingsLocalizedName = "";
    public float leggingsPercent = 0.0f;
    public String bootsLocalizedName = "";
    public float bootsPercent = 0.0f;
    public float hpPercent = 0.0f;

    //Public methods

    @Override
    public String toString() {
        //Prepare the StringBuilder
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(("name: " + name)).append(", ");
        stringBuilder.append(("uuid: " + uuid)).append(", ");
        stringBuilder.append(("weaponType: " + weaponType)).append(", ");
        stringBuilder.append(("weaponLocalizedName: " + weaponLocalizedName)).append(", ");
        stringBuilder.append(("weaponPercent: " + weaponPercent)).append(", ");
        stringBuilder.append(("shieldLocalizedName: " + shieldLocalizedName)).append(", ");
        stringBuilder.append(("shieldPercent: " + shieldPercent)).append(", ");
        stringBuilder.append(("potionLocalizedName: " + potionLocalizedName)).append(", ");
        stringBuilder.append(("potionCount: " + potionCount)).append(", ");
        stringBuilder.append(("foodLocalizedName: " + foodLocalizedName)).append(", ");
        stringBuilder.append(("foodCount: " + foodCount)).append(", ");
        stringBuilder.append(("helmetLocalizedName: " + helmetLocalizedName)).append(", ");
        stringBuilder.append(("helmetPercent: " + helmetPercent)).append(", ");
        stringBuilder.append(("chestplateLocalizedName: " + chestplateLocalizedName)).append(", ");
        stringBuilder.append(("chestplatePercent: " + chestplatePercent)).append(", ");
        stringBuilder.append(("leggingsLocalizedName: " + leggingsLocalizedName)).append(", ");
        stringBuilder.append(("leggingsPercent: " + leggingsPercent)).append(", ");
        stringBuilder.append(("bootsLocalizedName: " + bootsLocalizedName)).append(", ");
        stringBuilder.append(("bootsPercent: " + bootsPercent)).append(", ");
        stringBuilder.append(("hpPercent: " + hpPercent)).append(", ");

        //Return this class converted to a String
        return stringBuilder.toString();
    }
}