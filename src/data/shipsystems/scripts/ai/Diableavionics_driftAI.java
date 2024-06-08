package data.shipsystems.scripts.ai;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.ai.Diableavionics_UniThreatenJudge;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import java.awt.*;
import org.lwjgl.util.vector.Vector2f;
import data.scripts.ai.*;
import java.util.List;


public class Diableavionics_driftAI implements ShipSystemAIScript {
    private ShipAPI ship;
    private ShipSystemAPI system;
    private CombatEngineAPI engine;
    private boolean runOnce = false;
    private boolean SheildRunOnce = false;
    private float checkAgain=0.25f;
    private float delay=0f, timer=0f;
    private boolean SheildDown_flag = false;

   // private final IntervalUtil Sheild_timer = new IntervalUtil (1.0f,1.0f);

    @Override
    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.system = system;
    }

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target){

        if (engine != Global.getCombatEngine()) {
            this.engine = Global.getCombatEngine();
        }
        if (engine.isPaused() || ship.getShipAI()==null) {
            return;
        }

        if(!runOnce){
            runOnce=true;
            delay= 1.0f;
        }

        timer+=amount;
        //Sheild_timer.advance(amount);
        if(SheildDown_flag){
           //Global.getLogger(this.getClass()).info("进入关盾/撤退时流操作");
            // this part will let ship to close Sheild for a while and drift to vent hard flux in this period
            if(ship.getShield().isOn()) {
                ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK,null,0);
                //Global.getLogger(this.getClass()).info("开始关闭护盾");
            }
            if(ship.getShield().isOff()){
                ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
                //Global.getLogger(this.getClass()).info("阻止开盾");
            }
            Global.getLogger(this.getClass()).info(AIUtils.canUseSystemThisFrame(ship));
            Global.getLogger(this.getClass()).info(ship.getShield().isOff());
            if(!SheildRunOnce && AIUtils.canUseSystemThisFrame(ship)&&ship.getShield().isOff())
            {
                //Global.getLogger(this.getClass()).info("关盾并且使用时流");
                ship.useSystem();
                SheildRunOnce = true;
            }

            if(ship.getSystem().canBeActivated()&&SheildRunOnce) {
                //Global.getLogger(this.getClass()).info("关盾时流结束，更改关盾标记");
                SheildDown_flag = false;
                SheildRunOnce = false;
            }
        }

        if (timer>(delay+checkAgain)&&!SheildDown_flag) {
            //reset the timer
            timer=0;
            //remove the check delay
            checkAgain = 0f;

            if (!system.isActive() //the system is off
                    &&  AIUtils.getNearbyEnemies(ship, 2500).isEmpty() // or is alone
                    &&  ship.getSystem().getAmmo()>ship.getSystem().getMaxAmmo()-1  //and have full charges.
            ) {
                // then the system should activate, but can it?
                if (AIUtils.canUseSystemThisFrame(ship)){
                    //if the system can be activated, activate and set a minimum delay of 2 seconds before checking again
                    ship.useSystem();
                    checkAgain = 1.0f;
                    return;
                }
            }else if(!system.isActive()) //the system is off and now it` is engaging with enemy.
            {

                boolean Projectile_safe =!Diableavionics_UniThreatenJudge.isTreatenedbyProjectile(ship,1000);
                boolean Beam_safe=!Diableavionics_UniThreatenJudge.BeamweaponFiring(ship,2000);
                // then the system should activate, but can it?
                if (AIUtils.canUseSystemThisFrame(ship)){
                    //if the system can be activated, activate and set a minimum delay of 2 seconds before checking again
                    float hardfluxlevel =ship.getFluxTracker().getHardFlux()/ship.getFluxTracker().getMaxFlux();
                    float currsoftflux = ship.getFluxTracker().getCurrFlux()-ship.getFluxTracker().getHardFlux();
                    // if the ship has at least 3K softflux, then should use system to vent it.And this has a priority.
                    if(currsoftflux>=3000f){
                        ship.useSystem();
                        checkAgain = 1f;
                        return;
                    } // if the ship at least have 20%~80% hard-flux, and safe enough, down shield and use system to max the flux vent.
                    else if(hardfluxlevel<=0.8f&&hardfluxlevel>=0.2f&&Projectile_safe&&Beam_safe){
                        //Global.getLogger(this.getClass()).info("触发关盾时流");
                        SheildDown_flag=true;
                        checkAgain = 1f;
                        return;
                    }
                    else if(hardfluxlevel>0.8f){
                        //The ship is in danger,make it use system to retreat.
                        //Global.getLogger(this.getClass()).info("触发撤退时流");
                        ship.giveCommand(ShipCommand.ACCELERATE_BACKWARDS,null,0);
                        if(ship.getHitpoints()>ship.getMaxHitpoints()*0.25 && Beam_safe)
                            SheildDown_flag= true;
                        checkAgain = 0f; //no need to limit recheck
                        return;
                    }
                }
            }
        }
    }
}

