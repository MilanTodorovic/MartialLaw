package MartialLaw.data.campaign.abilities;

import MartialLaw.data.scripts.MartialLawPlugin;

import java.awt.Color;
import java.util.List;

import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class ArmedCrew extends BaseDurationAbility {

    protected static float amountToUse = MartialLawPlugin.LIGHT_ARMAMENT;
    protected static String commodityToUse = "hand_weapons2"; // light armaments

//    protected boolean hasEnoughCrew() {
//        return getFleet().getCargo().getCrew() - MartialLawPlugin.UNITS > getFleet().getFleetData().getMinCrew();
//    }

    protected boolean hasEnoughCR(List<FleetMemberAPI> ships) {
        for (FleetMemberAPI ship : ships) {
            float CR = ship.getRepairTracker().getCR();
            if (CR < MartialLawPlugin.MINIMUM_CR) {
                return false;
            }
        }
        return true;
    }

    protected boolean hasEnoughLigthArmaments(float amount) {
        return amount >= MartialLawPlugin.LIGHT_ARMAMENT;
    }

    protected boolean hasEnoughHeavyArmaments(float amount) {
        return amount >= MartialLawPlugin.HEAVY_ARMAMENT;
    }

    @Override
    protected String getActivationText() {
        if (getFleet() != null) {
            // TODO future reference: at this time, 'hand_weapons' are 'Heavy Armaments' using the picture 'heavyweapons.png'
            float heavryArmamanets = getFleet().getCargo().getCommodityQuantity("hand_weapons");
            // hand_weapons2 are my own commodity
            float lightArmaments = getFleet().getCargo().getCommodityQuantity("hand_weapons2");
            if (hasEnoughHeavyArmaments(heavryArmamanets) || hasEnoughLigthArmaments(lightArmaments)) {
                return "Undergoing militarization of crew members.";
            } else {
                return null;
            }
        } else {
            return null;
        }
    }


    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();

        String status = " (off)";
        if (turnedOn) {
            status = " (on)";
        }

        LabelAPI title = tooltip.addTitle(spec.getName() + status);
        title.highlightLast(status);
        title.setHighlightColor(gray);

        float pad = 10f;
        tooltip.addPara("Arms Crew with Light or Heavy Armaments and converts them into Armed Crew.", pad);
        // TODO this excludes scuttled ships. Disallow it?
        List<FleetMemberAPI> ships = getFleet().getFleetData().getCombatReadyMembersListCopy();
        if (!hasEnoughCR(ships)) {
            tooltip.addPara("Your fleet must be over " + MartialLawPlugin.MINIMUM_CR + " CR to be able to convert Crew to Armed Crew.",
                    Misc.getNegativeHighlightColor(), pad);
        } else {
            tooltip.addPara("Your fleet is arming " + MartialLawPlugin.UNITS + " crew members and is using" + amountToUse
                            + " " + commodityToUse + ". Combat readiness degraded by "
                            + Float.toString(MartialLawPlugin.COMBAT_READINESS_LOSS) + ".",
                    pad, Misc.getTextColor(), Misc.getPositiveHighlightColor());
        }

        tooltip.addPara("Lowers the maximum burn level at which the fleet can move by %s.",
                pad, highlight, String.valueOf(MartialLawPlugin.BURN_LEVEL_REDUCED));

        addIncompatibleToTooltip(tooltip, expanded);
    }

    @Override
    public boolean showActiveIndicator() {
        return isActive();
    }

    @Override
    protected void activateImpl() {
        float maxBurnLevel = getFleet().getFleetData().getMaxBurnLevel();
        getFleet().getStats().getFleetwideMaxBurnMod().modifyFlat(getModId(), maxBurnLevel - MartialLawPlugin.BURN_LEVEL_REDUCED, "Militarization of crew members");
    }

    @Override
    protected void applyEffect(float amount, float level) {

        CampaignFleetAPI fleet = getFleet();

        if (fleet == null) return;
        if (!isActive()) return;

        //float days = Global.getSector().getClock().convertToDays(amount);

        float minCrew = fleet.getFleetData().getMinCrew();
        float crew = fleet.getCargo().getCrew();
        // hand_weapons2 are my own commodity
        float lightArmament = fleet.getCargo().getCommodityQuantity("hand_weapons2");
        // TODO future reference: at this time, 'hand_weapons' are 'Heavy Armaments' using the picture 'heavyweapons.png'
        float heavyArmament = fleet.getCargo().getCommodityQuantity("hand_weapons");
        boolean hasLigthArmament = hasEnoughLigthArmaments(lightArmament);
        boolean hasHeavyArmament = hasEnoughHeavyArmaments(heavyArmament);

        // Get all ships from the fleet and check their CR
        // TODO this excludes scuttled ships. Disallow it?
        List<FleetMemberAPI> ships = fleet.getFleetData().getCombatReadyMembersListCopy();
        boolean result = hasEnoughCR(ships);
        if (!result) {
            deactivate();
            getFleet().addFloatingText("Not enough CR. Some ship(s) have less CR than the minimum required (" + MartialLawPlugin.MINIMUM_CR + "%).",
                    Misc.setAlpha(entity.getIndicatorColor(), 255), 0.8f);
        } else {
            for (FleetMemberAPI ship : ships) {
                float CR = ship.getRepairTracker().getCR();
                ship.getRepairTracker().applyCREvent(CR - MartialLawPlugin.COMBAT_READINESS_LOSS, "ml_armed_crew", "Militarizing crew members");
//                ship.getRepairTracker().setCR(CR - MartialLawPlugin.COMBAT_READINESS_LOSS);
            }

//            if (hasEnoughCrew()) {
//                deactivate();
//                getFleet().addFloatingText("Not enough crew members.", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
            if (!hasLigthArmament && heavyArmament == 0) {
                deactivate();
                getFleet().addFloatingText("Not enough light/heavy armament.", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
            } else if (!hasHeavyArmament && lightArmament == 0) {
                deactivate();
                getFleet().addFloatingText("Not enough light/heavy armament.", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
            } else {
                // all clear and ready to go
                // prefer Light Armaments
                if (hasLigthArmament) {
                    commodityToUse = "hand_weapons2";
                    amountToUse = MartialLawPlugin.LIGHT_ARMAMENT;
                } else if (hasHeavyArmament) {
                    // TODO future reference: at this time, 'hand_weapons' are 'Heavy Armaments' using the picture 'heavyweapons.png'
                    commodityToUse = "hand_weapons";
                    amountToUse = MartialLawPlugin.HEAVY_ARMAMENT;
                }

                fleet.getCargo().removeCommodity(commodityToUse, amountToUse);
                fleet.getCargo().removeCrew(MartialLawPlugin.UNITS);
                fleet.getCargo().addCommodity("armed_crew", amountToUse);
            }
        }
    }

    @Override
    protected void deactivateImpl() {
        cleanupImpl();
    }

    @Override
    protected void cleanupImpl() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;
        fleet.getStats().getFleetwideMaxBurnMod().unmodify(getModId());
    }

    @Override
    public boolean hasTooltip() {
        return true;
    }

    @Override
    public boolean isUsable() {
        return isActive();
    }
}
