package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.loading.HullModSpecAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.combat.ai.FighterAI;
import data.scripts.ai.WanzerMovementScript;
import org.lazywizard.lazylib.MathUtils;
import static data.scripts.util.Diableavionics_stringsManager.txt;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiableAvionicsSniperkit extends BaseHullMod {

    private float REDUCED_RANGE =0.25f;
    private float SMOD_REDUCED_RANGE=0.75f;
    boolean sMod = false;
    public static Map mag = new HashMap();
    static {
        mag.put(ShipAPI.HullSize.FIGHTER, 0f);
        mag.put(ShipAPI.HullSize.FRIGATE, 10f);
        mag.put(ShipAPI.HullSize.DESTROYER, 20f);
        mag.put(ShipAPI.HullSize.CRUISER, 40f);
        mag.put(ShipAPI.HullSize.CAPITAL_SHIP, 60f);
    }



    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

    }


    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {

       sMod=isSMod(stats);

    }



    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {


        if (fighter.getWing() != null && fighter.getWing().getSpec() != null) {
            if(fighter.getWing().getSpec().getId().contains("warlust"))
            {
                fighter.getMutableStats().getBallisticWeaponRangeBonus().modifyPercent(id,(Float)mag.get(ship.getHullSize()));

            }
        }
    }
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {

        int bays = (int) ship.getMutableStats().getNumFighterBays().getModifiedValue();
        return bays > 0 && !ship.getVariant().getHullMods().contains("defensive_targeting_array");

    }

    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + ((Float) mag.get(ShipAPI.HullSize.FRIGATE)).intValue() + "%";
        if (index == 1) return "" + ((Float) mag.get(ShipAPI.HullSize.DESTROYER)).intValue() + "%";
        if (index == 2) return "" + ((Float) mag.get(ShipAPI.HullSize.CRUISER)).intValue() + "%";
        if (index == 3) return "" + ((Float) mag.get(ShipAPI.HullSize.CAPITAL_SHIP)).intValue() + "%";
        if (index == 4) return "" + (int)((1-REDUCED_RANGE)*100)+ "%";
        return null;
    }



    public String getSModDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if (index == 0) return "" + (int)((1-SMOD_REDUCED_RANGE)*100) + "%";
        if (index == 1) return "" + (int)((1-REDUCED_RANGE)*100) + "%";
        return null;
    }

    public String getUnapplicableReason(ShipAPI ship) {
        if (!ship.hasLaunchBays()) {
            return txt("hm_sniperkit_01");
        }
        if (ship.getVariant().getHullMods().contains("defensive_targeting_array")) {
            return txt("hm_sniperkit_02");
        }
        return null;
    }
}
