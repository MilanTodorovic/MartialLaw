package MartialLaw.data.scripts;

import MartialLaw.data.campaign.ids.MartialLawHullmods;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import MartialLaw.data.campaign.ids.MartialLawCommodities;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.util.Misc;
// Saving this for a future game update when we get a `Crew API`
//import com.fs.starfarer.api.fleet.CrewCompositionAPI;
//import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;

import java.util.*;


public class MartialLawEveryFrameScript implements EveryFrameScript {

    public static int daySinceLastMutiny = 0;
    public static int currentDay = 0;
    public boolean firstTick = true;
    public static int loyaltyHullmods = 0;
//    public static Map<String, List<Float>> shipCrewCompistion = new HashMap<String, List<Float>>();

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return false;
    }

    @Override
    public void advance(float amount) {
        if (newDay()) {
            for (FleetMemberAPI ship : Global.getSector().getPlayerFleet().getFleetData().getCombatReadyMembersListCopy()) {
                // TODO choose random ships up to 3 to be victims
                //  should include parameters like hullsize, d-mods, s-mods, Crew loyalty mod
                if (ship.getHullSpec().getBuiltInMods().contains(MartialLawHullmods.CREW_LOYALTY)) {
                    // TODO maybe add a listener to hullmod changes and increment a global variable?
                    //  also take notes of lost/sold ships and deduct them
                    // TODO each ship hull contributes a different amount
                    loyaltyHullmods += 1;
                }
                // TODO Currently doesn't mean anything, since I can't filter by special type
//                final CrewCompositionAPI CC = ship.getCrewComposition();
//                shipCrewCompistion.put(ship.getShipName(), new ArrayList<Float>() {
//                    {
//                        add(CC.getCrew());
//                        add(CC.getMarines());
//                        add(3f);
//                    }
//                });
            }
//            // TODO how to distribute?
//            if (shouldAMutinyOccur) {
//                startMutiny();
//            }
            // For testing purposes, each day start a mutiny
            if (true) {
                // TODO disable mutinies for first few cycles? don't make it a problem from the start
                //  maybe add a similar mechanic like Tortuga? lost or avoided (1 story point evasion) battles contribute to crew dissatisfaction, increases mutiny chance
                Global.getSector().getCampaignUI().addMessage("Starting mutiny.");
                startMutiny();
            } else {
                daySinceLastMutiny += 1;
            }
        }
    }

    // Borrowed from Industrial.Evolution mod (IndEvo_TimeTracker.java)
    protected boolean newDay() {
        CampaignClockAPI clock = Global.getSector().getClock();
        if (firstTick) {
            currentDay = clock.getDay();
            firstTick = false;
            return false;
        } else if (clock.getDay() != currentDay) {
            currentDay = clock.getDay();
            return true;
        }
        return false;
    }

    protected boolean shouldAMutinyOccur(){
//        double chance = Math.random();
//        MartialLawPlugin.MUTINY_BASE_MULTIPLAYER
//        daySinceLastMutiny
//        MartialLawPlugin.MUTINY_PER_DAY_MULTIPLAYER
//        MartialLawPlugin.CREW_LOYALTY_BONUS
//        loyaltyHullmods
//        daySinceLastMutiny
//        MartialLawPlugin.MUTINY_GRACE_PERIOD_DAYS
        return true;
    }

    protected void startMutiny() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        float armedCrewOnBoard = fleet.getCargo().getCommodityQuantity(MartialLawCommodities.ARMED_CREW);
        float crewOnBoard = fleet.getCargo().getCommodityQuantity(Commodities.CREW);
        float marinesOnBoard = fleet.getCargo().getCommodityQuantity(Commodities.MARINES);

//        if (armedCrewOnBoard > crewOnBoard+marinesOnBoard){
        if (true){
            // one-sided fight
            // TODO take a percentage of the rebels and move them to the defenders side (loyalty bonus)
            //  marines are 90% loyal, crew 75%, armed crew 55%
            //  1 armed crew = 2 crew = 0,7 marine
            Global.getSector().getCampaignUI().addMessage(
                    "Mutiny has started: Crew %s, %s",
                    Misc.getTextColor(),
                    String.valueOf(crewOnBoard), // highlight 1
                    String.valueOf(String.format("Armed Crew %s, Marines %s", armedCrewOnBoard, marinesOnBoard)), // highlight 2
                    Misc.getHighlightColor(),
                    Misc.getHighlightColor()
            );
            decideVictory();
        }
    }

    protected void decideVictory(){
        double chance = Math.random();
        if (chance < 0.5f){
            // TODO remove crew and marines
            Global.getSector().getCampaignUI().addMessage(
                    "Victory! Crew lost %s, %s",
                    Misc.getTextColor(),
                    String.valueOf(10), // highlight 1
                    String.valueOf(String.format("Armed crew lost %s, Marines lost %s", 10, 10)), // highlight 2
                    Misc.getHighlightColor(),
                    Misc.getHighlightColor()
            );
        } else {
            // TODO remove crew and marines
            Global.getSector().getCampaignUI().addMessage(
                    "Defeat! Crew lost %s, %s",
                    Misc.getTextColor(),
                    String.valueOf(15), // highlight 1
                    String.valueOf(String.format("Armed crew lost %s, Marines lost %s", 15, 15)), // highlight 2
                    Misc.getHighlightColor(),
                    Misc.getHighlightColor()
            );
        }

    }
}
