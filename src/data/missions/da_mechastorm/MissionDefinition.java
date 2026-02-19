package data.missions.da_mechastorm;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.FactionAPI;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.fleet.FleetMemberType;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.ids.Personalities;
import com.fs.starfarer.api.impl.campaign.ids.Skills;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.mission.MissionDefinitionPlugin;

public class MissionDefinition implements MissionDefinitionPlugin {
    @Override
	public void defineMission(MissionDefinitionAPI api) {

	
		// Set up the fleets so we can add ships and fighter wings to them.
		// In this scenario, the fleets are attacking each other, but
		// in other scenarios, a fleet may be defending or trying to escape
		api.initFleet(FleetSide.PLAYER, "DSF", FleetGoal.ATTACK, false);
		api.initFleet(FleetSide.ENEMY, "TIF", FleetGoal.ATTACK, true);

		// Set a small blurb for each fleet that shows up on the mission detail and
		// mission results screens to identify each side.
		api.setFleetTagline(FleetSide.PLAYER, "航电卫队");
		api.setFleetTagline(FleetSide.ENEMY, "速子科技干涉舰队");
		
		// These show up as items in the bulleted list under 
		// "Tactical Objectives" on the mission detail screen
        api.addBriefingItem("敌方小型舰船缺乏应对饱和打击的能力,运用微型导弹压制它们");
        api.addBriefingItem("速子科技的舰队整备良好且训练有素,并且重点考虑了反战机能力,合理运用汐风的系统来组织机甲进攻");
        api.addBriefingItem("您的舰队缺乏直接与典范级正面冲突的能力,命令小型舰船牵制它.");
		
		// Set up the player's fleet.  Variant names come from the
		// files in data/variants and data/variants/fighters
                api.addToFleet(FleetSide.PLAYER, "diableavionics_sirocco_legion", FleetMemberType.SHIP, true);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_daze_beamer", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "diableavionics_storm_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_gust_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_gust_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_coanda_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_coanda_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_draft_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_draft_standard", FleetMemberType.SHIP, false);
                api.addToFleet(FleetSide.PLAYER, "diableavionics_sleet_standard", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "diableavionics_sleet_standard", FleetMemberType.SHIP, false);
				api.addToFleet(FleetSide.PLAYER, "diableavionics_fractus_standard", FleetMemberType.SHIP, false);



//                if(Global.getSettings().isDevMode()){
//                    api.addToFleet(FleetSide.PLAYER, "diableavionics_IBBgulf_boss", FleetMemberType.SHIP, false);
//                    api.addToFleet(FleetSide.PLAYER, "diableavionics_virtuous_grenadier", FleetMemberType.SHIP, false);
//
//                    api.addToFleet(FleetSide.PLAYER, "diableavionics_chinook_standard", FleetMemberType.SHIP, false);
//                    api.addToFleet(FleetSide.PLAYER, "diableavionics_cirrus_standard", FleetMemberType.SHIP, false);
//                    api.addToFleet(FleetSide.PLAYER, "diableavionics_stratus_standard", FleetMemberType.SHIP, false);
//                    api.addToFleet(FleetSide.PLAYER, "diableavionics_stratus_p_combat", FleetMemberType.SHIP, false);
//                    api.addToFleet(FleetSide.PLAYER, "diableavionics_rime_standard", FleetMemberType.SHIP, false);
//                    api.addToFleet(FleetSide.PLAYER, "diableavionics_rime_p_support", FleetMemberType.SHIP, false);
//
//                    api.addToFleet(FleetSide.PLAYER, "diableavionics_laminar_miner", FleetMemberType.SHIP, false);
//                    api.addToFleet(FleetSide.PLAYER, "diableavionics_shear_standard", FleetMemberType.SHIP, false);
//
//                }
             	                
		// Mark both ships as essential - losing either one results
		// in mission failure. Could also be set on an enemy ship,
		// in which case destroying it would result in a win.

		
		// Set up the enemy fleet.
		//api.addToFleet(FleetSide.ENEMY, "eagle_Assault", FleetMemberType.SHIP, false);		
		//api.addToFleet(FleetSide.ENEMY, "onslaught_Outdated", FleetMemberType.SHIP, false);	
		FleetMemberAPI enemyFlagship = api.addToFleet(FleetSide.ENEMY, "典范_mechastorm", FleetMemberType.SHIP, true);
		FleetMemberAPI enemyofficership01 = api.addToFleet(FleetSide.ENEMY, "极光_mechastorm", FleetMemberType.SHIP, false);

		api.addToFleet(FleetSide.ENEMY, "预兆_mechastorm", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "预兆_mechastorm", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "预兆_mechastorm", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "预兆_mechastorm", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "阿努比斯_mechastorm", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "阿努比斯_mechastorm", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "阿努比斯_mechastorm", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "暴雨_mechastorm", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "暴雨_mechastorm", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "暴雨_mechastorm", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "暴雨_mechastorm", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "伯劳鸟_mechastorm", FleetMemberType.SHIP, false);
		api.addToFleet(FleetSide.ENEMY, "伯劳鸟_mechastorm", FleetMemberType.SHIP, false);

		//set up officer

		FactionAPI tri = Global.getSettings().createBaseFaction(Factions.TRITACHYON);
		PersonAPI officerTri01 = tri.createRandomPerson(FullName.Gender.MALE);
		officerTri01.getStats().setSkillLevel(Skills.ENERGY_WEAPON_MASTERY, 1);
		officerTri01.getStats().setSkillLevel(Skills.COMBAT_ENDURANCE, 1);
		officerTri01.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		officerTri01.getStats().setSkillLevel(Skills.MISSILE_SPECIALIZATION, 1);
		officerTri01.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 1);
		officerTri01.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		officerTri01.getStats().setLevel(6);
		officerTri01.setFaction("tritachyon");
		officerTri01.setPersonality(Personalities.AGGRESSIVE);
		officerTri01.getName().setGender(FullName.Gender.MALE);


		PersonAPI officerTriFlag = tri.createRandomPerson(FullName.Gender.MALE);
		officerTriFlag.getStats().setSkillLevel(Skills.IMPACT_MITIGATION, 1);
		officerTriFlag.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 1);
		officerTriFlag.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2);
		officerTriFlag.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 1);
		officerTriFlag.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 1);
		officerTriFlag.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2);
		officerTriFlag.getStats().setSkillLevel(Skills.WOLFPACK_TACTICS, 1);
		officerTriFlag.getStats().setLevel(6);
		officerTriFlag.setFaction("tritachyon");
		officerTriFlag.setPersonality(Personalities.AGGRESSIVE);
		officerTriFlag.getName().setGender(FullName.Gender.MALE);


		enemyFlagship.setCaptain(officerTriFlag);
		enemyofficership01.setCaptain(officerTri01);
		float maxCRChurch = enemyFlagship.getRepairTracker().getMaxCR();
		enemyFlagship.getRepairTracker().setCR(maxCRChurch);
		enemyofficership01.getRepairTracker().setCR(maxCRChurch);


		// Set up the map.
		float width = 15000f;
		float height = 11000f;
		api.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);
		
		float minX = -width/2;
		float minY = -height/2;
		
		// All the addXXX methods take a pair of coordinates followed by data for
		// whatever object is being added.
		
		// And a few random ones to spice up the playing field.
		// A similar approach can be used to randomize everything
		// else, including fleet composition.
//		for (int i = 0; i < 7; i++) {
//			float x = (float) Math.random() * width - width/2;
//			float y = (float) Math.random() * height - height/2;
//			float radius = 100f + (float) Math.random() * 800f;
//			api.addNebula(x, y, radius);
//		}
		
		// Add objectives. These can be captured by each side
		// and provide stat bonuses and extra command points to
		// bring in reinforcements.
		// Reinforcements only matter for large fleets - in this
		// case, assuming a 100 command point battle size,
		// both fleets will be able to deploy fully right away.

		api.addObjective(minX + width * 0.7f, minY + height * 0.25f, "sensor_array");
		api.addObjective(minX + width * 0.8f, minY + height * 0.75f, "nav_buoy");
		api.addObjective(minX + width * 0.2f, minY + height * 0.25f, "nav_buoy");
		
		
	}

}
