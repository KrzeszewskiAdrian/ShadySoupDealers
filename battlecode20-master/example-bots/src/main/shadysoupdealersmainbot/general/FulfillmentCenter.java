package shadysoupdealersmainbot.general;

import battlecode.common.*;
import shadysoupdealersmainbot.RunnableBot;
import shadysoupdealersmainbot.general.util.BuildOrder;
import shadysoupdealersmainbot.general.util.Cache;
import shadysoupdealersmainbot.general.util.InformationBank;
import shadysoupdealersmainbot.general.util.InitEngine;

public class FulfillmentCenter implements RunnableBot {
	private RobotController robotController;
	public FulfillmentCenter(RobotController robotController) {
		this.robotController = robotController;
	}
	@Override
	public void init() {

	}
	@Override
	public void turn() throws GameActionException {
		if (!robotController.isReady()) {
			return;
		}

		if (robotController.getTeamSoup() < RobotType.DELIVERY_DRONE.cost) {
			return;
		}
		if (enemyNetGunVisible()) {
			return;
		}
		//Only miner and landscaper
		RobotInfo enemy = findEnemyUnit();
		if (enemy == null) {
			if (robotController.getTeamSoup() < BuildOrder.getSoupThreshold(RobotType.DELIVERY_DRONE)) {
				return;
			}
		}
		MapLocation location;
		if (enemy != null) {
			location = enemy.getLocation();
		} else if (InformationBank.getEnemyHQLocation() != null) {
			location = InformationBank.getEnemyHQLocation();
		} else {
			location = Cache.CENTER_LOCATION;
		}
		InitEngine.attemptBuildTowardsLocationSafe(RobotType.DELIVERY_DRONE, location);
	}
	public static RobotInfo findEnemyUnit() {
		RobotInfo unit = null;
		int currentDistance = Integer.MAX_VALUE;
		for (RobotInfo enemy : Cache.NEARBY_ENEMY_ROBOTS) {
			if (enemy.getType() == RobotType.MINER || enemy.getType() == RobotType.LANDSCAPER) {
				int distanceSquared = enemy.getLocation().distanceSquaredTo(Cache.CURRENT_LOCATION);
				if (distanceSquared < currentDistance) {
					currentDistance = distanceSquared;
					unit = enemy;
				}
			}
		}
		return unit;
	}
	public boolean enemyNetGunVisible() {
		for (RobotInfo robot : Cache.NEARBY_ENEMY_ROBOTS) {
			if (robot.getType() == RobotType.NET_GUN) {
				return true;
			}
		}
		return false;
	}
}
