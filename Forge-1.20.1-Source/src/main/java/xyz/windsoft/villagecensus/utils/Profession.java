package xyz.windsoft.villagecensus.utils;

public class Profession {

    //Public variables
    public String localizedName = "";
    public String technicalName = "";
    public String id = "";
    public String professionBlockLocalizedName = "";
    public String professionBlockId = "";
    public int professionBlocksFound = 0;
    public VillageCitizen[] villagers = new VillageCitizen[0];

    //Public methods

    @Override
    public String toString() {
        //Prepare the StringBuilder
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(("localizedName: " + localizedName)).append(", ");
        stringBuilder.append(("technicalName: " + technicalName)).append(", ");
        stringBuilder.append(("id: " + id)).append(", ");
        stringBuilder.append(("professionBlockLocalizedName: " + professionBlockLocalizedName)).append(", ");
        stringBuilder.append(("professionBlockId: " + professionBlockId)).append(", ");
        stringBuilder.append(("professionBlocksFound: " + professionBlocksFound)).append(", ");
        stringBuilder.append(("villagers: (" + villagers.length + ")[ "));
        for (int i = 0; i < villagers.length; i++){
            if (i == 0)
                stringBuilder.append(villagers[i]);
            if (i > 0)
                stringBuilder.append((" ,,, " + villagers[i]));
        }
        stringBuilder.append((" ]"));

        //Return this class converted to a String
        return stringBuilder.toString();
    }
}