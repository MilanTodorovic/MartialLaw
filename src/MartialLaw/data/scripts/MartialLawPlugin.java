package MartialLaw.data.scripts;

import MartialLaw.data.campaign.ids.MartialLawAbilities;
import com.fs.starfarer.api.BaseModPlugin;
import com.fs.starfarer.api.Global;
import org.json.JSONObject;

// TODO check if it doesn't use too many crew, keep the minimum
public class MartialLawPlugin extends BaseModPlugin {
    public static final String ID = "martiallaw";
    public static final String ABILITY_ID = MartialLawAbilities.ARMED_CREW_ABILITY;
    //    public static final String ABILITY_ID2 = "ml_disarmed_crew";
    public static final String SETTINGS_PATH = "MARTIAL_LAW_OPTIONS.ini";

    public static float
            MULTIPLIER_PER_CIVILIAN_HULL = 0.1f,
            MULTIPLIER_PER_MILITARIZED_SUBSYSTEM = 0.1f,
            BURN_LEVEL_REDUCED = 4f,
            COMBAT_READINESS_LOSS = 10f,
            HEAVY_ARMAMENT_AMOUNT = 3f,
            LIGHT_ARMAMENT_AMOUNT = 10F,
            MINIMUM_CR = 30f,
    // TODO think more about how to distrubute rations
    MUTINY_BASE_MULTIPLAYER = 0.04f,
            MUTINY_PER_DAY_MULTIPLAYER = 0.01f,
            CREW_LOYALTY_BONUS = 0.01f;
    public static int
            UNITS = 50,
            MUTINY_GRACE_PERIOD_DAYS = 25;

    private static boolean settingsAlreadyRead = false;

    @Override
    public void onGameLoad(boolean newGame) {
        try {
            if (!Global.getSector().getPlayerFleet().hasAbility(ABILITY_ID)) {
                Global.getLogger(MartialLawPlugin.class).debug("Adding ability " + ABILITY_ID + " to the game.");
                Global.getSector().getCharacterData().addAbility(ABILITY_ID);
                Global.getLogger(MartialLawPlugin.class).debug("Ability " + ABILITY_ID + " added to the game.");
            }

            if (!settingsAlreadyRead) {
                Global.getLogger(MartialLawPlugin.class).debug("Reading .INI file.");
                JSONObject cfg = Global.getSettings().getMergedJSONForMod(SETTINGS_PATH, ID);

//                MULTIPLIER_PER_CIVILIAN_HULL = (float) Math.max(0, cfg.getDouble("conversionMultiplierPerCivilianHull"));
                MULTIPLIER_PER_MILITARIZED_SUBSYSTEM = (float) cfg.getDouble("conversionMultiplierPerMilitarizedSubsystem");
                BURN_LEVEL_REDUCED = (float) cfg.getDouble("reduceMaxBurnLevel");
                COMBAT_READINESS_LOSS = (float) cfg.getDouble("combatReadinessLoss");
                HEAVY_ARMAMENT_AMOUNT = (float) cfg.getDouble("heavyArmament");
                LIGHT_ARMAMENT_AMOUNT = (float) cfg.getDouble("lightArmament");
                UNITS = cfg.getInt("convertedUnits");
                MINIMUM_CR = (float) cfg.getDouble("minimumCR");
                MUTINY_BASE_MULTIPLAYER = (float) cfg.getDouble("armedCrewMutinyBaseMultiplayer");
                MUTINY_PER_DAY_MULTIPLAYER = (float) cfg.getDouble("armedCrewMutinyPerDayMultiplayer");
                MUTINY_GRACE_PERIOD_DAYS = cfg.getInt("mutinyGracePeriodDays");
                CREW_LOYALTY_BONUS = (float) cfg.getDouble("CrewLoyaltyBonus");

                settingsAlreadyRead = true;
            }

            Global.getSector().addTransientScript(new MartialLawEveryFrameScript());
            // Global.getSector().getListenerManager().addListener(new SomeListener(), true);

        } catch (Exception e) {
            String stackTrace = "";

            for (int i = 0; i < e.getStackTrace().length; i++) {
                StackTraceElement ste = e.getStackTrace()[i];
                stackTrace += "    " + ste.toString() + System.lineSeparator();
            }

            Global.getLogger(MartialLawPlugin.class).error(e.getMessage() + System.lineSeparator() + stackTrace);
        }
    }
}
