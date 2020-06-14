package shadysoupdealersmainbot.general;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import shadysoupdealersmainbot.RunnableBot;
import shadysoupdealersmainbot.general.util.Cache;

public class NetGun implements RunnableBot {
	private RobotController robotController;
	public NetGun(RobotController robotController) {
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
		attack();
	}
	public void attack() throws GameActionException {
		int maxDistance = Integer.MAX_VALUE;
		RobotInfo target = null;
		RobotInfo[] potentialTargets = robotController.senseNearbyRobots(Cache.CURRENT_LOCATION,
				GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, robotController.getTeam().opponent());
		for (RobotInfo potentialTarget : potentialTargets) {
			int enemyID = potentialTarget.getID();
			int enemyDistance = Cache.CURRENT_LOCATION.distanceSquaredTo(potentialTarget.getLocation());
			if (enemyDistance < maxDistance) {
				if (robotController.canShootUnit(enemyID)) {
					if( target==null || (potentialTarget.isCurrentlyHoldingUnit() || !target.isCurrentlyHoldingUnit()) ){
						maxDistance = enemyDistance;
						target = potentialTarget;
					}
				}
			}
		}
		if (target != null) {
			robotController.shootUnit(target.getID());
			}
	}
}
