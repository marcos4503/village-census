package xyz.windsoft.villagecensus.block.entity;

import com.sun.jna.platform.win32.COM.util.annotation.ComObject;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.monster.ZombieVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.windsoft.villagecensus.block.ModBlockEntities;
import xyz.windsoft.villagecensus.utils.*;

import java.util.*;
import java.util.List;

/*
 * This class creates the custom behavior for the block entity binded to block "Census Lectern"
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class CensusLecternBlockEntity extends BlockEntity {

    //Private final variables
    private final long CENSUS_INTERVAL_GAME_TICKS = 6000; //<- Each 1000 ticks is equal to 1 hour of Game Time. Default: 6000
    private final int CENSUS_RESEARCH_RADIUS = 128;       //<- In Blocks

    //Private variables
    private long lastCensusGameTime = 0l;
    private boolean isVillage = false;
    private int totalVillagers = 0;
    private int regularVillagers = 0;
    private int guardVillagers = 0;
    private int zombieVillagers = 0;
    private int ironGolems = 0;
    private int snowGolems = 0;
    private int cats = 0;
    private int turrets = 0;
    private int totalBeds = 0;
    private int claimedBeds = 0;
    private int unclaimedBeds = 0;
    private int totalProfessionBlocks = 0;
    private int claimedProfessionBlocks = 0;
    private int unclaimedProfessionBlocks = 0;
    private int bells = 0;
    private List<Profession> professionsList = new ArrayList<>();
    private List<VillageGuard> guardsList = new ArrayList<>();
    private List<VillageIronGolem> ironGolemsList = new ArrayList<>();
    private List<VillageSnowGolem> snowGolemsList = new ArrayList<>();
    private List<VillageTurret> turretsList = new ArrayList<>();

    //Public methods

    public CensusLecternBlockEntity(BlockPos pPos, BlockState pBlockState) {
        //Repass the properties to parent class of this class
        super(ModBlockEntities.CENSUS_LECTERN_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    @Override
    public void load(CompoundTag pTag) {
        //Repass this call to parent class of this class
        super.load(pTag);

        //Load this block data, when the Game is loading data
        lastCensusGameTime = pTag.getLong("lastCensusGameTime");
        isVillage = pTag.getBoolean("isVillage");
        totalVillagers = pTag.getInt("totalVillagers");
        regularVillagers = pTag.getInt("regularVillagers");
        guardVillagers = pTag.getInt("guardVillagers");
        zombieVillagers = pTag.getInt("zombieVillagers");
        ironGolems = pTag.getInt("ironGolems");
        snowGolems = pTag.getInt("snowGolems");
        cats = pTag.getInt("cats");
        turrets = pTag.getInt("turrets");
        totalBeds = pTag.getInt("totalBeds");
        claimedBeds = pTag.getInt("claimedBeds");
        unclaimedBeds = pTag.getInt("unclaimedBeds");
        totalProfessionBlocks = pTag.getInt("totalProfessionBlocks");
        claimedProfessionBlocks = pTag.getInt("claimedProfessionBlocks");
        unclaimedProfessionBlocks = pTag.getInt("unclaimedProfessionBlocks");
        bells = pTag.getInt("bells");
        professionsList.clear();
        ListTag professionsListRaw = pTag.getList("professionsList", Tag.TAG_COMPOUND);
        for (int i = 0; i < professionsListRaw.size(); i++){
            CompoundTag currentEntry = professionsListRaw.getCompound(i);
            Profession profession = new Profession();
            profession.localizedName = currentEntry.getString("localizedName");
            profession.technicalName = currentEntry.getString("technicalName");
            profession.id = currentEntry.getString("id");
            profession.professionBlockLocalizedName = currentEntry.getString("professionBlockLocalizedName");
            profession.professionBlockId = currentEntry.getString("professionBlockId");
            profession.professionBlocksFound = currentEntry.getInt("professionBlocksFound");
            ListTag villagersListRaw = currentEntry.getList("villagers", Tag.TAG_COMPOUND);
            List<VillageCitizen> tmpVillagers = new ArrayList<>();
            for (int x = 0; x < villagersListRaw.size(); x++){
                CompoundTag currentSubEntry = villagersListRaw.getCompound(x);
                VillageCitizen villageCitizen = new VillageCitizen();
                villageCitizen.name = currentSubEntry.getString("name");
                villageCitizen.uuid = currentSubEntry.getUUID("uuid");
                villageCitizen.isBaby = currentSubEntry.getBoolean("isBaby");
                villageCitizen.professionLvl = currentSubEntry.getInt("professionLvl");
                villageCitizen.nonFoodItensCount = currentSubEntry.getInt("nonFoodItensCount");
                villageCitizen.foodItensCount = currentSubEntry.getInt("foodItensCount");
                villageCitizen.jobX = currentSubEntry.getInt("jobX");
                villageCitizen.jobY = currentSubEntry.getInt("jobY");
                villageCitizen.jobZ = currentSubEntry.getInt("jobZ");
                villageCitizen.jobDistance = currentSubEntry.getInt("jobDistance");
                villageCitizen.hpPercent = currentSubEntry.getFloat("hpPercent");
                tmpVillagers.add(villageCitizen);
            }
            profession.villagers = tmpVillagers.toArray(VillageCitizen[]::new);
            professionsList.add(profession);
        }
        guardsList.clear();
        ListTag guardsListRaw = pTag.getList("guardsList", Tag.TAG_COMPOUND);
        for (int i = 0; i < guardsListRaw.size(); i++){
            CompoundTag currentEntry = guardsListRaw.getCompound(i);
            VillageGuard villageGuard = new VillageGuard();
            villageGuard.name = currentEntry.getString("name");
            villageGuard.uuid = currentEntry.getUUID("uuid");
            villageGuard.weaponType = currentEntry.getString("weaponType");
            villageGuard.weaponLocalizedName = currentEntry.getString("weaponLocalizedName");
            villageGuard.weaponPercent = currentEntry.getFloat("weaponPercent");
            villageGuard.shieldLocalizedName = currentEntry.getString("shieldLocalizedName");
            villageGuard.shieldPercent = currentEntry.getFloat("shieldPercent");
            villageGuard.potionLocalizedName = currentEntry.getString("potionLocalizedName");
            villageGuard.potionCount = currentEntry.getInt("potionCount");
            villageGuard.foodLocalizedName = currentEntry.getString("foodLocalizedName");
            villageGuard.foodCount = currentEntry.getInt("foodCount");
            villageGuard.helmetLocalizedName = currentEntry.getString("helmetLocalizedName");
            villageGuard.helmetPercent = currentEntry.getFloat("helmetPercent");
            villageGuard.chestplateLocalizedName = currentEntry.getString("chestplateLocalizedName");
            villageGuard.chestplatePercent = currentEntry.getFloat("chestplatePercent");
            villageGuard.leggingsLocalizedName = currentEntry.getString("leggingsLocalizedName");
            villageGuard.leggingsPercent = currentEntry.getFloat("leggingsPercent");
            villageGuard.bootsLocalizedName = currentEntry.getString("bootsLocalizedName");
            villageGuard.bootsPercent = currentEntry.getFloat("bootsPercent");
            villageGuard.hpPercent = currentEntry.getFloat("hpPercent");
            guardsList.add(villageGuard);
        }
        ironGolemsList.clear();
        ListTag ironGolemsListRaw = pTag.getList("ironGolemsList", Tag.TAG_COMPOUND);
        for (int i = 0; i < ironGolemsListRaw.size(); i++) {
            CompoundTag currentEntry = ironGolemsListRaw.getCompound(i);
            VillageIronGolem villageIronGolem = new VillageIronGolem();
            villageIronGolem.name = currentEntry.getString("name");
            villageIronGolem.uuid = currentEntry.getUUID("uuid");
            villageIronGolem.hpPercent = currentEntry.getFloat("hpPercent");
            ironGolemsList.add(villageIronGolem);
        }
        snowGolemsList.clear();
        ListTag snowGolemsListRaw = pTag.getList("snowGolemsList", Tag.TAG_COMPOUND);
        for (int i = 0; i < snowGolemsListRaw.size(); i++) {
            CompoundTag currentEntry = snowGolemsListRaw.getCompound(i);
            VillageSnowGolem villageSnowGolem = new VillageSnowGolem();
            villageSnowGolem.name = currentEntry.getString("name");
            villageSnowGolem.uuid = currentEntry.getUUID("uuid");
            villageSnowGolem.hpPercent = currentEntry.getFloat("hpPercent");
            snowGolemsList.add(villageSnowGolem);
        }
        turretsList.clear();
        ListTag turretsListRaw = pTag.getList("turretsList", Tag.TAG_COMPOUND);
        for (int i = 0; i < turretsListRaw.size(); i++) {
            CompoundTag currentEntry = turretsListRaw.getCompound(i);
            VillageTurret villageTurret = new VillageTurret();
            villageTurret.name = currentEntry.getString("name");
            villageTurret.uuid = currentEntry.getUUID("uuid");
            villageTurret.hpPercent = currentEntry.getFloat("hpPercent");
            turretsList.add(villageTurret);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        //Save this block data, when the Game is saving data
        pTag.putLong("lastCensusGameTime", lastCensusGameTime);
        pTag.putBoolean("isVillage", isVillage);
        pTag.putInt("totalVillagers", totalVillagers);
        pTag.putInt("regularVillagers", regularVillagers);
        pTag.putInt("guardVillagers", guardVillagers);
        pTag.putInt("zombieVillagers", zombieVillagers);
        pTag.putInt("ironGolems", ironGolems);
        pTag.putInt("snowGolems", snowGolems);
        pTag.putInt("cats", cats);
        pTag.putInt("turrets", turrets);
        pTag.putInt("totalBeds", totalBeds);
        pTag.putInt("claimedBeds", claimedBeds);
        pTag.putInt("unclaimedBeds", unclaimedBeds);
        pTag.putInt("totalProfessionBlocks", totalProfessionBlocks);
        pTag.putInt("claimedProfessionBlocks", claimedProfessionBlocks);
        pTag.putInt("unclaimedProfessionBlocks", unclaimedProfessionBlocks);
        pTag.putInt("bells", bells);
        ListTag professionsListRaw = new ListTag();
        for (Profession curProfession : professionsList){
            CompoundTag curTag = new CompoundTag();
            curTag.putString("localizedName", curProfession.localizedName);
            curTag.putString("technicalName", curProfession.technicalName);
            curTag.putString("id", curProfession.id);
            curTag.putString("professionBlockLocalizedName", curProfession.professionBlockLocalizedName);
            curTag.putString("professionBlockId", curProfession.professionBlockId);
            curTag.putInt("professionBlocksFound", curProfession.professionBlocksFound);
            ListTag villagersListRaw = new ListTag();
            for (VillageCitizen curVillager : curProfession.villagers){
                CompoundTag curSubTag = new CompoundTag();
                curSubTag.putString("name", curVillager.name);
                curSubTag.putUUID("uuid", curVillager.uuid);
                curSubTag.putBoolean("isBaby", curVillager.isBaby);
                curSubTag.putInt("professionLvl", curVillager.professionLvl);
                curSubTag.putInt("nonFoodItensCount", curVillager.nonFoodItensCount);
                curSubTag.putInt("foodItensCount", curVillager.foodItensCount);
                curSubTag.putInt("jobX", curVillager.jobX);
                curSubTag.putInt("jobY", curVillager.jobY);
                curSubTag.putInt("jobZ", curVillager.jobZ);
                curSubTag.putInt("jobDistance", curVillager.jobDistance);
                curSubTag.putFloat("hpPercent", curVillager.hpPercent);
                villagersListRaw.add(curSubTag);
            }
            curTag.put("villagers", villagersListRaw);
            professionsListRaw.add(curTag);
        }
        pTag.put("professionsList", professionsListRaw);
        ListTag guardsListRaw = new ListTag();
        for (VillageGuard curGuard : guardsList){
            CompoundTag curTag = new CompoundTag();
            curTag.putString("name", curGuard.name);
            curTag.putUUID("uuid", curGuard.uuid);
            curTag.putString("weaponType", curGuard.weaponType);
            curTag.putString("weaponLocalizedName", curGuard.weaponLocalizedName);
            curTag.putFloat("weaponPercent", curGuard.weaponPercent);
            curTag.putString("shieldLocalizedName", curGuard.shieldLocalizedName);
            curTag.putFloat("shieldPercent", curGuard.shieldPercent);
            curTag.putString("potionLocalizedName", curGuard.potionLocalizedName);
            curTag.putInt("potionCount", curGuard.potionCount);
            curTag.putString("foodLocalizedName", curGuard.foodLocalizedName);
            curTag.putInt("foodCount", curGuard.foodCount);
            curTag.putString("helmetLocalizedName", curGuard.helmetLocalizedName);
            curTag.putFloat("helmetPercent", curGuard.helmetPercent);
            curTag.putString("chestplateLocalizedName", curGuard.chestplateLocalizedName);
            curTag.putFloat("chestplatePercent", curGuard.chestplatePercent);
            curTag.putString("leggingsLocalizedName", curGuard.leggingsLocalizedName);
            curTag.putFloat("leggingsPercent", curGuard.leggingsPercent);
            curTag.putString("bootsLocalizedName", curGuard.bootsLocalizedName);
            curTag.putFloat("bootsPercent", curGuard.bootsPercent);
            curTag.putFloat("hpPercent", curGuard.hpPercent);
            guardsListRaw.add(curTag);
        }
        pTag.put("guardsList", guardsListRaw);
        ListTag ironGolemsListRaw = new ListTag();
        for (VillageIronGolem curGolem : ironGolemsList){
            CompoundTag curTag = new CompoundTag();
            curTag.putString("name", curGolem.name);
            curTag.putUUID("uuid", curGolem.uuid);
            curTag.putFloat("hpPercent", curGolem.hpPercent);
            ironGolemsListRaw.add(curTag);
        }
        pTag.put("ironGolemsList", ironGolemsListRaw);
        ListTag snowGolemsListRaw = new ListTag();
        for (VillageSnowGolem curGolem : snowGolemsList){
            CompoundTag curTag = new CompoundTag();
            curTag.putString("name", curGolem.name);
            curTag.putUUID("uuid", curGolem.uuid);
            curTag.putFloat("hpPercent", curGolem.hpPercent);
            snowGolemsListRaw.add(curTag);
        }
        pTag.put("snowGolemsList", snowGolemsListRaw);
        ListTag turretsListRaw = new ListTag();
        for (VillageTurret curTurret : turretsList){
            CompoundTag curTag = new CompoundTag();
            curTag.putString("name", curTurret.name);
            curTag.putUUID("uuid", curTurret.uuid);
            curTag.putFloat("hpPercent", curTurret.hpPercent);
            turretsListRaw.add(curTag);
        }
        pTag.put("turretsList", turretsListRaw);

        //Repass this call to parent class of this class
        super.saveAdditional(pTag);
    }

    @Override
    public void onLoad() {
        //Repass this call for this parent class
        super.onLoad();
    }

    //Public auxiliar methods

    public void Tick(Level pLevel, BlockPos pPos, BlockState pState){
        //Nothing to do on Tick...
    }

    public void ReceivePlayerInteraction(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit){
        //If is running on Client side, stop here
        if (pLevel.isClientSide() == true)
            return;



        //Get elapsed time since the last Census
        long elapsedGameTimeSinceLastCensus = (pLevel.getGameTime() - lastCensusGameTime);
        //If was elapsed more than 24 hours of game...
        if (elapsedGameTimeSinceLastCensus >= CENSUS_INTERVAL_GAME_TICKS){
            //Update the Census
            RunNewCensus(pLevel, pPos);
            //Send the Census to the Player
            SendTheCensusToPlayer(pLevel, pPos, pPlayer);
        }
        //If was elapsed less than 24 hours of game...
        if (elapsedGameTimeSinceLastCensus < CENSUS_INTERVAL_GAME_TICKS){
            //Send the Census to the Player
            SendTheCensusToPlayer(pLevel, pPos, pPlayer);
        }

        //Warn to the server that the block was changed, and need to be saved
        setChanged(pLevel, pPos, pState);
    }

    public void RunNewCensus(Level pLevel, BlockPos pPos){
        //Get needed informations
        ServerLevel serverLevel = (ServerLevel) pLevel;
        PoiManager poiManager = serverLevel.getPoiManager();

        //Get a Dictionary of IDs of Turrets of other mods, to search
        HashMap<String, List<String>> otherModsTurretsIDs = GetHashMapOfKnownOtherModsTurretsEntitiesIDs();
        //Prepare the area of Census Research
        AABB censusResearchArea = new AABB(pPos).inflate(CENSUS_RESEARCH_RADIUS);
        //Get a List of Village Entities
        List<Villager> villagersFound = serverLevel.getEntitiesOfClass(Villager.class, censusResearchArea, Entity::isAlive);
        List<LivingEntity> guardVillagersFound = serverLevel.getEntitiesOfClass(LivingEntity.class, censusResearchArea, entity -> (new ResourceLocation("guardvillagers", "guard")).equals(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType())));
        List<ZombieVillager> zombieVillagersFound = serverLevel.getEntitiesOfClass(ZombieVillager.class, censusResearchArea, Entity::isAlive);
        List<IronGolem> ironGolemsFound = serverLevel.getEntitiesOfClass(IronGolem.class, censusResearchArea, Entity::isAlive);
        List<SnowGolem> snowGolemsFound = serverLevel.getEntitiesOfClass(SnowGolem.class, censusResearchArea, Entity::isAlive);
        List<Cat> catsFound = serverLevel.getEntitiesOfClass(Cat.class, censusResearchArea, Entity::isAlive);
        List<LivingEntity> turretsFound = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : otherModsTurretsIDs.entrySet())
            for (String item : entry.getValue())
                turretsFound.addAll(serverLevel.getEntitiesOfClass(LivingEntity.class, censusResearchArea, entity -> (new ResourceLocation(entry.getKey(), item)).equals(ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()))));
        //Get a List of Village Special Blocks
        List<BlockPos> totalBedsFound = poiManager.getInRange(type -> type.is(PoiTypes.HOME), pPos, CENSUS_RESEARCH_RADIUS, PoiManager.Occupancy.ANY).map(PoiRecord::getPos).toList();
        List<BlockPos> claimedBedsFound = poiManager.getInRange(type -> type.is(PoiTypes.HOME), pPos, CENSUS_RESEARCH_RADIUS, PoiManager.Occupancy.IS_OCCUPIED).map(PoiRecord::getPos).toList();
        List<BlockPos> unclaimedBedsFound = poiManager.getInRange(type -> type.is(PoiTypes.HOME), pPos, CENSUS_RESEARCH_RADIUS, PoiManager.Occupancy.HAS_SPACE).map(PoiRecord::getPos).toList();
        List<PoiRecord> totalProfessionBlocksFound = poiManager.getInRange(type -> isPoiTypeBindedToAnyVillagerProfession(serverLevel, type), pPos, CENSUS_RESEARCH_RADIUS, PoiManager.Occupancy.ANY).toList();
        List<PoiRecord> claimedProfessionBlocksFound = poiManager.getInRange(type -> isPoiTypeBindedToAnyVillagerProfession(serverLevel, type), pPos, CENSUS_RESEARCH_RADIUS, PoiManager.Occupancy.IS_OCCUPIED).toList();
        List<PoiRecord> unclaimedProfessionBlocksFound = poiManager.getInRange(type -> isPoiTypeBindedToAnyVillagerProfession(serverLevel, type), pPos, CENSUS_RESEARCH_RADIUS, PoiManager.Occupancy.HAS_SPACE).toList();
        List<BlockPos> totalBellsFound = poiManager.getInRange(type -> type.is(PoiTypes.MEETING), pPos, CENSUS_RESEARCH_RADIUS, PoiManager.Occupancy.ANY).map(PoiRecord::getPos).toList();
        //Detect if this is a Village
        boolean isThisVillage = ((totalBedsFound.size() >= 1 && villagersFound.size() >= 1) ? true : false);

        //Save information about the Village
        isVillage = isThisVillage;
        //Save information about generic Counters
        totalVillagers = (villagersFound.size() + guardVillagersFound.size());
        regularVillagers = villagersFound.size();
        guardVillagers = guardVillagersFound.size();
        zombieVillagers = zombieVillagersFound.size();
        ironGolems = ironGolemsFound.size();
        snowGolems = snowGolemsFound.size();
        cats = catsFound.size();
        turrets = turretsFound.size();
        totalBeds = totalBedsFound.size();
        claimedBeds = claimedBedsFound.size();
        unclaimedBeds = unclaimedBedsFound.size();
        totalProfessionBlocks = totalProfessionBlocksFound.size();
        claimedProfessionBlocks = claimedProfessionBlocksFound.size();
        unclaimedProfessionBlocks = unclaimedProfessionBlocksFound.size();
        bells = totalBellsFound.size();
        //Feed the Professions list with informations about all Villagers
        professionsList.clear();
        for (VillagerProfession registeredProfession : ForgeRegistries.VILLAGER_PROFESSIONS.getValues().stream().toList()) {
            //Instantiate a new object for this Profession
            Profession currentProfession = new Profession();
            //Fill this Profession object
            if (registeredProfession == VillagerProfession.NONE)
                currentProfession.localizedName = Component.translatable("chat.villagecensus.unemployed").getString();
            if (registeredProfession != VillagerProfession.NONE)
                currentProfession.localizedName = Component.translatable("entity.minecraft.villager." + ForgeRegistries.VILLAGER_PROFESSIONS.getKey(registeredProfession).getPath()).getString();
            currentProfession.technicalName = registeredProfession.toString();
            currentProfession.id = ForgeRegistries.VILLAGER_PROFESSIONS.getKey(registeredProfession).toString();
            if (registeredProfession != VillagerProfession.NONE && registeredProfession != VillagerProfession.NITWIT)
                ForgeRegistries.POI_TYPES.getValues().stream().filter(type -> { return ForgeRegistries.POI_TYPES.getHolder(type).map(registeredProfession.heldJobSite()::test).orElse(false); }).findFirst().ifPresent(type -> {
                    if (type.matchingStates().isEmpty() == false)
                        currentProfession.professionBlockId = ForgeRegistries.BLOCKS.getKey(type.matchingStates().iterator().next().getBlock()).toString();
                });
            if (registeredProfession == VillagerProfession.NONE || registeredProfession == VillagerProfession.NITWIT)
                currentProfession.professionBlockId = "minecraft:air";
            currentProfession.professionBlockLocalizedName = BuiltInRegistries.BLOCK.get(new ResourceLocation(currentProfession.professionBlockId)).getName().getString();
            Set<BlockState> uniqueValidBlockStatesForCurrentProfession = new HashSet<>();
            Set<TagKey<Block>> uniqueValidBlockTagsForCurrentProfession = new HashSet<>();
            ForgeRegistries.POI_TYPES.getValues().stream().filter(type -> { return ForgeRegistries.POI_TYPES.getHolder(type).map(registeredProfession.heldJobSite()::test).orElse(false); }).forEach(type -> {
                uniqueValidBlockStatesForCurrentProfession.addAll(type.matchingStates());
                for (BlockState state : type.matchingStates()){
                    String blockIdPath = ForgeRegistries.BLOCKS.getKey(state.getBlock()).getPath().toString();
                    state.getTags().forEach(tagKey -> {
                        if (tagKey.location().toString().contains(blockIdPath) == true)
                            uniqueValidBlockTagsForCurrentProfession.add(tagKey);
                    });
                }
            });
            for (PoiRecord poiRecord : totalProfessionBlocksFound){
                BlockState poiBlockState = serverLevel.getBlockState(poiRecord.getPos());
                if (uniqueValidBlockStatesForCurrentProfession.contains(poiBlockState) == true)
                    currentProfession.professionBlocksFound += 1;
                if (uniqueValidBlockStatesForCurrentProfession.contains(poiBlockState) == false)
                    if (poiBlockState.getTags().anyMatch(uniqueValidBlockTagsForCurrentProfession::contains) == true)
                        currentProfession.professionBlocksFound += 1;
            }
            List<VillageCitizen> villageCitizensWithCurrentProfession = new ArrayList<>();
            for (Villager curVillager : villagersFound)
                if (curVillager.getVillagerData().getProfession() == registeredProfession){
                    VillageCitizen villageCitizen = new VillageCitizen();
                    villageCitizen.name = Component.translatable(EntityType.VILLAGER.getDescriptionId()).getString();
                    if (curVillager.hasCustomName() == true)
                        villageCitizen.name = curVillager.getCustomName().getString();
                    villageCitizen.uuid = curVillager.getUUID();
                    villageCitizen.isBaby = curVillager.isBaby();
                    villageCitizen.professionLvl = curVillager.getVillagerData().getLevel();
                    for (int i = 0; i < curVillager.getInventory().getContainerSize(); i++)
                        if (curVillager.getInventory().getItem(i).isEmpty() == false){
                            if (curVillager.getInventory().getItem(i).isEdible() == true)
                                villageCitizen.foodItensCount += curVillager.getInventory().getItem(i).getCount();
                            if (curVillager.getInventory().getItem(i).isEdible() == false)
                                villageCitizen.nonFoodItensCount += curVillager.getInventory().getItem(i).getCount();
                        }
                    GetVillagerJobSitePosition(curVillager).ifPresent(pos -> {
                        villageCitizen.jobX = pos.getX();
                        villageCitizen.jobY = pos.getY();
                        villageCitizen.jobZ = pos.getZ();
                        villageCitizen.jobDistance = (int)(Math.sqrt(this.getBlockPos().distSqr(new BlockPos(villageCitizen.jobX, villageCitizen.jobY, villageCitizen.jobZ))));
                    });
                    villageCitizen.hpPercent = ((curVillager.getHealth() / curVillager.getMaxHealth()) * 100.0f);
                    villageCitizensWithCurrentProfession.add(villageCitizen);
                }
            currentProfession.villagers = villageCitizensWithCurrentProfession.toArray(VillageCitizen[]::new);
            professionsList.add(currentProfession);
        }
        //Feed the Guards list with information about all Guards
        guardsList.clear();
        for (LivingEntity curGuard : guardVillagersFound){
            //Instantiate a new object for this Guard
            VillageGuard currentGuard = new VillageGuard();
            //Fill this Guard object
            currentGuard.name = Component.translatable("chat.villagecensus.generic_guard").getString();
            if (curGuard.hasCustomName() == true)
                currentGuard.name = curGuard.getCustomName().getString();
            currentGuard.uuid = curGuard.getUUID();
            final boolean[] foundSword = new boolean[] { false };
            final boolean[] foundCrossbow = new boolean[] { false };
            curGuard.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
                ItemStack itemStack = inventory.getStackInSlot(5);
                if (itemStack.isEmpty() == false){
                    if (BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString().toLowerCase().contains("sword") == true)
                        foundSword[0] = true;
                    if (BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString().toLowerCase().contains("bow") == true)
                        foundCrossbow[0] = true;
                }
            });
            if (foundSword[0] == true && foundCrossbow[0] == false)
                currentGuard.weaponType = "melee";
            if (foundSword[0] == false && foundCrossbow[0] == true)
                currentGuard.weaponType = "ranged";
            if (foundSword[0] == true && foundCrossbow[0] == true)
                currentGuard.weaponType = "melee";
            if (foundSword[0] == false && foundCrossbow[0] == false)
                currentGuard.weaponType = "none";
            final String[] weaponName = new String[] { "" };
            final float[] weaponDurability = new float[] { 100.0f };
            curGuard.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
                ItemStack itemStack = inventory.getStackInSlot(5);
                if (itemStack.isEmpty() == false){
                    weaponName[0] = itemStack.getHoverName().getString();
                    if (itemStack.isDamageableItem() == true)
                        weaponDurability[0] = (((itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0f) / itemStack.getMaxDamage());
                }
            });
            currentGuard.weaponLocalizedName = weaponName[0];
            currentGuard.weaponPercent = weaponDurability[0];
            final String[] shieldName = new String[] { "" };
            final float[] shieldDurability = new float[] { 100.0f };
            final String[] potionName = new String[] { "" };
            final int[] potionCount = new int[] { 0 };
            final String[] foodName = new String[] { "" };
            final int[] foodCount = new int[] { 0 };
            curGuard.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
                ItemStack itemStack = inventory.getStackInSlot(4);
                if (itemStack.isEmpty() == false){
                    if (BuiltInRegistries.ITEM.getKey(itemStack.getItem()).toString().toLowerCase().contains("shield") == true){
                        shieldName[0] = itemStack.getHoverName().getString();
                        if (itemStack.isDamageableItem() == true)
                            shieldDurability[0] = (((itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0f) / itemStack.getMaxDamage());
                    }
                    if (itemStack.is(Items.POTION) == true || itemStack.is(Items.SPLASH_POTION) == true){
                        potionName[0] = itemStack.getHoverName().getString();
                        potionCount[0] = itemStack.getCount();
                    }
                    if (itemStack.getItem().isEdible() == true){
                        foodName[0] = itemStack.getHoverName().getString();
                        foodCount[0] = itemStack.getCount();
                    }
                }
            });
            currentGuard.shieldLocalizedName = shieldName[0];
            currentGuard.shieldPercent = shieldDurability[0];
            currentGuard.potionLocalizedName = potionName[0];
            currentGuard.potionCount = potionCount[0];
            currentGuard.foodLocalizedName = foodName[0];
            currentGuard.foodCount = foodCount[0];
            final String[] helmetName = new String[] { "" };
            final float[] helmetDurability = new float[] { 100.0f };
            curGuard.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
                ItemStack itemStack = inventory.getStackInSlot(0);
                if (itemStack.isEmpty() == false){
                    helmetName[0] = itemStack.getHoverName().getString();
                    if (itemStack.isDamageableItem() == true)
                        helmetDurability[0] = (((itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0f) / itemStack.getMaxDamage());
                }
            });
            currentGuard.helmetLocalizedName = helmetName[0];
            currentGuard.helmetPercent = helmetDurability[0];
            final String[] chestplateName = new String[] { "" };
            final float[] chestplateDurability = new float[] { 100.0f };
            curGuard.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
                ItemStack itemStack = inventory.getStackInSlot(1);
                if (itemStack.isEmpty() == false){
                    chestplateName[0] = itemStack.getHoverName().getString();
                    if (itemStack.isDamageableItem() == true)
                        chestplateDurability[0] = (((itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0f) / itemStack.getMaxDamage());
                }
            });
            currentGuard.chestplateLocalizedName = chestplateName[0];
            currentGuard.chestplatePercent = chestplateDurability[0];
            final String[] leggingsName = new String[] { "" };
            final float[] leggingsDurability = new float[] { 100.0f };
            curGuard.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
                ItemStack itemStack = inventory.getStackInSlot(2);
                if (itemStack.isEmpty() == false){
                    leggingsName[0] = itemStack.getHoverName().getString();
                    if (itemStack.isDamageableItem() == true)
                        leggingsDurability[0] = (((itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0f) / itemStack.getMaxDamage());
                }
            });
            currentGuard.leggingsLocalizedName = leggingsName[0];
            currentGuard.leggingsPercent = leggingsDurability[0];
            final String[] bootsName = new String[] { "" };
            final float[] bootsDurability = new float[] { 100.0f };
            curGuard.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(inventory -> {
                ItemStack itemStack = inventory.getStackInSlot(3);
                if (itemStack.isEmpty() == false){
                    bootsName[0] = itemStack.getHoverName().getString();
                    if (itemStack.isDamageableItem() == true)
                        bootsDurability[0] = (((itemStack.getMaxDamage() - itemStack.getDamageValue()) * 100.0f) / itemStack.getMaxDamage());
                }
            });
            currentGuard.bootsLocalizedName = bootsName[0];
            currentGuard.bootsPercent = bootsDurability[0];
            currentGuard.hpPercent = ((curGuard.getHealth() / curGuard.getMaxHealth()) * 100.0f);
            guardsList.add(currentGuard);
        }
        //Feed the Iron Golems list with information about all Golems
        ironGolemsList.clear();
        for (IronGolem curGolem : ironGolemsFound){
            //Instantiate a new object for this Iron Golem
            VillageIronGolem currentGolem = new VillageIronGolem();
            //Fill this Iron Golem object
            currentGolem.name = Component.translatable(EntityType.IRON_GOLEM.getDescriptionId()).getString();
            if (curGolem.hasCustomName() == true)
                currentGolem.name = curGolem.getCustomName().getString();
            currentGolem.uuid = curGolem.getUUID();
            currentGolem.hpPercent = ((curGolem.getHealth() / curGolem.getMaxHealth()) * 100.0f);
            ironGolemsList.add(currentGolem);
        }
        //Feed the Snow Golems list with information about all Golems
        snowGolemsList.clear();
        for (SnowGolem curGolem : snowGolemsFound){
            //Instantiate a new object for this Snow Golem
            VillageSnowGolem currentGolem = new VillageSnowGolem();
            //Fill this Snow Golem object
            currentGolem.name = Component.translatable(EntityType.SNOW_GOLEM.getDescriptionId()).getString();
            if (curGolem.hasCustomName() == true)
                currentGolem.name = curGolem.getCustomName().getString();
            currentGolem.uuid = curGolem.getUUID();
            currentGolem.hpPercent = ((curGolem.getHealth() / curGolem.getMaxHealth()) * 100.0f);
            snowGolemsList.add(currentGolem);
        }
        //Feed the Turrets list with information about all Turrets
        turretsList.clear();
        for (LivingEntity curTurret : turretsFound){
            //Instantiate a new object for this Turret
            VillageTurret currentTurret = new VillageTurret();
            //Fill this Turret object
            currentTurret.name = Component.translatable("chat.villagecensus.generic_turret").getString();
            if (curTurret.hasCustomName() == true)
                currentTurret.name = curTurret.getCustomName().getString();
            currentTurret.uuid = curTurret.getUUID();
            currentTurret.hpPercent = ((curTurret.getHealth() / curTurret.getMaxHealth()) * 100.0f);
            turretsList.add(currentTurret);
        }

        //Inform the last census update
        lastCensusGameTime = pLevel.getGameTime();
    }

    public void SendTheCensusToPlayer(Level pLevel, BlockPos pPos, Player pPlayer){
        //Play a sound effect when sending the Census
        pLevel.playSound(null, pPos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1.0f, 1.0f);

        //If is not in a Village, stop here
        if (isVillage == false){
            pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.notVillage").withStyle(ChatFormatting.RED));
            return;
        }

        //Send the cache Census relatory to the Player
        pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.relatory_start").withStyle(ChatFormatting.AQUA));
        pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.relatory_radius0", CENSUS_RESEARCH_RADIUS).withStyle(ChatFormatting.DARK_GRAY));
        pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.relatory_radius1", CENSUS_RESEARCH_RADIUS).withStyle(ChatFormatting.DARK_GRAY));
        pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.relatory_radius2", CENSUS_RESEARCH_RADIUS).withStyle(ChatFormatting.DARK_GRAY));
        pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.relatory_spacer").withStyle(ChatFormatting.DARK_GRAY));
        pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.professions").withStyle(ChatFormatting.AQUA).append(Component.literal(":")).withStyle(ChatFormatting.AQUA));
        for (Profession profession : professionsList){
            pPlayer.sendSystemMessage(Component.literal(" ").append(Component.translatable(profession.localizedName).withStyle(ChatFormatting.GREEN)).append(Component.literal(":")).withStyle(ChatFormatting.GREEN));
            if (profession.technicalName.equals("none") == false && profession.technicalName.equals("nitwit") == false)
                pPlayer.sendSystemMessage(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.translatable("chat.villagecensus.professions_blocks_count", profession.professionBlockLocalizedName).withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(": ").withStyle(ChatFormatting.WHITE)).append(Component.literal(String.valueOf(profession.professionBlocksFound)).withStyle(ChatFormatting.GRAY))
                );
            if (profession.technicalName.equals("none") == false && profession.technicalName.equals("nitwit") == false)
                pPlayer.sendSystemMessage(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.translatable("chat.villagecensus.professions_villagers").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(": ").withStyle(ChatFormatting.WHITE)).append(Component.literal(String.valueOf(profession.villagers.length)).withStyle(ChatFormatting.GRAY))
                );
            if (profession.technicalName.equals("none") == true || profession.technicalName.equals("nitwit") == true)
                pPlayer.sendSystemMessage(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)
                        .append(Component.translatable("chat.villagecensus.professions_villagers_none").withStyle(ChatFormatting.WHITE))
                        .append(Component.literal(": ").withStyle(ChatFormatting.WHITE)).append(Component.literal(String.valueOf(profession.villagers.length)).withStyle(ChatFormatting.GRAY))
                );
            MutableComponent currentLineBeingBuilded = null;
            int elementsOnLine = 0;
            for (VillageCitizen citizen : profession.villagers){
                MutableComponent hoverContent = Component.literal(citizen.name);
                if (citizen.isBaby == true)
                    hoverContent.append(Component.literal(" (")).append(Component.translatable("chat.villagecensus.professions_villager_isBaby")).append(Component.literal(")"));
                if (citizen.isBaby == false)
                    hoverContent.append(Component.literal(" (")).append(Component.translatable("chat.villagecensus.professions_villager_isAdult")).append(Component.literal(")"));
                hoverContent.append(Component.literal("\n\n")).append(Component.translatable("chat.villagecensus.professions_villager_hp").append(Component.literal(": "))
                            .append(Component.literal((int)citizen.hpPercent + "%").withStyle(ChatFormatting.GRAY)));
                if (profession.technicalName.equals("none") == false && profession.technicalName.equals("nitwit") == false){
                    hoverContent.append(Component.literal("\n")).append(Component.translatable("chat.villagecensus.professions_villager_level")).append(Component.literal(": "))
                                .append(Component.literal(String.valueOf(citizen.professionLvl)).withStyle(ChatFormatting.GRAY));
                    hoverContent.append(Component.literal("\n")).append(Component.translatable("chat.villagecensus.professions_villager_nonFood")).append(Component.literal(": "))
                                .append(Component.literal(String.valueOf(citizen.nonFoodItensCount) + "x").withStyle(ChatFormatting.GRAY));
                    hoverContent.append(Component.literal("\n")).append(Component.translatable("chat.villagecensus.professions_villager_food")).append(Component.literal(": "))
                                .append(Component.literal(String.valueOf(citizen.foodItensCount) + "x").withStyle(ChatFormatting.GRAY));
                    hoverContent.append(Component.literal("\n")).append(Component.translatable("chat.villagecensus.professions_villager_job")).append(Component.literal(": "))
                                .append(Component.literal((citizen.jobX + " " + citizen.jobY + " " + citizen.jobZ)).withStyle(ChatFormatting.GRAY));
                    hoverContent.append(Component.literal("\n")).append(Component.translatable("chat.villagecensus.professions_villager_job_dist")).append(Component.literal(": "))
                                .append(Component.literal((citizen.jobDistance + " ")).withStyle(ChatFormatting.GRAY))
                                .append(Component.translatable("chat.villagecensus.professions_villager_job_distb").withStyle(ChatFormatting.GRAY));
                }
                final ChatFormatting[] color = new ChatFormatting[]{ ChatFormatting.GRAY };
                if (citizen.hpPercent <= 80)
                    color[0] = ChatFormatting.GOLD;
                if (citizen.hpPercent <= 50)
                    color[0] = ChatFormatting.RED;
                if (currentLineBeingBuilded != null)
                    currentLineBeingBuilded = currentLineBeingBuilded.append(Component.literal(", "))
                            .append(Component.literal(citizen.name)
                            .withStyle(style -> style.withColor(color[0]).withUnderlined(false).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContent)).withClickEvent(GetVillageMemberClickEvent(citizen.uuid))));
                if (currentLineBeingBuilded == null)
                    currentLineBeingBuilded = Component.literal("  - ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.literal(citizen.name)
                            .withStyle(style -> style.withColor(color[0]).withUnderlined(false).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContent)).withClickEvent(GetVillageMemberClickEvent(citizen.uuid))));
                elementsOnLine += 1;
                if (elementsOnLine >= 6){
                    pPlayer.sendSystemMessage(currentLineBeingBuilded);
                    currentLineBeingBuilded = null;
                    elementsOnLine = 0;
                }
            }
            if (currentLineBeingBuilded != null)
                pPlayer.sendSystemMessage(currentLineBeingBuilded);
        }
        pPlayer.sendSystemMessage(Component.literal(" ").append(Component.translatable("chat.villagecensus.generic_guard_profession").withStyle(ChatFormatting.GREEN)).append(Component.literal(":")).withStyle(ChatFormatting.GREEN));
        int archersGuard = 0;
        for (VillageGuard villageGuard : guardsList)
            if (villageGuard.weaponType.equals("ranged") == true)
                archersGuard += 1;
        pPlayer.sendSystemMessage(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("chat.villagecensus.generic_guard_profession_archer").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(": ").withStyle(ChatFormatting.WHITE)).append(Component.literal(String.valueOf(archersGuard)).withStyle(ChatFormatting.GRAY))
        );
        SendGuardsListToPlayerAsMessage(pPlayer, "ranged");
        int fightersGuard = 0;
        for (VillageGuard villageGuard : guardsList)
            if (villageGuard.weaponType.equals("melee") == true)
                fightersGuard += 1;
        pPlayer.sendSystemMessage(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("chat.villagecensus.generic_guard_profession_fighter").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(": ").withStyle(ChatFormatting.WHITE)).append(Component.literal(String.valueOf(fightersGuard)).withStyle(ChatFormatting.GRAY))
        );
        SendGuardsListToPlayerAsMessage(pPlayer, "melee");
        int disarmedGuard = 0;
        for (VillageGuard villageGuard : guardsList)
            if (villageGuard.weaponType.equals("none") == true)
                disarmedGuard += 1;
        pPlayer.sendSystemMessage(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.translatable("chat.villagecensus.generic_guard_profession_disarmed").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(": ").withStyle(ChatFormatting.WHITE)).append(Component.literal(String.valueOf(disarmedGuard)).withStyle(ChatFormatting.GRAY))
        );
        SendGuardsListToPlayerAsMessage(pPlayer, "none");
        if (ironGolemsList.size() > 0){
            pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.relatory_spacer").withStyle(ChatFormatting.DARK_GRAY));
            pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.iron_golems").withStyle(ChatFormatting.AQUA).append(Component.literal(":")).withStyle(ChatFormatting.AQUA));
            pPlayer.sendSystemMessage(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal(String.valueOf(ironGolemsList.size())).withStyle(ChatFormatting.GRAY)));
            MutableComponent irongolem_currentLineBeingBuilded = null;
            int irongolem_elementsOnLine = 0;
            for (VillageIronGolem golem : ironGolemsList){
                MutableComponent hoverContent = Component.literal(golem.name);
                hoverContent.append(Component.literal("\n\n")).append(Component.translatable("chat.villagecensus.generic_health").append(Component.literal(": "))
                            .append(Component.literal((int)golem.hpPercent + "%").withStyle(ChatFormatting.GRAY)));
                final ChatFormatting[] color = new ChatFormatting[]{ ChatFormatting.GRAY };
                if (golem.hpPercent <= 80)
                    color[0] = ChatFormatting.GOLD;
                if (golem.hpPercent <= 50)
                    color[0] = ChatFormatting.RED;
                if (irongolem_currentLineBeingBuilded != null)
                    irongolem_currentLineBeingBuilded = irongolem_currentLineBeingBuilded.append(Component.literal(", "))
                            .append(Component.literal(golem.name)
                            .withStyle(style -> style.withColor(color[0]).withUnderlined(false).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContent)).withClickEvent(GetVillageMemberClickEvent(golem.uuid))));
                if (irongolem_currentLineBeingBuilded == null)
                    irongolem_currentLineBeingBuilded = Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.literal(golem.name)
                            .withStyle(style -> style.withColor(color[0]).withUnderlined(false).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContent)).withClickEvent(GetVillageMemberClickEvent(golem.uuid))));
                irongolem_elementsOnLine += 1;
                if (irongolem_elementsOnLine >= 3){
                    pPlayer.sendSystemMessage(irongolem_currentLineBeingBuilded);
                    irongolem_currentLineBeingBuilded = null;
                    irongolem_elementsOnLine = 0;
                }
            }
            if (irongolem_currentLineBeingBuilded != null)
                pPlayer.sendSystemMessage(irongolem_currentLineBeingBuilded);
        }
        if (snowGolemsList.size() > 0){
            pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.relatory_spacer").withStyle(ChatFormatting.DARK_GRAY));
            pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.snow_golems").withStyle(ChatFormatting.AQUA).append(Component.literal(":")).withStyle(ChatFormatting.AQUA));
            pPlayer.sendSystemMessage(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal(String.valueOf(snowGolemsList.size())).withStyle(ChatFormatting.GRAY)));
            MutableComponent snowgolem_currentLineBeingBuilded = null;
            int snowgolem_elementsOnLine = 0;
            for (VillageSnowGolem golem : snowGolemsList){
                MutableComponent hoverContent = Component.literal(golem.name);
                hoverContent.append(Component.literal("\n\n")).append(Component.translatable("chat.villagecensus.generic_health").append(Component.literal(": "))
                            .append(Component.literal((int)golem.hpPercent + "%").withStyle(ChatFormatting.GRAY)));
                final ChatFormatting[] color = new ChatFormatting[]{ ChatFormatting.GRAY };
                if (golem.hpPercent <= 80)
                    color[0] = ChatFormatting.GOLD;
                if (golem.hpPercent <= 50)
                    color[0] = ChatFormatting.RED;
                if (snowgolem_currentLineBeingBuilded != null)
                    snowgolem_currentLineBeingBuilded = snowgolem_currentLineBeingBuilded.append(Component.literal(", "))
                            .append(Component.literal(golem.name)
                            .withStyle(style -> style.withColor(color[0]).withUnderlined(false).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContent)).withClickEvent(GetVillageMemberClickEvent(golem.uuid))));
                if (snowgolem_currentLineBeingBuilded == null)
                    snowgolem_currentLineBeingBuilded = Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.literal(golem.name)
                            .withStyle(style -> style.withColor(color[0]).withUnderlined(false).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContent)).withClickEvent(GetVillageMemberClickEvent(golem.uuid))));
                snowgolem_elementsOnLine += 1;
                if (snowgolem_elementsOnLine >= 3){
                    pPlayer.sendSystemMessage(snowgolem_currentLineBeingBuilded);
                    snowgolem_currentLineBeingBuilded = null;
                    snowgolem_elementsOnLine = 0;
                }
            }
            if (snowgolem_currentLineBeingBuilded != null)
                pPlayer.sendSystemMessage(snowgolem_currentLineBeingBuilded);
        }
        if (turretsList.size() > 0){
            pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.relatory_spacer").withStyle(ChatFormatting.DARK_GRAY));
            pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.turrets").withStyle(ChatFormatting.AQUA).append(Component.literal(":")).withStyle(ChatFormatting.AQUA));
            pPlayer.sendSystemMessage(Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY).append(Component.literal(String.valueOf(turretsList.size())).withStyle(ChatFormatting.GRAY)));
            MutableComponent turrets_currentLineBeingBuilded = null;
            int turrets_elementsOnLine = 0;
            for (VillageTurret turret : turretsList){
                MutableComponent hoverContent = Component.literal(turret.name);
                hoverContent.append(Component.literal("\n\n")).append(Component.translatable("chat.villagecensus.generic_health").append(Component.literal(": "))
                            .append(Component.literal((int)turret.hpPercent + "%").withStyle(ChatFormatting.GRAY)));
                final ChatFormatting[] color = new ChatFormatting[]{ ChatFormatting.GRAY };
                if (turret.hpPercent <= 80)
                    color[0] = ChatFormatting.GOLD;
                if (turret.hpPercent <= 50)
                    color[0] = ChatFormatting.RED;
                if (turrets_currentLineBeingBuilded != null)
                    turrets_currentLineBeingBuilded = turrets_currentLineBeingBuilded.append(Component.literal(", "))
                            .append(Component.literal(turret.name)
                            .withStyle(style -> style.withColor(color[0]).withUnderlined(false).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContent)).withClickEvent(GetVillageMemberClickEvent(turret.uuid))));
                if (turrets_currentLineBeingBuilded == null)
                    turrets_currentLineBeingBuilded = Component.literal(" - ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.literal(turret.name)
                            .withStyle(style -> style.withColor(color[0]).withUnderlined(false).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContent)).withClickEvent(GetVillageMemberClickEvent(turret.uuid))));
                turrets_elementsOnLine += 1;
                if (turrets_elementsOnLine >= 3){
                    pPlayer.sendSystemMessage(turrets_currentLineBeingBuilded);
                    turrets_currentLineBeingBuilded = null;
                    turrets_elementsOnLine = 0;
                }
            }
            if (turrets_currentLineBeingBuilded != null)
                pPlayer.sendSystemMessage(turrets_currentLineBeingBuilded);
        }
        pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.relatory_spacer").withStyle(ChatFormatting.DARK_GRAY));
        pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.overview").withStyle(ChatFormatting.AQUA).append(Component.literal(":")).withStyle(ChatFormatting.AQUA));
        pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.total_bells_count").append(": ").append(Component.literal(String.valueOf(bells)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.total_beds_count")).append(": ").append(Component.literal(String.valueOf(totalBeds)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.claimed_beds_count")).append(": ").append(Component.literal(String.valueOf(claimedBeds)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.unclaimed_beds_count")).append(": ").append(Component.literal(String.valueOf(unclaimedBeds)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.total_profession_blocks_count")).append(": ").append(Component.literal(String.valueOf(totalProfessionBlocks)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.claimed_profession_blocks_count")).append(": ").append(Component.literal(String.valueOf(claimedProfessionBlocks)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.unclaimed_profession_blocks_count")).append(": ").append(Component.literal(String.valueOf(unclaimedProfessionBlocks)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.total_villagers_count")).append(": ").append(Component.literal(String.valueOf(totalVillagers)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.regular_villagers_count")).append(": ").append(Component.literal(String.valueOf(regularVillagers)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.guard_villagers_count")).append(": ").append(Component.literal(String.valueOf(guardVillagers)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.cats_count")).append(": ").append(Component.literal(String.valueOf(cats)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.zombie_villagers_count")).append(": ").append(Component.literal(String.valueOf(zombieVillagers)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.iron_golems_count")).append(": ").append(Component.literal(String.valueOf(ironGolems)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.snow_golems_count")).append(": ").append(Component.literal(String.valueOf(snowGolems)).withStyle(ChatFormatting.GRAY)).append(", ")
                .append(Component.translatable("chat.villagecensus.turrets_count")).append(": ").append(Component.literal(String.valueOf(turrets)).withStyle(ChatFormatting.GRAY)).append(".")
        );
        pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.relatory_spacer").withStyle(ChatFormatting.DARK_GRAY));
        long secsToNextCensus = ((CENSUS_INTERVAL_GAME_TICKS - (pLevel.getGameTime() - lastCensusGameTime)) / 20);
        long minsToNextCensus = 0;
        while (secsToNextCensus >= 60){
            minsToNextCensus += 1;
            secsToNextCensus -= 60;
            if (secsToNextCensus <= 0)
                secsToNextCensus = 0;
        }
        pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.next_relatory_in").withStyle(ChatFormatting.DARK_GRAY).append(": " + minsToNextCensus + "h and " + secsToNextCensus + "m.").withStyle(ChatFormatting.DARK_GRAY));
        pPlayer.sendSystemMessage(Component.translatable("chat.villagecensus.relatory_end").withStyle(ChatFormatting.AQUA));
    }

    //Private auxiliar methods

    private HashMap GetHashMapOfKnownOtherModsTurretsEntitiesIDs(){
        //Prepare the Dictionary to return
        HashMap<String, List<String>> toReturn = new HashMap<>();

        //Fill with the turrets of "Vouniern's Turrets"
        toReturn.put("v_turrets", new ArrayList<>());
        toReturn.get("v_turrets").add("basic_turret_t1");
        toReturn.get("v_turrets").add("basic_turret_t2");
        toReturn.get("v_turrets").add("basic_turret_t3");
        toReturn.get("v_turrets").add("laser_turret_t1");
        toReturn.get("v_turrets").add("laser_turret_t2");
        toReturn.get("v_turrets").add("laser_turret_t3");
        toReturn.get("v_turrets").add("seed_turret_t1");
        toReturn.get("v_turrets").add("seed_turret_t2");
        toReturn.get("v_turrets").add("seed_turret_t3");
        toReturn.get("v_turrets").add("sniper_turret_t1");
        toReturn.get("v_turrets").add("sniper_turret_t2");
        toReturn.get("v_turrets").add("sniper_turret_t3");

        //Return the Dictionary
        return toReturn;
    }

    private boolean isPoiTypeBindedToAnyVillagerProfession(ServerLevel serverLevel, Holder<PoiType> poiType){
        //Prepare the value to return
        boolean toReturn = false;

        //Check if this PoiType is binded to any registered Villager Profession
        if (serverLevel.registryAccess().registryOrThrow(Registries.VILLAGER_PROFESSION).stream().anyMatch(profession -> profession.heldJobSite().test(poiType)) == true)
            toReturn = true;

        //Return the value
        return toReturn;
    }

    public Optional<BlockPos> GetVillagerJobSitePosition(Villager villager) {
        //Access the Brain Memory of the Villager and get the BlockPos of the Job Site, if have
        return villager.getBrain().getMemory(MemoryModuleType.JOB_SITE).map(GlobalPos::pos);
    }

    private List<BlockPos> FindBlocksOfType(ServerLevel serverLevel, BlockPos centerPos, Block blockToFind) {
        //Prepare the List of Blocks to return
        List<BlockPos> toReturn = new ArrayList<>();

        //Set the bounds of the search
        int minX = centerPos.getX() - CENSUS_RESEARCH_RADIUS;
        int maxX = centerPos.getX() + CENSUS_RESEARCH_RADIUS;
        int minY = centerPos.getY() - CENSUS_RESEARCH_RADIUS;
        int maxY = centerPos.getY() + CENSUS_RESEARCH_RADIUS;
        int minZ = centerPos.getZ() - CENSUS_RESEARCH_RADIUS;
        int maxZ = centerPos.getZ() + CENSUS_RESEARCH_RADIUS;

        //Do iteraction in the search bound
        for (int x = minX; x <= maxX; x++)
            for (int z = minZ; z <= maxZ; z++) {
                //If the chunk here, is not loaded, stop here
                if (serverLevel.hasChunkAt(new BlockPos(x, 0, z)) == false)
                    continue;

                for (int y = minY; y <= maxY; y++) {
                    //Get the current pos of the current Block
                    BlockPos currentPos = new BlockPos(x, y, z);

                    //If the Block is the required Block, store it in the list
                    if (serverLevel.getBlockState(currentPos).is(blockToFind) == true)
                        toReturn.add(currentPos.immutable());
                }
            }

        //Return the list
        return toReturn;
    }

    private ClickEvent GetVillageMemberClickEvent(UUID uuid){
        //Return a object of Click Event, for the Member being clicked
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/villagecensus highlight " + uuid.toString());
    }

    private void SendGuardsListToPlayerAsMessage(Player pPlayer, String weaponType){
        //Send a list of Guards of a category, in the Chat, with details
        MutableComponent currentLineBeingBuilded = null;
        int elementsOnLine = 0;
        for (VillageGuard villageGuard : guardsList)
            if (villageGuard.weaponType.equals(weaponType) == true){
                MutableComponent hoverContent = Component.literal(villageGuard.name);
                hoverContent.append(Component.literal("\n\n")).append(Component.translatable("chat.villagecensus.professions_villager_hp").append(Component.literal(": "))
                            .append(Component.literal((int)villageGuard.hpPercent + "%").withStyle(ChatFormatting.GRAY)));
                if (villageGuard.weaponLocalizedName.equals("") == false)
                    hoverContent.append("\n").append(villageGuard.weaponLocalizedName + ": ").append(Component.literal(String.valueOf((int)villageGuard.weaponPercent) + "%").withStyle(ChatFormatting.GRAY));
                if (villageGuard.shieldLocalizedName.equals("") == false)
                    hoverContent.append("\n").append(villageGuard.shieldLocalizedName + ": ").append(Component.literal(String.valueOf((int)villageGuard.shieldPercent) + "%").withStyle(ChatFormatting.GRAY));
                if (villageGuard.potionLocalizedName.equals("") == false)
                    hoverContent.append("\n").append(villageGuard.potionLocalizedName + ": ").append(Component.literal(String.valueOf(villageGuard.potionCount) + "x").withStyle(ChatFormatting.GRAY));
                if (villageGuard.foodLocalizedName.equals("") == false)
                    hoverContent.append("\n").append(villageGuard.foodLocalizedName + ": ").append(Component.literal(String.valueOf(villageGuard.foodCount) + "x").withStyle(ChatFormatting.GRAY));
                if (villageGuard.helmetLocalizedName.equals("") == false || villageGuard.chestplateLocalizedName.equals("") == false || villageGuard.leggingsLocalizedName.equals("") == false || villageGuard.bootsLocalizedName.equals("") == false)
                    hoverContent.append("\n");
                if (villageGuard.helmetLocalizedName.equals("") == false)
                    hoverContent.append("\n").append(villageGuard.helmetLocalizedName + ": ").append(Component.literal(String.valueOf((int)villageGuard.helmetPercent) + "%").withStyle(ChatFormatting.GRAY));
                if (villageGuard.chestplateLocalizedName.equals("") == false)
                    hoverContent.append("\n").append(villageGuard.chestplateLocalizedName + ": ").append(Component.literal(String.valueOf((int)villageGuard.chestplatePercent) + "%").withStyle(ChatFormatting.GRAY));
                if (villageGuard.leggingsLocalizedName.equals("") == false)
                    hoverContent.append("\n").append(villageGuard.leggingsLocalizedName + ": ").append(Component.literal(String.valueOf((int)villageGuard.leggingsPercent) + "%").withStyle(ChatFormatting.GRAY));
                if (villageGuard.bootsLocalizedName.equals("") == false)
                    hoverContent.append("\n").append(villageGuard.bootsLocalizedName + ": ").append(Component.literal(String.valueOf((int)villageGuard.bootsPercent) + "%").withStyle(ChatFormatting.GRAY));
                final ChatFormatting[] color = new ChatFormatting[]{ ChatFormatting.GRAY };
                if (villageGuard.hpPercent <= 80)
                    color[0] = ChatFormatting.GOLD;
                if (villageGuard.hpPercent <= 50)
                    color[0] = ChatFormatting.RED;
                if (currentLineBeingBuilded != null)
                    currentLineBeingBuilded = currentLineBeingBuilded.append(Component.literal(", "))
                            .append(Component.literal(villageGuard.name)
                            .withStyle(style -> style.withColor(color[0]).withUnderlined(false).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContent)).withClickEvent(GetVillageMemberClickEvent(villageGuard.uuid))));
                if (currentLineBeingBuilded == null)
                    currentLineBeingBuilded = Component.literal("  - ").withStyle(ChatFormatting.DARK_GRAY)
                            .append(Component.literal(villageGuard.name)
                            .withStyle(style -> style.withColor(color[0]).withUnderlined(false).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverContent)).withClickEvent(GetVillageMemberClickEvent(villageGuard.uuid))));
                boolean addedStar = false;
                if (villageGuard.weaponPercent <= 15.0f || villageGuard.shieldPercent <= 15.0f ||
                    villageGuard.helmetPercent <= 15.0f || villageGuard.chestplatePercent <= 15.0f || villageGuard.leggingsPercent <= 15.0f || villageGuard.bootsPercent <= 15.0f)
                    if (addedStar == false){
                        currentLineBeingBuilded.append(Component.literal("*").withStyle(ChatFormatting.RED));
                        addedStar = true;
                    }
                if (villageGuard.weaponPercent <= 35.0f || villageGuard.shieldPercent <= 35.0f ||
                    villageGuard.helmetPercent <= 35.0f || villageGuard.chestplatePercent <= 35.0f || villageGuard.leggingsPercent <= 35.0f || villageGuard.bootsPercent <= 35.0f)
                    if (addedStar == false){
                        currentLineBeingBuilded.append(Component.literal("*").withStyle(ChatFormatting.GOLD));
                        addedStar = true;
                    }
                elementsOnLine += 1;
                if (elementsOnLine >= 6){
                    pPlayer.sendSystemMessage(currentLineBeingBuilded);
                    currentLineBeingBuilded = null;
                    elementsOnLine = 0;
                }
            }
        if (currentLineBeingBuilded != null)
            pPlayer.sendSystemMessage(currentLineBeingBuilded);
    }
}