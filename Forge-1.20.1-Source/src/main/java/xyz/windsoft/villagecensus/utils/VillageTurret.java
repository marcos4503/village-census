package xyz.windsoft.villagecensus.utils;

import java.util.UUID;

public class VillageTurret {

    //Public variables
    public String name = "";
    public UUID uuid = UUID.randomUUID();
    public float hpPercent = 0.0f;

    //Public methods

    @Override
    public String toString() {
        //Prepare the StringBuilder
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(("name: " + name)).append(", ");
        stringBuilder.append(("uuid: " + uuid)).append(", ");
        stringBuilder.append(("hpPercent: " + hpPercent));

        //Return this class converted to a String
        return stringBuilder.toString();
    }
}