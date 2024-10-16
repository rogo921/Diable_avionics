package data.scripts.util;

import com.fs.starfarer.api.combat.ShipAPI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.CargoAPI;
import com.fs.starfarer.api.campaign.econ.MonthlyReport;
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.rulecmd.Nex_IsFactionRuler;
import com.fs.starfarer.api.impl.combat.RecallDeviceStats;
import com.fs.starfarer.api.impl.combat.dem.DEMEffect;
import com.fs.starfarer.api.loading.ProjectileSpecAPI;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.campaign.fleet.FleetMember;
import com.fs.starfarer.combat.entities.BallisticProjectile;
import org.jetbrains.annotations.NotNull;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.FastTrig;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lazywizard.lazylib.ui.LazyFont;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.plugins.MagicRenderPlugin;
import org.magiclib.util.MagicRender;
import java.awt.Color;

public class Diablavionics_tool {






    /**
     * 参考了fsf的代码的工具类
     * 用于直飞到目标点，会停在目标点上
     * 舰船类
     * */
    //entity代表ship
    public static void moveToPosition(ShipAPI ship, Vector2f toPosition) {
        Vector2f directionVec = VectorUtils.getDirectionalVector(ship.getLocation(), toPosition);
        float directionAngle = VectorUtils.getFacing(directionVec);
        float distSq = MathUtils.getDistanceSquared(ship.getLocation(), toPosition);
        Vector2f angleAndSpeed = velocity2Speed(ship.getVelocity());
        float speedAngle = VectorUtils.getFacing(ship.getVelocity());
        float timeToSlowDown = angleAndSpeed.y / ship.getDeceleration(); //time to slow down to zero speed(by seconds,squared)				s
        float faceAngleDiff = MathUtils.getShortestRotation(ship.getFacing(), directionAngle);
        float velAngleDiff = Math.abs(MathUtils.getShortestRotation(speedAngle, directionAngle));

        //控制加速减速
        //以出发点方向为切线，向目标点做圆，求圆弧长度得到目标距离。
        //飞过目标距离所花的时间，小于等于转过面向需要的时间
        double sine = Math.abs(FastTrig.sin(Math.toRadians(velAngleDiff)));
        double rSq = (distSq/4)/(sine*sine);
        double arcDistSq = 3.14f * 3.14f * 4f * rSq;
        double timeArcSq = arcDistSq/(angleAndSpeed.y * angleAndSpeed.y + 1);
        float timeTurn = Math.abs(velAngleDiff/(ship.getAngularVelocity()+1));
        //如果船飞过圆弧需要时间小于转向需要时间,即转向时间不足，减速，提前0.5秒转向当缓冲
        if( timeArcSq < (timeTurn+0.5f)*(timeTurn+0.5f) ){
            ship.giveCommand(ShipCommand.DECELERATE, null, 0);
        }else{//差别在容忍范围内
            //如果靠近目标点，开始减速
            if (distSq <= (angleAndSpeed.y * timeToSlowDown + 10) * (angleAndSpeed.y * timeToSlowDown + 10)) {
                ship.giveCommand(ShipCommand.DECELERATE, null, 0);
            }else{
                if(faceAngleDiff > 20 && faceAngleDiff < 90){
                    ship.giveCommand(ShipCommand.STRAFE_LEFT,null,0);
                }else if(faceAngleDiff < -20 && faceAngleDiff > -90){
                    ship.giveCommand(ShipCommand.STRAFE_RIGHT,null,0);
                } else{
                    ship.giveCommand(ShipCommand.ACCELERATE, null, 0);
                }
            }
        }

        //旋转朝向，注意速度方向并不会跟着旋转
        // moveToAngle(ship, directionAngle);
    }

    public static void moveToAngle(@NotNull ShipAPI ship, float toAngle) {
        if(ship==null)
            throw new NullPointerException("Ship is null! Diableavionics_tool.class");
        float angleDist = 0.0F;
        float angleNow = ship.getFacing();
        angleDist = MathUtils.getShortestRotation(angleNow, toAngle);
        boolean turnRight = false;
        turnRight = angleDist < 0.0F;
        float angleDistBeforeStop = ship.getAngularVelocity() * ship.getAngularVelocity() / (ship.getMutableStats().getTurnAcceleration().getModifiedValue() * (float)2 + (float)1);
        if (turnRight) {
            if (ship.getAngularVelocity() > 0.0F) {
                ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
            } else if (Math.abs(angleDist) - (float)2 >= angleDistBeforeStop) {
                ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
            } else {
                ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
            }
        } else if (ship.getAngularVelocity() < 0.0F) {
            ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
        } else if (Math.abs(angleDist) - (float)2 > angleDistBeforeStop) {
            ship.giveCommand(ShipCommand.TURN_LEFT, null, 0);
        } else {
            ship.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
        }

    }


    @NotNull
    public static Vector2f velocity2Speed(@NotNull Vector2f velocity) {
        if(velocity==null)
            throw new NullPointerException("null since get a null velocity Diableavionics_tool.class");

        if (velocity.x == 0.0F && velocity.y == 0.0F) {
            return new Vector2f(0.0F, 0.0F);
        } else {
            float x = VectorUtils.getFacing(velocity);
            float y = (float)Math.sqrt((double)(velocity.getX() * velocity.getX() + velocity.getY() * velocity.getY()));
            return new Vector2f(x, y);
        }
    }
}
