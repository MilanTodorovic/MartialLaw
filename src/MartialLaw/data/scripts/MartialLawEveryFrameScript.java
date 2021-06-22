package MartialLaw.data.scripts;

import MartialLaw.data.campaign.ids.MartialLawHullmods;
import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignClockAPI;
import MartialLaw.data.campaign.ids.MartialLawCommodities;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.fleet.CrewCompositionAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.MutableFleetStatsAPI;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;


import java.util.*;


public class MartialLawEveryFrameScript implements EveryFrameScript {

    public static int daySinceLastMutiny = 0;
    public static int currentDay = 0;
    public boolean firstTick = true;
    public static int loyaltyHullmods = 0;
    public static Map<String, List<Float>> shipCrewCompistion = new HashMap<String, List<Float>>();

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
            double chance = Math.random();
            for (FleetMemberAPI ship : Global.getSector().getPlayerFleet().getFleetData().getCombatReadyMembersListCopy()) {
                if (ship.getHullSpec().getBuiltInMods().contains(MartialLawHullmods.CREW_LOYALTY)) {
                    // TODO maybe add a listener to hullmod changes and increment a global variable?
                    //  also take notes of lost/sold ships and deduct them
                    // TODO each ship hull contributes a different amount
                    loyaltyHullmods += 1;
                }
                final CrewCompositionAPI CC = ship.getCrewComposition();
                shipCrewCompistion.put(ship.getShipName(), new ArrayList<Float>() {
                    {
                        add(CC.getCrew());
                        add(CC.getMarines());
                        add(3f);
                    }
                });
            }
            // TODO how to distribute?
            if (chance < MartialLawPlugin.MUTINY_BASE_MULTIPLAYER + (daySinceLastMutiny * MartialLawPlugin.MUTINY_PER_DAY_MULTIPLAYER)
                    - (MartialLawPlugin.CREW_LOYALTY_BONUS * loyaltyHullmods)
                    && daySinceLastMutiny > MartialLawPlugin.MUTINY_GRACE_PERIOD_DAYS) {
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

    protected void startMutiny() {
        CampaignFleetAPI fleet = Global.getSector().getPlayerFleet();
        float armedCrewOnBoard = fleet.getCargo().getCommodityQuantity(MartialLawCommodities.ARMED_CREW);
        float crewOnBoard = fleet.getCargo().getCommodityQuantity(Commodities.CREW);
        float marinesOnBoard = fleet.getCargo().getCommodityQuantity(Commodities.MARINES);
        // TODO Pop-up message detailing the mutiny and results
        //  killing some crew/armed crew/marines?
        //  decide who defends and who attacks


    }
}
