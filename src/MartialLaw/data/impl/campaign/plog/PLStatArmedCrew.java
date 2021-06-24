package MartialLaw.data.impl.campaign.plog;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.plog.BasePLStat;

import java.awt.*;

public class PLStatArmedCrew extends BasePLStat {

    @Override
    public long getCurrentValue() {
        return (int) Global.getSector().getPlayerFleet().getCargo().getCommodityQuantity("armed_crew");
    }

    @Override
    public Color getGraphColor() {
        return Global.getSettings().getColor("progressBarCrewColor");
    }

    @Override
    public String getGraphLabel() {
        return "Armed Crew";
    }

    @Override
    public String getId() {
        return "armed_crew";
    }

    public long getGraphMax() {
        return CARGO_MAX;
    }

    public String getSharedCategory() {
        return "cargo_etc";
    }
}
