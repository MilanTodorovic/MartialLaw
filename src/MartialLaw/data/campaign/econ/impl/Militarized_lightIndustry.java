package MartialLaw.data.campaign.econ.impl;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CargoStackAPI;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.listeners.EconomyTickListener;
import com.fs.starfarer.api.impl.campaign.econ.impl.BaseIndustry;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Pair;
import org.apache.log4j.Logger;

import java.awt.Color;

public class Militarized_lightIndustry extends BaseIndustry implements EconomyTickListener {

    public static final Logger log = Global.getLogger(Militarized_lightIndustry.class);

    private static final int ALPHA_CORE_SALVAGE_POINT_VAL = 40;
    private static final int BASE_SALVAGE_POINT_VAL = 50;
    private static final float ALPHA_CORE_DECAY_MULT = 0.6F;
    private static final float BETA_CORE_QUALITY_BONUS = 0.2F;
    private static final float GAMMA_CORE_DECAY_MULT = 0.7F;
    private static final float BASE_DECAY_MULT = 0.5F;


    private int outputSalvagePointValue = 50;
    private boolean hasFinishedIntelArchive = false;

    private float outputDecayMult = 0.5F;

    public void apply() {
        super.apply(true);

        if (!market.isPlayerOwned()) {
            AImode();
            return;
        }

        Global.getSector().getListenerManager().addListener(this, true);

        market.getStats().getDynamic().getMod("production_quality_mod").modifyFlatAlways(getModId(), -0.20F, getNameForModifier());

        int hullOutput = getHullOutput();

        demand(Commodities.HEAVY_MACHINERY, 1);
        demand(Commodities.METALS, 3);
        demand(Commodities.TAG_CREW, 2);
        supply("hand_weapons2", 2);
        supply("armed_crew", 1);

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.HEAVY_MACHINERY);
        applyDeficitToProduction(1, deficit, Commodities.SHIPS, Commodities.METALS);

        if (!isFunctional()) {
            supply.clear();
            demand.clear();
        } else {
            modifyPlayerCustomProduction(getHullOutput());
            applyListenerToFleetsInSystem();
        }

        addSharedSubmarket();
    }

    private void modifyPlayerCustomProduction(int mod) {
        int max = 0;

        for (Industry ind : market.getIndustries()){
            if (ind.getId().equals(getId())) continue;
            int i = ind.getSupply(Commodities.SHIPS).getQuantity().getModifiedInt();
            if(i > max) max = i;
        }

        //if there is another exporter, use whatever value is smaller to mod the budget
        if(max > 0){
            Global.getSector().getPlayerStats().getDynamic().getMod("custom_production_mod").modifyFlatAlways(getModId(), Math.min(max, mod) * 25000f, market.getName() + " " + getNameForModifier());
        }
    }

    private void unmodifyPlayerCustomProduction() {
        Global.getSector().getPlayerStats().getDynamic().getMod("custom_production_mod").unmodify(getModId());
    }


    public void unapply() {
        super.unapply();

        market.getStats().getDynamic().getMod("production_quality_mod").unmodifyFlat(getModId());

        if (!market.isPlayerOwned()) return;

        removeSharedSubmarket();
        unmodifyPlayerCustomProduction();
        Global.getSector().getListenerManager().removeListener(this);
    }

    public void AImode() {
        int size = market.getSize();
        market.getStats().getDynamic().getMod("production_quality_mod").modifyFlatAlways(getModId(), -0.20F, getNameForModifier());

        demand(Commodities.HEAVY_MACHINERY, size - 2);
        supply(Commodities.SHIPS, size - 2);
        supply(Commodities.METALS, size);
        supply(IndEvo_Items.PARTS, size -1);

        Pair<String, Integer> deficit = getMaxDeficit(Commodities.HEAVY_MACHINERY);
        applyDeficitToProduction(1, deficit, Commodities.SHIPS, Commodities.METALS);

        if (!isFunctional()) {
            supply.clear();
            demand.clear();
        }
    }

    private void applyListenerToFleetsInSystem() {
        //throw all current ships in the system on a list
        List<CampaignFleetAPI> allFleets = new ArrayList<>(this.market.getStarSystem().getFleets());

        for (CampaignFleetAPI fleet : allFleets) {

            //Skip LP smugglers to avoid vanilla "ConcurrentModificationException" bug
            boolean b = false;
            for (FleetEventListener e : fleet.getEventListeners()) {
                if (e instanceof LuddicPathCellsIntel) {
                    b = true;
                    break;
                }
            }
            if (b) {
                continue;
            }

            //if the ship is not on the fleetsizelist, apply listener and add
            if (!fleetSizeList.containsKey(fleet)) {
                fleet.addEventListener(this);
                fleetSizeList.put(fleet, fleet.getFleetPoints());

                //if it is, but the size of the fleet has increased - adjust size
            } else if (fleetSizeList.containsKey(fleet) && fleetSizeList.get(fleet) < fleet.getFleetPoints()) {
                fleetSizeList.put(fleet, fleet.getFleetPoints());
            }
        }
    }

    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {

        //add remaining points to SalvagePoints if destroyed by battle
        if (fleet != null && fleetSizeList.containsKey(fleet) && reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
            int amount = fleetSizeList.get(fleet);
            availableSalvagePoints += amount;
            fleetSizeList.remove(fleet);

            return;
        }

        if (fleet != null && fleetSizeList.containsKey(fleet)) {
            fleetSizeList.remove(fleet);
        }
    }

    //if a fleet loses FP through battle, add those FP to salvagePoints
    public void reportBattleOccurred(CampaignFleetAPI fleet, CampaignFleetAPI primaryWinner, BattleAPI battle) {

        if (fleet != null && fleetSizeList.containsKey(fleet) && fleet.getFleetPoints() < fleetSizeList.get(fleet)) {
            availableSalvagePoints += fleetSizeList.get(fleet) - fleet.getFleetPoints();
            //update fleet point count
            fleetSizeList.put(fleet, fleet.getFleetPoints());
        }
    }

    //take weapons from storage, convert to SP every day
    //define SP/weapons (cargo space?)
    //define limit
    //take a certain amount of SP, then calculate the required weapons to fill it

    //max amoutn market size * 50
    //50 x marketSize
    //weapon yould give SP according to size? cargo space x2

    //check how many SP are needed per day, take from cargo until filled or until limit is reached

    private static final int WEAPON_SP_MONTH_LIMIT = 50;
    private static final int WEAPON_SP_CARGO_SPACE_MULT = 2;
    private int currentWeaponBonusSP = 0;

    private int getWeaponSPMonthLimit() {
        return market.getSize() * WEAPON_SP_MONTH_LIMIT;
    }

    private int getWeaponSPDayLimit() {
        int dayLimit = Math.round(getWeaponSPMonthLimit() * 1f / IndEvo_IndustryHelper.getDaysOfCurrentMonth());
        int monthLimit = getWeaponSPMonthLimit();

        if (monthLimit - currentWeaponBonusSP > 0) {
            if (currentWeaponBonusSP + dayLimit > monthLimit) {
                return currentWeaponBonusSP + dayLimit - monthLimit;
            } else return dayLimit;
        } else return 0;
    }

    private float getSPPerUnit(CargoStackAPI stack) {
        return !stack.isWeaponStack() ? 0f : stack.getCargoSpacePerUnit() * WEAPON_SP_CARGO_SPACE_MULT;
    }

    private void autoFeed() {
        //autofeed
        if (!market.hasSubmarket(IndEvo_ids.SHAREDSTORAGE) || getWeaponSPMonthLimit() - currentWeaponBonusSP <= 0)
            return;
        CargoAPI cargo = market.getSubmarket(IndEvo_ids.SHAREDSTORAGE).getCargo();

        int dayLimit = getWeaponSPDayLimit();
        int added = 0;

        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            if (added >= dayLimit) break;

            if (stack.isWeaponStack()) {
                float stackTotalUnits = stack.getSize();
                float spPerUnit = getSPPerUnit(stack);
                int requiredUnits = (int) Math.ceil((dayLimit - added) / spPerUnit);
                float toAdd = 0;

                if (stackTotalUnits <= requiredUnits) {
                    toAdd = stackTotalUnits * spPerUnit;
                    cargo.removeStack(stack);
                } else {
                    toAdd = requiredUnits * spPerUnit;
                    cargo.removeWeapons(stack.getWeaponSpecIfWeapon().getWeaponId(), requiredUnits);
                }

                toAdd = Math.round(toAdd);

                availableSalvagePoints += toAdd;
                currentWeaponBonusSP += toAdd;
                added += toAdd;
            }
        }
    }

    private int getHullOutput() {
        if (!isFunctional()) {
            return 0;
        }

        int size = market.getSize() + 3;
        int hullOutput = availableSalvagePoints / outputSalvagePointValue;
        if (hullOutput > size) {
            hullOutput = size;
        }

        return hullOutput;
    }

    public void reportEconomyTick(int iterIndex) {
    }

    public void reportEconomyMonthEnd() {
        currentWeaponBonusSP = 0;

        if (!isFunctional()) return;

        availableSalvagePoints *= outputDecayMult;

        if (market.hasIndustry(getId()) && getHullOutput() <= market.getSize() / 3) {
            String s = IndEvo_StringHelper.getStringAndSubstituteToken(getId(), "lowMaterial", "$marketName", market.getName());
            String s1 = IndEvo_StringHelper.getString(getId(), "lowMaterialAdd");

            MessageIntel intel = new MessageIntel(s, Misc.getTextColor());
            intel.addLine(s1, Misc.getNegativeHighlightColor());
            intel.setIcon(Global.getSettings().getSpriteName("IndEvo", "notification"));
            Global.getSector().getCampaignUI().addMessage(intel, CommMessageAPI.MessageClickAction.COLONY_INFO, market);
        }
    }

    private boolean hasPirateActivity(MarketAPI market) {
        boolean hasActivity = false;
        if (market.hasCondition(Conditions.PIRATE_ACTIVITY)) {
            hasActivity = true;
        }
        return hasActivity;
    }

    private boolean systemHasPirateActivity() {

        List<MarketAPI> MarketsInSystem = IndEvo_IndustryHelper.getMarketsInLocation(market.getStarSystem());
        boolean hasActivity = false;
        for (MarketAPI Market : MarketsInSystem) {
            if (hasPirateActivity(Market)) {
                hasActivity = true;
                break;
            }
        }
        return hasActivity;
    }

    //Archives all intel that has finished up to now, so it doesn't trigger for this industry
    public void IntelArchive() {

        List<IntelInfoPlugin> thisSystemIntelList = new ArrayList<>(Global.getSector().getIntelManager().getIntel());
        for (IntelInfoPlugin intel : thisSystemIntelList) {
            if (intel instanceof RaidIntel && (market.getStarSystem() == ((RaidIntel) intel).getSystem()) && ((RaidIntel) intel).isEnded()) {
                finishedIntel.add(intel.getClass().hashCode());
            }
        }
        hasFinishedIntelArchive = true;
    }

    @Override
    public void onNewDay() {
        if (!hasFinishedIntelArchive) {
            IntelArchive();
        }

        autoFeed();

        //get all intel
        List<IntelInfoPlugin> allIntel = new ArrayList<>(Global.getSector().getIntelManager().getIntel(RaidIntel.class));
        for (IntelInfoPlugin intel : allIntel) {
            //check if it's raidIntel and targets this system
            if (intel instanceof RaidIntel && (market.getStarSystem() == ((RaidIntel) intel).getSystem()) && !finishedIntel.contains(intel.getClass().hashCode())) {
                //if its ended and succeeded, give player 30% raid FP
                int salvagePoints = 0;
                String msg = null;

                if (((RaidIntel) intel).isSucceeded()) {
                    salvagePoints = (int) (((RaidIntel) intel).getRaidFPAdjusted() * 0.30F);
                    msg = "incursionSuccess";
                    //Else if its ended and failed in the action stage, and the player has not visited for the duration of the action stage
                } else if (((RaidIntel) intel).isFailed()
                        && ((RaidIntel) intel).getFailStage() == ((RaidIntel) intel).getStageIndex(((RaidIntel) intel).getActionStage())
                        && market.getStarSystem().getDaysSinceLastPlayerVisit() >= ((RaidIntel) intel).getActionStage().getElapsed()) {
                    salvagePoints = (int) (((RaidIntel) intel).getRaidFPAdjusted() * 0.70F);
                    msg = "incursionFailed";
                }

                if (msg != null) {
                    availableSalvagePoints += salvagePoints;
                    finishedIntel.add(intel.getClass().hashCode());
                    String name = market.getStarSystem().getName();

                    msg = IndEvo_StringHelper.getStringAndSubstituteToken(getId(), msg, "$systemName", name);

                    Global.getSector().getCampaignUI().addMessage(msg,
                            Misc.getTextColor(),
                            name,
                            salvagePoints + "",
                            Misc.getNegativeHighlightColor(),
                            Misc.getHighlightColor());
                }
            }
        }

        //As long as the market has pirate activity, add a flat amount of FP to the counter every day
        if (systemHasPirateActivity()) {
            availableSalvagePoints += 3;
        }
    }

    @Override
    protected void addRightAfterDescriptionSection(TooltipMakerAPI tooltip, IndustryTooltipMode mode) {

        if (!isBuilding() && currTooltipMode != IndustryTooltipMode.ADD_INDUSTRY && market.isPlayerOwned()) {
            float opad = 5.0F;

            if (this.isFunctional()) {
                if (market.isPlayerOwned() || currTooltipMode == IndustryTooltipMode.NORMAL) {
                    tooltip.addPara(IndEvo_StringHelper.getString(getId(), "sUnitsAvailableTooltip"),
                            opad, Misc.getHighlightColor(), new String[]{"" + availableSalvagePoints, "" + outputSalvagePointValue});
                }
            }
        }
    }

//Building checks and tooltips

    @Override
    public boolean isAvailableToBuild() {
        return Global.getSettings().getBoolean("ScrapYard") && isOnlyInstanceInSystem() && super.isAvailableToBuild();
    }

    @Override
    public boolean showWhenUnavailable() {
        return Global.getSettings().getBoolean("ScrapYard");
    }

    private boolean isOnlyInstanceInSystem() {
        return IndEvo_IndustryHelper.isOnlyInstanceInSystemExcludeMarket(getId(), market.getStarSystem(), market, market.getFaction());
    }

    @Override
    public String getUnavailableReason() {
        if (!isOnlyInstanceInSystem()) {
            return IndEvo_StringHelper.getString(getId(), "unavailableReason");
        } else {
            return super.getUnavailableReason();
        }
    }

    protected void addAlphaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();
        String suffix = mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP ? "Short" : "Long";
        String pre = IndEvo_StringHelper.getString("IndEvo_AICores", "aCoreAssigned" + suffix);
        String effect = IndEvo_StringHelper.getString(getId(), "aCoreEffect");
        String[] highlightString = new String[]{ALPHA_CORE_SALVAGE_POINT_VAL - BASE_SALVAGE_POINT_VAL + "", IndEvo_StringHelper.getAbsPercentString(ALPHA_CORE_DECAY_MULT, true)};

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + effect, 0.0F, highlight, highlightString);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + effect, opad, highlight, highlightString);
        }
    }

    protected void addBetaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();

        String suffix = mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP ? "Short" : "Long";
        String pre = IndEvo_StringHelper.getString("IndEvo_AICores", "bCoreAssigned" + suffix);
        String effect = IndEvo_StringHelper.getString(getId(), "bCoreEffect");
        String highlightString = "";

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + effect, 0.0F, highlight, highlightString);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + effect, opad, highlight, highlightString);
        }
    }

    protected void addGammaCoreDescription(TooltipMakerAPI tooltip, AICoreDescriptionMode mode) {
        float opad = 10.0F;
        Color highlight = Misc.getHighlightColor();

        String suffix = mode == AICoreDescriptionMode.MANAGE_CORE_DIALOG_LIST || mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP ? "Short" : "Long";
        String pre = IndEvo_StringHelper.getString("IndEvo_AICores", "gCoreAssigned" + suffix);
        String effect = IndEvo_StringHelper.getString(getId(), "gCoreEffect");
        String highlightString = IndEvo_StringHelper.getAbsPercentString(GAMMA_CORE_DECAY_MULT, true);

        if (mode == AICoreDescriptionMode.INDUSTRY_TOOLTIP) {
            CommoditySpecAPI coreSpec = Global.getSettings().getCommoditySpec(aiCoreId);
            TooltipMakerAPI text = tooltip.beginImageWithText(coreSpec.getIconName(), 48.0F);
            text.addPara(pre + effect, 0.0F, highlight, highlightString);
            tooltip.addImageWithText(opad);
        } else {
            tooltip.addPara(pre + effect, opad, highlight, highlightString);
        }
    }

    protected void applyAlphaCoreModifiers() {
        outputDecayMult = ALPHA_CORE_DECAY_MULT;
        outputSalvagePointValue = ALPHA_CORE_SALVAGE_POINT_VAL;
    }

    protected void applyBetaCoreModifiers() {
        market.getStats().getDynamic().getMod("production_quality_mod").modifyFlat("ScrapYardsAICore", BETA_CORE_QUALITY_BONUS);
    }

    @Override
    protected void applyGammaCoreModifiers() {
        outputDecayMult = GAMMA_CORE_DECAY_MULT;
    }

    @Override
    protected void applyNoAICoreModifiers() {
        outputSalvagePointValue = BASE_SALVAGE_POINT_VAL;
        outputDecayMult = BASE_DECAY_MULT;
        market.getStats().getDynamic().getMod("production_quality_mod").unmodifyFlat("ScrapYardsAICore");
    }

    protected void updateAICoreToSupplyAndDemandModifiers() {
    }

    protected void applyAICoreToIncomeAndUpkeep() {
    }

    @Override
    public boolean isLegalOnSharedSubmarket(CargoStackAPI stack) {
        return stack.isWeaponStack();
    }

    @Override
    public void addTooltipLine(TooltipMakerAPI tooltip, boolean expanded) {
        tooltip.addPara("Salvage Yards: disassembles %s in this storage to generate Salvage Points.", 10f, Misc.getHighlightColor(), "weapons");
    }
}