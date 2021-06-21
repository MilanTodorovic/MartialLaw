//package MartialLaw.data.campaign.abilities;
//
//import MartialLaw.data.scripts.MartialLawPlugin;
//
//import java.awt.Color;
//import java.util.EnumSet;
//
//import com.fs.starfarer.api.campaign.*;
//import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
//import com.fs.starfarer.api.Global;
//import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
//import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
//import com.fs.starfarer.api.impl.campaign.ids.Commodities;
//import com.fs.starfarer.api.ui.LabelAPI;
//import com.fs.starfarer.api.ui.TooltipMakerAPI;
//import com.fs.starfarer.api.util.Misc;
//
//public class AbilityTwo extends BaseDurationAbility {
//
//    protected boolean hasEnoughMarines() {
//        return getFleet().getCargo().getMarines() > 0;
//    }
//
//    @Override
//    protected String getActivationText() {
//        if (Commodities.SUPPLIES != null && getFleet() != null
//                && (getFleet().getCargo().getSupplies() <= 0
//                || getFleet().getCargo().getFuel() >= getFleet().getCargo().getMaxFuel())) {
//            return null;
//        } else return "Siphoning Fuel";
//    }
//
//    @Override
//    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
//        Color gray = Misc.getGrayColor();
//        Color highlight = Misc.getHighlightColor();
//
//        String status = " (off)";
//        if (turnedOn) {
//            status = " (on)";
//        }
//
//        LabelAPI title = tooltip.addTitle(spec.getName() + status);
//        title.highlightLast(status);
//        title.setHighlightColor(gray);
//
//        float pad = 10f;
//        tooltip.addPara("Synthesize fuel using the trace antimatter and heavy isotopes of hydrogen found in nebulae.", pad);
//
//        if (!) {
//            tooltip.addPara("Your fleet must be within a nebula in order to siphon fuel.", Misc.getNegativeHighlightColor(), pad);
//        } else {
//            boolean inNebulaSystem = getFleet().getContainingLocation().isNebula();
//            Color clr = inNebulaSystem ? Misc.getPositiveHighlightColor() : Misc.getHighlightColor();
//            String density = (inNebulaSystem ? "high" : "low") + " density nebula";
//            String fuelPerSupply = Misc.getRoundedValueMaxOneAfterDecimal(getFuelPerSupply(inNebulaSystem));
//            String canOrIs = isActive() ? "is siphoning" : "can siphon";
//
//            tooltip.addPara("Your fleet is within a %s and " + canOrIs + " %s fuel per unit of supplies.",
//                    pad, Misc.getTextColor(), clr, density, fuelPerSupply);
//        }
//
//        tooltip.addPara("Increases the range at which the fleet can be detected by %s and consumes supplies in exchange for fuel.",
//                pad, highlight, (int)MartialLawPlugin.SENSOR_PROFILE_INCREASE_PERCENT + "%");
//
//
//        addIncompatibleToTooltip(tooltip, expanded);
//    }
//
//    @Override
//    protected void activateImpl() {
//
//    }
//
//    @Override
//    protected void applyEffect(float amount, float level) {
//        // TODO convert all marines to crew, if they ar less than the default amount ot convert
//        CampaignFleetAPI fleet = getFleet();
//
//        if (fleet == null) return;
//        if(!isActive()) return;
//
//        float days = Global.getSector().getClock().convertToDays(amount);
//        CampaignFleetAPI fleet = getFleet();
//        float marines = fleet.getCargo().getMarines();
//
//    }
//
//    @Override
//    protected void deactivateImpl() {
//
//    }
//
//    @Override
//    protected void cleanupImpl() {
//
//    }
//
//    @Override
//    public boolean isUsable() {
//        return isActive() || hasEnoughMarines();
//    }
//}
