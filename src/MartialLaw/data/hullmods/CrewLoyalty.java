package MartialLaw.data.hullmods;

import java.util.EnumMap;
import java.util.Map;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.fleet.FleetMemberAPI;

public class CrewLoyalty extends BaseHullMod {

    // TODO decide on percentages
    private static Map<HullSize,Float> mag = new EnumMap<HullSize,Float>(HullSize.class);
    static {
        mag.put(HullSize.FRIGATE, 5f);
        mag.put(HullSize.DESTROYER, 4f);
        mag.put(HullSize.CRUISER, 3f);
        mag.put(HullSize.CAPITAL_SHIP, 2f);
    }

    @Override
    public void advanceInCampaign(FleetMemberAPI member, float amount) {
        super.advanceInCampaign(member, amount);
    }

    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) return "" + ((Float) mag.get(HullSize.FRIGATE)).intValue() + "%";
        if (index == 1) return "" + ((Float) mag.get(HullSize.DESTROYER)).intValue() + "%";
        if (index == 2) return "" + ((Float) mag.get(HullSize.CRUISER)).intValue() + "%";
        if (index == 3) return "" + ((Float) mag.get(HullSize.CAPITAL_SHIP)).intValue() + "%";
        return null;
    }
}
