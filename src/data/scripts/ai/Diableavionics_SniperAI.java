//package data.scripts.ai;
//
//
//import com.fs.starfarer.api.Global;
//import com.fs.starfarer.api.combat.*;
//import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
//import com.fs.starfarer.api.impl.campaign.ids.Tags;
//import com.fs.starfarer.api.util.IntervalUtil;
//import com.fs.starfarer.combat.ai.BasicShipAI;
//import com.fs.starfarer.combat.ai.FighterAI;
//import com.fs.starfarer.combat.ai.movement.maneuvers.EscortTargetManeuverV3;
//import org.lazywizard.lazylib.MathUtils;
//import org.lazywizard.lazylib.VectorUtils;
//import org.lazywizard.lazylib.combat.AIUtils;
//import org.lwjgl.util.vector.Vector2f;
//import data.scripts.util.Diablavionics_tool;
//import java.util.List;
//
//
//public class Diableavionics_SniperAI implements ShipAIPlugin {
//
//    private ShipAPI fighter = null;
//    private ShipAPI morthership = null;
//    private float maxDistance = Float.MAX_VALUE;
//    private float Warlust_range = 4000f;
//
//    private IntervalUtil decelerateInterval = new IntervalUtil(0.25f, 0.5f);   //判定减速的间隔
//    private boolean canDecelerate = true;
//
//
//    private IntervalUtil strafeInterval = new IntervalUtil(0.25f, 0.5f);  //判定左右游荡的间隔
//    private static ShipCommand[] validStrafeStates = new ShipCommand[]{ShipCommand.STRAFE_LEFT, ShipCommand.STRAFE_RIGHT};
//    private ShipCommand strafeState = ShipCommand.STRAFE_LEFT;
//
//    private float REDUCED_RANGE =0.25f;
//    private float SMOD_REDUCED_RANGE=0.75f;
//    private boolean runOnce = false;
//    private boolean isSniper= false;
//
//    private static final float DEFAULT_FACING_THRESHOLD = 3f;
//    private static final float DEFAULT_ACC_THRESHOLD = 30f;
//
//    public Diableavionics_SniperAI(ShipAPI wanzer){
//        this.fighter = wanzer;
//    }
//
//    @Override
//    public void advance(float amount) {
//
//
//        if(fighter.getWing().getSourceShip()==null)
//            return;
//        else
//            this.morthership=fighter.getWing().getSourceShip();
//        if(!runOnce) {
//
//            if(fighter.getWing().getSpec().getId().contains("warlust")&&morthership.getVariant().getHullMods().contains("diableavionics_sniperkit")){
//
//                isSniper=true;
//                if(morthership.getVariant().getSMods().contains("diableavionics_sniperkit")){
//                    maxDistance = Warlust_range*SMOD_REDUCED_RANGE;
//                }else
//                    maxDistance = Warlust_range*REDUCED_RANGE;
//
//            }
//            runOnce=true;
//        }
//
//        if(isSniper){
//
//
//            Vector2f warlust_pos=fighter.getLocation();
//            Vector2f To_Mother_Ship = morthership.getLocation();
//
//            float distance = MathUtils.getDistance(fighter, morthership);
//
//            Boolean Infront_Of_Ship =  fighter.getLocation().getY()>(morthership.getLocation().getY()+100f);
//            fighter.addTag(Tags.WING_STAY_IN_FRONT_OF_SHIP);
//
//            strafeInterval.advance(amount);
//            if (strafeInterval.intervalElapsed()) {
//                strafeState = randomStrafeDir();     //随机挑选一个方向运动
//            }
//            fighter.giveCommand(strafeState, null, 0); //如果飞机与目标的距离小于最大判定距离,那么一边交火并前进
//
//
//            if (distance >= maxDistance) {
//                decelerateInterval.advance(amount);             //判断是否度过一定时间
//                if (decelerateInterval.intervalElapsed()) {
//                    canDecelerate = true;       //判定可以减速
//                }
//            }
////            ShipAPI EnemyTarget= AIUtils.getNearestEnemy(fighter);
////            fighter.setShipTarget(EnemyTarget);
//            this.forceCircumstanceEvaluation();
//
//            if (canDecelerate && distance >= maxDistance) {
//                //Diablavionics_tool.moveToPosition(fighter,morthership.getLocation());
//                returnMove(morthership.getLocation());
//            }else if(canDecelerate && distance<maxDistance-100f){
//                fighter.giveCommand(ShipCommand.DECELERATE,null,0);
//                canDecelerate = false;
//            }
//
//
//
//            //使用系统，由于是无人机，条件允许就启动
//            if(fighter.getSystem().canBeActivated())
//            {
//                fighter.giveCommand(ShipCommand.USE_SYSTEM,null,0);
//            }
//
//
//        }
//    }
//
//    private void strafeToward(Vector2f location) {
//        float degreeAngle = VectorUtils.getAngle(fighter.getLocation(), location);
//        float angleDif = MathUtils.getShortestRotation(fighter.getFacing(), degreeAngle);
//
//        if ((Math.abs(angleDif) < DEFAULT_FACING_THRESHOLD)) return;
//
//        ShipCommand direction = angleDif < 0 ? ShipCommand.STRAFE_LEFT : ShipCommand.STRAFE_RIGHT;
//        fighter.giveCommand(direction, null, 0);
//    }
//
//
//    private void returnMove(Vector2f location) {
//        //Don`t change ship`s facing
//        // turnToward(location);
//
//        float degreeAngle = VectorUtils.getAngle(fighter.getLocation(), location);
//        float angleDif = MathUtils.getShortestRotation(fighter.getFacing(), degreeAngle);
//        if (Math.abs(angleDif) < DEFAULT_ACC_THRESHOLD) {
//            fighter.giveCommand(ShipCommand.ACCELERATE, null, 0);
//        } else {
//            fighter.giveCommand(ShipCommand.DECELERATE, null, 0);
//        }
//    }
//    private void toPosition(Vector2f location,ShipAPI ship){
//
//        Vector2f shiploaction=ship.getLocation();
//        if(shiploaction.getX()>=location.getX()+100f){
//                ship.giveCommand(ShipCommand.STRAFE_LEFT,null,0);
//        }else if(shiploaction.getX()<=location.getX()-100f){
//                ship.giveCommand(ShipCommand.STRAFE_RIGHT,null,0);
//        }
//
//        if(shiploaction.getY()>=location.getX()+10)
//
//
//    }
//    private void turnToward(Vector2f location) {
//        float degreeAngle = VectorUtils.getAngle(fighter.getLocation(), location);
//        float angleDif = MathUtils.getShortestRotation(fighter.getFacing(), degreeAngle);
//
//        if ((Math.abs(angleDif) < DEFAULT_FACING_THRESHOLD)) return;
//
//        ShipCommand direction = angleDif > 0 ? ShipCommand.TURN_LEFT : ShipCommand.TURN_RIGHT;
//        fighter.giveCommand(direction, null, 0);
//    }
//
//    private static ShipCommand randomStrafeDir() {
//        return validStrafeStates[MathUtils.getRandomNumberInRange(0, validStrafeStates.length - 1)];
//    }
//
//
//    @Override
//    public void setDoNotFireDelay(float amount) {
//
//    }
//
//    @Override
//    public void forceCircumstanceEvaluation() {
//
//    }
//
//    @Override
//    public boolean needsRefit() {
//        return false;
//    }
//
//
//
//    @Override
//    public void cancelCurrentManeuver() {
//
//    }
//
//    private final ShipwideAIFlags AIFlags = new ShipwideAIFlags();
//    private final ShipAIConfig AIConfig = new ShipAIConfig();
//
//    @Override
//    public ShipAIConfig getConfig() {
//        return AIConfig;
//    }
//    @Override
//    public ShipwideAIFlags getAIFlags() {
//        return AIFlags;
//    }
//
//
//
//}
