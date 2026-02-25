package data.scripts.ai;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.Diableavionics_wanzerAI;
import data.scripts.util.Diableavionics_wanzerAI.*;
import org.lazywizard.lazylib.MathUtils;

import java.util.HashMap;
import java.util.Map;

public class WanzerMovementScript implements AdvanceableListener {

    private final static Map<ShipAPI.HullSize, Float> SHIP_DISTANCE_MODIFER= new HashMap<ShipAPI.HullSize,Float>();

    {
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.CAPITAL_SHIP,0.7f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.CRUISER,0.6f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.DESTROYER,0.5f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.FRIGATE,0.5f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.DEFAULT,1f);
        SHIP_DISTANCE_MODIFER.put(ShipAPI.HullSize.FIGHTER,1f);
    }

    enum wanzerState{
        engaging,retreat
    }
    wanzerState state= wanzerState.engaging;
    private ShipAPI ship = null;
    private float maxDistance;
    private IntervalUtil decelerateInterval = new IntervalUtil(0.2f, 0.3f);   //判定减速的间隔
    private IntervalUtil stateInterval = new IntervalUtil(4f, 6f);   //判定交战状态的间隔
    private boolean finishDecelerate = true;

    private IntervalUtil strafeInterval = new IntervalUtil(4f, 8f);  //判定左右游荡的间隔

    private static ShipCommand[] validStrafeStates = new ShipCommand[]{ShipCommand.STRAFE_LEFT, ShipCommand.STRAFE_RIGHT};
    private ShipCommand strafeState = ShipCommand.STRAFE_LEFT;

    private boolean runOnce = false;

    private ShipAIPlugin defaulAI;

    public WanzerMovementScript(ShipAPI ship) {
        this.ship = ship;
        maxDistance=Float.MAX_VALUE;
    }

    @Override
    public void advance(float amount) {

        if(!runOnce){
            defaulAI=ship.getShipAI();
            runOnce=true;
        }

        //Different Wanzer logic judgement here:

        //战意：狙击手套件下不适用万泽ai,直接返回 smod的狙击手套件依然然能够作战，因此需要使用ai

//        if(ship.getHullSpec().getHullId().contains("warlust")){
//
//            if(ship.getWing().getSourceShip().getVariant().getSMods().contains("diableavionics_sniperkit")){
//                //sniper kit is smod
//                //do nothing
//            }else if(ship.getWing().getSourceShip().getVariant().getHullMods().contains("diableavionics_sniperkit")){
//                //sniper kit is hullmod but not smod
//                //end movement script
//                return;
//            }
//        }

        stateInterval.advance(amount);

        if(stateInterval.intervalElapsed()){



            if(ship.getWing()==null){
                state=wanzerState.engaging;
            }
            else{

                if(state==wanzerState.retreat&&!Diableavionics_wanzerAI.canRetreat(ship)){
                    state=wanzerState.engaging;
                }

                if(Diableavionics_wanzerAI.canRetreat(ship)){
                    state=wanzerState.retreat;
                }
            }



        }

        switch(state){

            case retreat:{

                ship.getWing().orderReturn(ship);
                ship.setShipAI(defaulAI);  //命令舰船撤退需要重置正常ai
                ship.getMutableStats().getMaxSpeed().modifyMult(this.ship.getId(), 1.25f);

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

                //距离判断部分
                maxDistance= Diableavionics_wanzerAI.maxDistanceByweapon(ship);

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

            }  break;

        }

    }


    private static ShipCommand randomStrafeDir() {
        return validStrafeStates[MathUtils.getRandomNumberInRange(0, validStrafeStates.length - 1)];
    }




}
