package shadysoupdealersmainbot.general.util;

import battlecode.common.*;

public class Cache {
	public static RobotController controller;
	public static Team MY_TEAM;
	public static Team ENEMY_TEAM;
	
	public static RobotType ROBOT_TYPE;
	
	public static RobotInfo[] NEARBY_ROBOTS;
	public static RobotInfo[] NEARBY_FRIENDLY_ROBOTS;
	public static RobotInfo[] NEARBY_ENEMY_ROBOTS;
	
	public static int MAP_WIDTH;
	public static int MAP_HEIGHT;
	
	public static MapLocation CENTER_LOCATION;
	public static MapLocation CURRENT_LOCATION;

	public static MapLocation[] NEARBY_ENEMY_NET_GUNS;
	public static int NEARBY_ENEMY_NET_GUNS_SIZE;
	public static void init(RobotController controller) {
		Cache.controller = controller;
		CURRENT_LOCATION = controller.getLocation();
		
		MY_TEAM = controller.getTeam();
		ENEMY_TEAM = MY_TEAM.opponent();
		
		MAP_WIDTH = controller.getMapWidth();
		MAP_HEIGHT = controller.getMapHeight();
		
		CENTER_LOCATION = new MapLocation(MAP_WIDTH / 2, MAP_HEIGHT / 2);
		
		ROBOT_TYPE = controller.getType();
		
		if (ROBOT_TYPE == RobotType.DELIVERY_DRONE || ROBOT_TYPE == RobotType.MINER ||
				ROBOT_TYPE == RobotType.FULFILLMENT_CENTER) {
			NEARBY_ENEMY_NET_GUNS = new MapLocation[68]; // max number of net guns in vision range
			NEARBY_ENEMY_NET_GUNS_SIZE = 0;
		}
	}
	public static void loop() {
		Cache.CURRENT_LOCATION = controller.getLocation();
		
		Cache.NEARBY_ROBOTS = controller.senseNearbyRobots();
		Cache.NEARBY_FRIENDLY_ROBOTS = controller.senseNearbyRobots(-1, MY_TEAM);
		Cache.NEARBY_ENEMY_ROBOTS = controller.senseNearbyRobots(-1, ENEMY_TEAM);
		
		if (ROBOT_TYPE == RobotType.DELIVERY_DRONE || ROBOT_TYPE == RobotType.MINER ||
				ROBOT_TYPE == RobotType.FULFILLMENT_CENTER) {
			NEARBY_ENEMY_NET_GUNS_SIZE = 0;
			for (RobotInfo robot : Cache.NEARBY_ENEMY_ROBOTS) {
				if (robot.getType() == RobotType.NET_GUN) {
					NEARBY_ENEMY_NET_GUNS[NEARBY_ENEMY_NET_GUNS_SIZE++] = robot.getLocation();
				}
			}
			
			MapLocation enemyHQ = InformationBank.getEnemyHQLocation();
			if (enemyHQ != null) {
				NEARBY_ENEMY_NET_GUNS[NEARBY_ENEMY_NET_GUNS_SIZE++] = enemyHQ;
			}
		}
	}
}
