package MartialLaw;

import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.json.JSONObject;

// TODO check if it doesn't use too many crew, keep the minimum
public class ModPlugin extends BaseModPlugin {
    public static final String ID = "martiallaw";
    public static final String ABILITY_ID = "ml_arm_the_civilians";
//    public static final String ABILITY_ID2 = "ml_cease_fire";
    public static final String SETTINGS_PATH = "MARTIAL_LAW_OPTIONS.ini";

    public static float
            MULTIPLIER_PER_CIVILIAN_HULL = 0.1f,
            MULTIPLIER_PER_MILITARIZED_HULL = 0.1f,
            BURN_LEVEL_REDUCED = 1f,
            COMBAT_READINESS_LOSS_PER_DAY = 5f,
            HEAVY_ARMAMENT_PER_DAY = 1f,
            LIGHT_ARMAMENT_PER_DAY = 10F,
            MINIMUM_CR = 50f;
    public static int UNITS_PER_DAY = 50;

    private static boolean settingsAlreadyRead = false;

    @Override
    public void afterGameSave() {
        Global.getSector().getCharacterData().addAbility(ABILITY_ID);
//        Global.getSector().getCharacterData().addAbility(ABILITY_ID2);
    }

    @Override
    public void beforeGameSave() {
        Global.getSector().getCharacterData().removeAbility(ABILITY_ID);
//        Global.getSector().getCharacterData().removeAbility(ABILITY_ID2);
    }

    @Override
    public void onGameLoad(boolean newGame) {
        try {
            if (!Global.getSector().getPlayerFleet().hasAbility(ABILITY_ID)) {
                Global.getSector().getCharacterData().addAbility(ABILITY_ID);
            }

//            if (!Global.getSector().getPlayerFleet().hasAbility(ABILITY_ID2)) {
//                Global.getSector().getCharacterData().addAbility(ABILITY_ID2);
//            }


            if (!settingsAlreadyRead) {
                JSONObject cfg = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, ID);

                MULTIPLIER_PER_CIVILIAN_HULL = (float) Math.max(0, cfg.getDouble("conversionMultiplierPerCivilianHull"));
                MULTIPLIER_PER_MILITARIZED_HULL = (float) cfg.getDouble("conversionMultiplierPerMilitarizedHull");
                BURN_LEVEL_REDUCED = (float) cfg.getDouble("reduceMaxBurnLevel");
                COMBAT_READINESS_LOSS_PER_DAY = (float) cfg.getDouble("combatReadinessLossPerDay");
                HEAVY_ARMAMENT_PER_DAY = (float) cfg.getDouble("heavyArmamentPerDay");
                LIGHT_ARMAMENT_PER_DAY = (float) cfg.getDouble("lightArmamentPerDay");
                UNITS_PER_DAY = cfg.getInt("convertedUnitsPerDay");
                MINIMUM_CR = (float) cfg.getDouble("minimumCR");

                settingsAlreadyRead = true;
            }

        } catch (Exception e) {
            String stackTrace = "";

            for (int i = 0; i < e.getStackTrace().length; i++) {
                StackTraceElement ste = e.getStackTrace()[i];
                stackTrace += "    " + ste.toString() + System.lineSeparator();
            }

            Global.getLogger(ModPlugin.class).error(e.getMessage() + System.lineSeparator() + stackTrace);
        }
    }
}
