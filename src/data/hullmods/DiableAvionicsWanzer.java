package data.hullmods;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.ai.WanzerMovementScript;
import org.lazywizard.lazylib.MathUtils;

import javax.xml.bind.Unmarshaller;
import java.util.Iterator;
import java.util.List;

public class DiableAvionicsWanzer extends BaseHullMod {

    private final float EMP_RESIST = 33, DISABLE_RESIST = 66;


    private ShipAPI ship = null;
    private float maxDistance = Float.MAX_VALUE;
    private IntervalUtil decelerateInterval = new IntervalUtil(0.25f, 0.5f);   //判定减速的间隔
    private boolean canDecelerate = true;
    private IntervalUtil strafeInterval = new IntervalUtil(4f, 8f);  //判定左右游荡的间隔
    private IntervalUtil judgeInterval = new IntervalUtil(0.1f,0.1f);
    private static ShipCommand[] validStrafeStates = new ShipCommand[]{ShipCommand.STRAFE_LEFT, ShipCommand.STRAFE_RIGHT};
    private ShipCommand strafeState = ShipCommand.STRAFE_LEFT;

    private boolean blizzaia_reloading =false;


    @Override
    public String getDescriptionParam(int index, HullSize hullSize) {
        if (index == 0) {
            return "" + (int) EMP_RESIST + "%";
        }
        if (index == 1) {
            return "" + (int) DISABLE_RESIST + "%";
        }
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {

    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.getMutableStats().getEmpDamageTakenMult().modifyMult(id, (100 - EMP_RESIST) / 100);
        ship.getMutableStats().getEngineDamageTakenMult().modifyMult(id, (100 - DISABLE_RESIST) / 100);
        ship.getMutableStats().getWeaponDamageTakenMult().modifyMult(id, (100 - DISABLE_RESIST) / 100);



        ship.addListener(new WanzerMovementScript(ship));
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {



    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        // Allows any ship with a diableavionics hull id
        return (ship.getHullSpec().getHullId().startsWith("diableavionics_"));
    }

    private static ShipCommand randomStrafeDir() {
        return validStrafeStates[MathUtils.getRandomNumberInRange(0, validStrafeStates.length - 1)];
    }

    private float maxDistanceByweapon(ShipAPI ship){
        float maxDistance = Float.MAX_VALUE;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.isDecorative() || weapon.hasAIHint(WeaponAPI.AIHints.PD)) continue; //对于飞机,遍历他的所有武器,取无pd词条的射程最小的武器作为最大判定距离
            if (weapon.getRange() < maxDistance) {
                maxDistance = weapon.getRange() * 0.9f;  //最大判定距离设置为这个武器的90% tart写的 我懒得改了 可以自己修改
            }
        }

        return maxDistance;
    }

}
