package MartialLaw.data.campaign.abilities;

import MartialLaw.ModPlugin;
import java.awt.Color;
import java.util.List;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility ;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;


public class CrewToMarines extends BaseToggleAbility  {

    protected static float amountToUse = ModPlugin.LIGHT_ARMAMENT_PER_DAY;
    protected static String commoditiToUse = "hand_weapons2"; // light armaments

    protected boolean hasEnoughCrew() {
        return getFleet().getCargo().getCrew() - ModPlugin.UNITS_PER_DAY > getFleet().getFleetData().getMinCrew();
    }

    protected boolean hasEnoughCR() {
        return false;
    }

    protected boolean hasEnoughLigthArmaments(float amount) {
        return amount >= ModPlugin.LIGHT_ARMAMENT_PER_DAY;
    }

    protected boolean hasEnoughHeavyArmaments(float amount) {
        return amount >= ModPlugin.HEAVY_ARMAMENT_PER_DAY;
    }

    @Override
    protected String getActivationText() {
        if (getFleet() != null) {
            float heavryArmamanets = getFleet().getCargo().getCommodityQuantity("hand_weapons");
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
        tooltip.addPara("Arms crew members with Light or Heavy Armaments and converts them to Marines.", pad);

        if (!hasEnoughCR()) {
            tooltip.addPara("Your fleet must be over "+ModPlugin.MINIMUM_CR+" CR to be able to convert Crew to Marines.",
                    Misc.getNegativeHighlightColor(), pad);
        } else {
            tooltip.addPara("Your fleet is arming " + ModPlugin.UNITS_PER_DAY + " and is using" + amountToUse
                            + " " + commoditiToUse + " per day. Combat readiness degrading by "
                            + Float.toString(ModPlugin.COMBAT_READINESS_LOSS_PER_DAY) + " per day.",
                    pad, Misc.getTextColor(), Misc.getPositiveHighlightColor());
        }

        tooltip.addPara("Lowers the maximum burn level at which the fleet can move by %s.",
                pad, highlight, String.valueOf(ModPlugin.BURN_LEVEL_REDUCED));

        addIncompatibleToTooltip(tooltip, expanded);
    }

    @Override
    public boolean showActiveIndicator() {
        return isActive();
    }

    @Override
    protected void activateImpl() {

    }

    @Override
    protected void applyEffect(float amount, float level) {
        // TODO remove crew, add marines, reduce burn level, reduce combat readiness, use materials
        CampaignFleetAPI fleet = getFleet();

        if (fleet == null) return;
        if (!isActive()) return;

        float maxBurnLevel = fleet.getFleetData().getMaxBurnLevel();
        fleet.getStats().getFleetwideMaxBurnMod().modifyFlat(getModId(), maxBurnLevel - ModPlugin.BURN_LEVEL_REDUCED, "Militarization of crew members");
        float days = Global.getSector().getClock().convertToDays(amount);


        float minCrew = fleet.getFleetData().getMinCrew();
        float crew = fleet.getCargo().getCrew();

        float lightArmament = fleet.getCargo().getCommodityQuantity("hand_weapons2");
        float heavyArmament = fleet.getCargo().getCommodityQuantity("hand_weapons");
        boolean hasLigthArmament = hasEnoughLigthArmaments(lightArmament);
        boolean hasHeavyArmament = hasEnoughHeavyArmaments(heavyArmament);

        List<FleetMemberAPI> ships = fleet.getFleetData().getCombatReadyMembersListCopy();
        for (FleetMemberAPI ship : ships){
            ship.getStats();
        }

        if (crew <= minCrew || (crew - ModPlugin.UNITS_PER_DAY) <= minCrew) {
            deactivate();
            getFleet().addFloatingText("Not enough crew members.", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
        } else if (!hasLigthArmament && heavyArmament == 0) {
            deactivate();
            getFleet().addFloatingText("Not enough light/heavy armament.", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
        } else if (!hasHeavyArmament && lightArmament == 0) {
            deactivate();
            getFleet().addFloatingText("Not enough light/heavy armament.", Misc.setAlpha(entity.getIndicatorColor(), 255), 0.5f);
        } else {
            // all clear and ready to go
            // prefer Light Armaments
            if (hasLigthArmament) {
                commoditiToUse = "hand_weapons2";
                amountToUse = ModPlugin.LIGHT_ARMAMENT_PER_DAY;
            } else if (hasHeavyArmament) {
                commoditiToUse = "hand_weapons";
                amountToUse = ModPlugin.HEAVY_ARMAMENT_PER_DAY;
            }
            // only when a full day has passed since the skill has been activated
            if (true) {
                Global.getSector().getCampaignUI().addMessage(
                        "Days and activationdays: %s,%s",
                        Misc.getTextColor(),
                        String.valueOf(days), // highlight 1
                        String.valueOf(getActivationDays()), // highlight 2
                        Misc.getHighlightColor(),
                        Misc.getHighlightColor()
                );
                // get the fleet member's repair tracker, the CR-related methods are there
                fleet.getCargo().removeCommodity(commoditiToUse, amountToUse);
                fleet.getCargo().removeCrew(ModPlugin.UNITS_PER_DAY);
                fleet.getCargo().addMarines(ModPlugin.UNITS_PER_DAY);
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
        return isActive() || hasEnoughCrew();
    }
}
