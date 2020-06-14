package shadysoupdealersmainbot.general;

import battlecode.common.*;
import shadysoupdealersmainbot.RunnableBot;
import shadysoupdealersmainbot.general.util.*;

public class DesignSchool implements RunnableBot {
	private RobotController robotController;
	public DesignSchool(RobotController robotController) {
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
		if (!Miner.validateDesignSchoolLocation(Cache.CURRENT_LOCATION,
				robotController.senseElevation(Cache.CURRENT_LOCATION))) {
			robotController.disintegrate();
		}
		// TODO: If we see a drone, we should not build landscapers
		if (robotController.getTeamSoup() < RobotType.LANDSCAPER.cost) {
			return;
		}
		if (robotController.getTeamSoup() >= BuildOrder.getSoupThreshold(RobotType.LANDSCAPER)) {
			buildLandscaper();
		}

	}

	public static void buildLandscaper() throws GameActionException {
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		MapLocation enemyHQLocation = InformationBank.getEnemyHQLocation();
		if (enemyHQLocation == null) {
			enemyHQLocation = Cache.CENTER_LOCATION;
		}
		Direction idealDirection = currentLocation.directionTo(enemyHQLocation);
		for (Direction direction : InitEngine.getAttemptOrder(idealDirection)) {
			MapLocation currentLocationWithDirection = currentLocation.add(direction);
			if (!GridChecker.checkIfIsHole(currentLocationWithDirection) &&
					InitEngine.checkCanSafeBuildRobot(RobotType.LANDSCAPER, direction)) {
				Cache.controller.buildRobot(RobotType.LANDSCAPER, direction);
				InformationBank.broadcastBuildLandscaper();
				return;
			}
		}
	}
}
