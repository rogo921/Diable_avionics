package data.scripts.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.Diableavionics_wanzerAI;
import org.lazywizard.lazylib.MathUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Diableavionics_warlustAI implements AdvanceableListener {

    private final static Map<ShipAPI.HullSize, Float> SHIP_DISTANCE_MODIFER= new HashMap<ShipAPI.HullSize,Float>();

    private final static Map<String, Boolean> WANZER_PDWEAPON_FILTER= new HashMap<String, Boolean>();

    {
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.CAPITAL_SHIP,0.9f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.CRUISER,0.9f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.DESTROYER,0.7f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.FRIGATE,0.7f);
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

    enum wanzerState{
        engaging,retreat
    }
    WanzerMovementScript.wanzerState state= WanzerMovementScript.wanzerState.engaging;

    private ShipAPI ship = null;
    private float maxDistance = Float.MAX_VALUE;
    private IntervalUtil decelerateInterval = new IntervalUtil(0.2f, 0.3f);   //判定减速的间隔

    private boolean finishDecelerate = true;

    private IntervalUtil strafeInterval = new IntervalUtil(4f, 8f);  //判定左右游荡的间隔
    private IntervalUtil stateInterval = new IntervalUtil(4f, 6f);   //判定交战状态的间隔

    private static ShipCommand[] validStrafeStates = new ShipCommand[]{ShipCommand.STRAFE_LEFT, ShipCommand.STRAFE_RIGHT};
    private ShipCommand strafeState = ShipCommand.STRAFE_LEFT;

    private boolean runOnce = false;


    private ShipAIPlugin defaulAI;
    public Diableavionics_warlustAI(ShipAPI ship) {
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
            maxDistance=Diableavionics_wanzerAI.maxDistanceByweapon(ship);
            runOnce=true;
        }



        stateInterval.advance(amount);

        if(stateInterval.intervalElapsed()){


            if(ship.getWing()==null){
                state= WanzerMovementScript.wanzerState.engaging;
            }else{

                if(state== WanzerMovementScript.wanzerState.retreat&&!Diableavionics_wanzerAI.canRetreat(ship)){
                    state= WanzerMovementScript.wanzerState.engaging;
                }

                if(Diableavionics_wanzerAI.canRetreat(ship)){
                    state= WanzerMovementScript.wanzerState.retreat;
                }
            }

        }

        //Different Wanzer logic judgement here:

        //战意：狙击手套件下不适用万泽ai,直接返回 smod的狙击手套件依然然能够作战，因此需要使用ai

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

        //distance check here:


        switch(state){

            case retreat:{
                for(FighterWingAPI wanzerWing:ship.getWing().getSourceShip().getAllWings()){
                    wanzerWing.orderReturn(ship);

                }
            }break;
            case engaging:{

                //推进时钟
                strafeInterval.advance(amount);
                decelerateInterval.advance(amount);

                //顺逆时针运动
                if (strafeInterval.intervalElapsed()) {
                    strafeState = randomStrafeDir();
                }
//        Global.getLogger(this.getClass()).info(ship.getId());


                //——————运动与后撤部分
                ShipAPI target = ship.getShipTarget();
                if (target != null) {
                    float distance = MathUtils.getDistance(ship,target);

                    // 交战中 选择一个方向侧滑
                    if (distance <= maxDistance) {
                        ship.giveCommand(strafeState, null, 0);
                    }



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

            }break;
        }

    }


    private static ShipCommand randomStrafeDir() {
        return validStrafeStates[MathUtils.getRandomNumberInRange(0, validStrafeStates.length - 1)];
    }


}
