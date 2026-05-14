package xyz.windsoft.villagecensus.utils;

import java.util.UUID;

public class VillageCitizen {

    //Public variables
    public String name = "";
    public UUID uuid = UUID.randomUUID();
    public boolean isBaby = false;
    public int professionLvl = 0;
    public int nonFoodItensCount = 0;
    public int foodItensCount = 0;
    public int jobX = 0;
    public int jobY = 0;
    public int jobZ = 0;
    public int jobDistance = 0;
    public float hpPercent = 0.0f;

    //Public methods

    @Override
    public String toString() {
        //Prepare the StringBuilder
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(("name: " + name)).append(", ");
        stringBuilder.append(("uuid: " + uuid)).append(", ");
        stringBuilder.append(("isBaby: " + isBaby)).append(", ");
        stringBuilder.append(("professionLvl: " + professionLvl)).append(", ");
        stringBuilder.append(("nonFoodItensCount: " + nonFoodItensCount)).append(", ");
        stringBuilder.append(("foodItensCount: " + foodItensCount)).append(", ");
        stringBuilder.append(("jobX: " + jobX)).append(", ");
        stringBuilder.append(("jobY: " + jobY)).append(", ");
        stringBuilder.append(("jobZ: " + jobZ)).append(", ");
        stringBuilder.append(("jobDistance: " + jobDistance)).append(", ");
        stringBuilder.append(("hpPercent: " + hpPercent));

        //Return this class converted to a String
        return stringBuilder.toString();
    }
}