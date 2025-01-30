package data.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAIPlugin;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import data.scripts.ai.Diableavionics_WanzerDrawbackAI;
import sun.awt.windows.WGlobalCursorManager;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class WanzerMovementScript implements AdvanceableListener {

    private final static Map<ShipAPI.HullSize, Float> SHIP_DISTANCE_MODIFER= new HashMap<ShipAPI.HullSize,Float>();

    private final static Map<String, Boolean> WANZER_PDWEAPON_FILTER= new HashMap<String, Boolean>();

    {
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.CAPITAL_SHIP,0.7f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.CRUISER,0.6f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.DESTROYER,0.5f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.FRIGATE,0.5f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.DEFAULT,1f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.FIGHTER,1f);
    }
    {
        WANZER_PDWEAPON_FILTER.put("diableavionics_blizzaiaHead",false);
        WANZER_PDWEAPON_FILTER.put("diableavionics_strifehead",false);
        WANZER_PDWEAPON_FILTER.put("diableavionics_frosthead",false);
        WANZER_PDWEAPON_FILTER.put("diableavionics_warlusthead",false);
        WANZER_PDWEAPON_FILTER.put("diableavionics_warlustmissile",false);
    }

    private ShipAPI ship = null;
    private float maxDistance = Float.MAX_VALUE;
    private IntervalUtil decelerateInterval = new IntervalUtil(0.2f, 0.3f);   //判定减速的间隔

    private boolean finishDecelerate = true;

    private IntervalUtil strafeInterval = new IntervalUtil(4f, 8f);  //判定左右游荡的间隔

    private static ShipCommand[] validStrafeStates = new ShipCommand[]{ShipCommand.STRAFE_LEFT, ShipCommand.STRAFE_RIGHT};
    private ShipCommand strafeState = ShipCommand.STRAFE_LEFT;

    private boolean blizzaia_reloading =false;
    private boolean runOnce = false;

    float CicadaCooldown = 7.4f;
    private ShipAIPlugin defaulAI;
    public WanzerMovementScript(ShipAPI ship) {
        this.ship = ship;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.isDecorative() || weapon.hasAIHint(WeaponAPI.AIHints.PD)) continue; //对于飞机,遍历他的所有武器,取无pd词条的射程最小的武器作为最大判定距离
            if (weapon.getRange() <= maxDistance) {
                maxDistance = weapon.getRange() * 0.9f;  //最大判定距离设置为这个武器的90%
            }
        }
    }

    @Override
    public void advance(float amount) {

        if(!runOnce){
            defaulAI=ship.getShipAI();
            runOnce=true;
        }

        //战意：狙击手套件下不适用万泽ai,直接返回 smod的狙击手套件任然能够作战，因此需要使用ai

        if(ship.getHullSpec().getHullId().contains("warlust")){

            if(ship.getWing().getSourceShip().getVariant().getSMods().contains("diableavionics_sniperkit")){
                //sniper kit is smod
                //do nothing
            }else if(ship.getWing().getSourceShip().getVariant().getHullMods().contains("diableavionics_sniperkit")){
                //sniper kit is hullmod but not smod
                //end movement script
                return;
            }
        }




        strafeInterval.advance(amount);
        if (strafeInterval.intervalElapsed()) {
            strafeState = randomStrafeDir();     //随机挑选一个方向运动
        }
//        Global.getLogger(this.getClass()).info(ship.getId());

        //距离判断部分
        maxDistance=maxDistanceByweapon(ship);


        //暴雪:金蝉榴弹处于装弹装填下 拉远距离
        if(ship.getHullSpec().getBaseHullId().contains("blizzaia")){

            for (WeaponAPI weapon : ship.getAllWeapons()) {

                if (weapon.getOriginalSpec().getWeaponId().equals("diableavionics_blizzaiaCicada")) {

                    float progress=weapon.getAmmoTracker().getReloadProgress();
                    if(progress==0){
                        blizzaia_reloading=false;
                    }else{
                        blizzaia_reloading=true;
                    }
                    if(blizzaia_reloading){
                        maxDistance = 600f; //令暴雪后撤至较远的距离
                    } else {
                        maxDistance = maxDistanceByweapon(ship); //暴雪的武器有效射程大概只有350点左右
                    }
                    break;
                }
            }
        }





        ShipAPI target = ship.getShipTarget();
        if (target != null) {
            float distance = MathUtils.getDistance(ship,target);

            // 交战中 选择一个方向侧滑
            if (distance <= maxDistance) {
                ship.giveCommand(strafeState, null, 0);
            }

            decelerateInterval.advance(amount);

            if (decelerateInterval.intervalElapsed()) {

                if (distance <= maxDistance * SHIP_DISTANCE_MODIFER.get(target.getHullSize())
                        && finishDecelerate) {
                    ship.setShipAI(new Diableavionics_WanzerDrawbackAI(ship,maxDistance));
                    finishDecelerate=false;
                }

                if(distance>maxDistance*(SHIP_DISTANCE_MODIFER.get(target.getHullSize())+0.1f)
                        && !finishDecelerate){
                    ship.setShipAI(defaulAI);
                    finishDecelerate=true;
                }
            }


        }


    }

    private static ShipCommand randomStrafeDir() {
        return validStrafeStates[MathUtils.getRandomNumberInRange(0, validStrafeStates.length - 1)];
    }


    private float maxDistanceByweapon(ShipAPI ship){
        float maxDistance = Float.MAX_VALUE;
        for (WeaponAPI weapon : ship.getAllWeapons()) {
            if (weapon.isDecorative()) {continue;} //对于万泽,遍历他的所有武器,寻找非pd武器射程最小的那个

            if(WANZER_PDWEAPON_FILTER.containsKey(weapon.getId())){continue;} //排除掉所有不希望参与到距离计算的pd武器

            if (weapon.getRange() <= maxDistance) {
                maxDistance = weapon.getRange() * 0.9f;  //最大判定距离设置为这个武器的90% tart写的 我懒得改了 可以自己修改
            }
        }

        return maxDistance;
    }

}
