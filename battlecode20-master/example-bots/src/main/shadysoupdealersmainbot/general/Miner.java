package shadysoupdealersmainbot.general;

import battlecode.common.*;
import shadysoupdealersmainbot.RunnableBot;
import shadysoupdealersmainbot.general.util.*;

public class Miner implements RunnableBot {
	private RobotController robotController;
	private boolean savings = true;
	public Miner(RobotController robotController) {
		this.robotController = robotController;
	}
	public static MapLocation HQLocation;

	@Override
	public void init() {
		for (RobotInfo robot : robotController.senseNearbyRobots(-1, robotController.getTeam())) {
			if (robot.type == RobotType.HQ) {
				HQLocation = robot.getLocation();
				break;
			}
		}
	}
	@Override
	public void turn() throws GameActionException {
		if (!robotController.isReady()) {
			return;
		}

		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		
		// Kite drones
		if (InitEngine.attemptRunAwayFromAdjacentDrones()) {
			return;
		}
		if (InitEngine.hasLattice) {
			if (!HQLocation.isWithinDistanceSquared(Cache.CURRENT_LOCATION, RobotType.HQ.sensorRadiusSquared)) {
				int elevation = robotController.senseElevation(Cache.CURRENT_LOCATION);
				if (InitEngine.getTurnsToFlooded(elevation) - robotController.getRoundNum() < 50) {
					PathfindingEngine.execute(HQLocation);
					return;
				}
			}
		}
		// See if we should build net guns
		if ( buildingNetGun()) {
			return;
		}
		if (savings) {
			savings = false;
			InformationBank.broadcastSaveForNetgunSignal(false);
		}
		// See what we should build
		RobotType toBuild = determineBuildTarget();
		if (toBuild != null) {
			if (build(toBuild)) {
				return;
			}
		}
		if (depositSoup()) {
			return;
		}
		if (InformationBank.wallState == InformationBank.WALL_STATE_NEEDS) {
			if (currentLocation.isAdjacentTo(HQLocation)) {
				// Try move away
				for (Direction direction : InitEngine.getAttemptOrder(HQLocation.directionTo(currentLocation))) {
					if (robotController.canMove(direction)) {
						robotController.move(direction);
						return;
					}
				}
				robotController.disintegrate();
			}
		}

		if (robotController.getSoupCarrying() < RobotType.MINER.soupLimit) {
			for (Direction direction : InitEngine.ALL_DIRECTIONS) {
				if (robotController.canMineSoup(direction)) {
					robotController.mineSoup(direction);
					if (!MapChecker.sharedSoupLocations.contains(currentLocation.add(direction))) {
						InformationBank.broadcastSoupLoacation(currentLocation.add(direction));
					}
					return;
				}
			}
			MapLocation soupLocation = findSoup();
			if (soupLocation != null) {
				PathfindingEngine.execute(soupLocation);
			} else {
				if (!MapChecker.sharedSoupLocations.isEmpty()) {
					MapLocation nearestSoup = MapChecker.sharedSoupLocations.closestSoup(currentLocation);
					if (currentLocation.isAdjacentTo(nearestSoup)) {
						// if the soup is gone
						if (robotController.senseSoup(nearestSoup) == 0) {
							InformationBank.broadcastSoupGone(nearestSoup);
							return;
						} else { // mine if there is still soup
							Direction directionToSoup = currentLocation.directionTo(nearestSoup);
							if (robotController.canMineSoup(directionToSoup)) {
								robotController.mineSoup(directionToSoup);
								return;
							}
						}
					} else { //if not adjacent, path to the soupLocation
						PathfindingEngine.execute(nearestSoup);
					}
				} else {
					InitEngine.goExploreRandomDirection();
				}
			}
		}
	}
	public boolean depositSoup() throws GameActionException {
		if (robotController.getSoupCarrying() < RobotType.MINER.soupLimit) {
			return false;
		}
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		// Try to deposit soup
		for (Direction direction : Direction.values()) {
			if (robotController.canDepositSoup(direction)) {
				MapLocation location = currentLocation.add(direction);
				if (robotController.canSenseLocation(location)) {
					RobotInfo robot = robotController.senseRobotAtLocation(location);
					if (robot != null && robot.getTeam() == robotController.getTeam()) {
						robotController.depositSoup(direction, robotController.getSoupCarrying());
						return true;
					}
				}
			}
		}

		MapLocation currentDestination = null;
		int currentDistance = Integer.MAX_VALUE;

		boolean hqAvailable = false;

		for (Direction direction : InitEngine.ADJACENT_DIRECTIONS) {
			MapLocation location = HQLocation.add(direction);
			if (robotController.canSenseLocation(location)) {
				if (!robotController.isLocationOccupied(location)) {
					hqAvailable = true;
				}
			} else {
				hqAvailable = true;
			}
		}
		if (hqAvailable) {
			if (PathfindingEngine.getTurnsCounter() < 70) {
				currentDestination = HQLocation;
				currentDistance = currentLocation.distanceSquaredTo(HQLocation);
			}
		}
		for (RobotInfo robot : Cache.NEARBY_FRIENDLY_ROBOTS) {
			if (robot.getType() == RobotType.REFINERY) {
				MapLocation location = robot.getLocation();
				int distanceSquared = currentLocation.distanceSquaredTo(location);
				if (distanceSquared < currentDistance) {
					currentDistance = distanceSquared;
					currentDestination = location;
				}
			}
		}
		if (currentDestination == null) {
			// Build a refinery
			for (Direction direction : InitEngine.ADJACENT_DIRECTIONS) {
				MapLocation location = currentLocation.add(direction);
				if (location.isWithinDistanceSquared(HQLocation, 2)) {
					continue;
				}
				if (InitEngine.checkCanSafeBuildRobot(RobotType.REFINERY, direction)) {
					robotController.buildRobot(RobotType.REFINERY, direction);
					return true;
				}
			}
			InitEngine.goExploreRandomDirection();
		} else {
			PathfindingEngine.execute(currentDestination);
		}
		return true;
	}

	public int calculateBestDistance(MapLocation location) {
		int maxDistance = Integer.MAX_VALUE;
		for (RobotInfo friend : Cache.NEARBY_FRIENDLY_ROBOTS) {
			if (friend.getType() == RobotType.NET_GUN || friend.getType() == RobotType.HQ) {
				int distance = friend.getLocation().distanceSquaredTo(location);
				if (distance < maxDistance) {
					maxDistance = distance;
				}
			}
		}
		return maxDistance;
	}

	public boolean buildingNetGun() throws GameActionException {
		boolean friendlyBuildings = false;
		boolean enemyBuildings = false;
		for (RobotInfo friend : Cache.NEARBY_FRIENDLY_ROBOTS) {
			if (friend.getType().isBuilding()) {
				if (friend.getType() == RobotType.NET_GUN) {
					if (InformationBank.getVaporatorCount() < 8) {
						if (friend.getLocation().isWithinDistanceSquared(Cache.CURRENT_LOCATION, 5)) {
							return false;
						}
					} else {
						friendlyBuildings = true;
					}
				} else {
					friendlyBuildings = true;
				}
			}
		}
		for (RobotInfo enemy : Cache.NEARBY_ENEMY_ROBOTS) {
			if (enemy.getType() == RobotType.HQ || enemy.getType() == RobotType.FULFILLMENT_CENTER) {
				enemyBuildings = true;
				break;
			}
		}
		if ((!enemyBuildings) && (!friendlyBuildings)) {
			return false;
		}
		// Find closest enemy drone
		int maxDistance = Integer.MAX_VALUE;
		RobotInfo bestEnemy = null;
		for (RobotInfo enemy : Cache.NEARBY_ENEMY_ROBOTS) {
			if (enemy.getType() == RobotType.DELIVERY_DRONE) {
				MapLocation enemyLocation = enemy.getLocation();
				int distance = enemyLocation.distanceSquaredTo(Cache.CURRENT_LOCATION);
				if (distance < maxDistance) {
					// Check if there is an ally net gun nearby
					if (calculateBestDistance(enemyLocation) <= GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED) {
						continue;
					}
					maxDistance = distance;
					bestEnemy = enemy;
				}
			}
		}
		if (bestEnemy != null) {
			// We should build a net gun in range of the drone
			// do we have enough money
			boolean saveForNetGun = true;
			turn: {
				MapLocation enemyLocation = bestEnemy.getLocation();
				robotController.setIndicatorLine(Cache.CURRENT_LOCATION, enemyLocation, 128, 0, 255);
				// Pathfind towards enemyLocation if it's not close enough
				if (!Cache.CURRENT_LOCATION.isWithinDistanceSquared(enemyLocation, 8)) {
					PathfindingEngine.execute(enemyLocation);
					break turn;
				}
				// Kite away
				if (Cache.CURRENT_LOCATION.isWithinDistanceSquared(enemyLocation, InitEngine.ADJACENT_DISTANCE_SQUARED)) {
					// Move away
					InitEngine.attemptRunAwayFromLocation(enemyLocation, true);
				}
				// do we have enough money
				if (robotController.getTeamSoup() >= RobotType.NET_GUN.cost) {
					for (Direction direction : InitEngine.getAttemptOrder(Cache.CURRENT_LOCATION.directionTo(enemyLocation))) {
						MapLocation location = Cache.CURRENT_LOCATION.add(direction);
						// Ensure the net gun location would be in range of drone
						if (!location.isWithinDistanceSquared(enemyLocation, GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED)) {
							continue;
						}
						// Ensure it is not too close to our HQ
						if (location.isWithinDistanceSquared(HQLocation, InitEngine.ADJACENT_DISTANCE_SQUARED)) {
							continue;
						}
						// Don't build in pit
						if (GridChecker.checkIfIsHole(location)) {
							continue;
						}
						if (InitEngine.checkCanSafeBuildRobot(RobotType.NET_GUN, direction)) {
							robotController.buildRobot(RobotType.NET_GUN, direction);
							saveForNetGun = false;
							break turn;
						}
					}
				}
			}
			if (saveForNetGun && !InformationBank.isSavingForNetgun) {
				savings = true;
				InformationBank.broadcastSaveForNetgunSignal(true);
			}
			return true;
		}
		return false;
	}
	public static MapLocation findSoup() throws GameActionException {
		int currentElevation = Cache.controller.senseElevation(Cache.CURRENT_LOCATION);
		for (int i = 0; i < InitEngine.FLOOD_FILL_X.length; i++) {
			MapLocation location = Cache.CURRENT_LOCATION.translate(InitEngine.FLOOD_FILL_X[i], InitEngine.FLOOD_FILL_Y[i]);
			if (!Cache.controller.canSenseLocation(location)) {
				break;
			}
			if (Cache.controller.senseSoup(location) > 0) {
				int currentElevationDistance = Integer.MAX_VALUE;
				for (Direction direction : Direction.values()) {
					MapLocation adjacent = location.add(direction);
					if (Cache.controller.canSenseLocation(adjacent)) {
						currentElevationDistance =
								Math.min(currentElevationDistance, Math.abs(Cache.controller.senseElevation(adjacent) - currentElevation));
					}
				}
				// elevationDifference / 3 < distance + 2 (buffer)
				if (currentElevationDistance / 3.0 < Math.sqrt(Cache.CURRENT_LOCATION.distanceSquaredTo(location)) + 2) {
					return location;
				}
			}
		}
		return null;
	}

	public boolean build(RobotType type) throws GameActionException {
		MapLocation location = findBuildLocation(type);
		if (location == null) {
			PathfindingEngine.execute(HQLocation);
			return true;
		}
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		robotController.setIndicatorDot(location, 0, 0, 255);
		if (currentLocation.isAdjacentTo(location)) {
			// Build
			Direction direction = currentLocation.directionTo(location);
			if (robotController.canBuildRobot(type, direction)) {
				robotController.buildRobot(type, direction);
			}
		} else {
			PathfindingEngine.execute(location);
		}
		return true;
	}
	public MapLocation findBuildLocation(RobotType type) throws GameActionException {
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		MapLocation buildLocation = null;
		int currentDistance = Integer.MAX_VALUE;
		int currentElevation = Cache.controller.senseElevation(Cache.CURRENT_LOCATION);
		int targetElevation = Landscaper.getTargetElevation();
		// Do not consider the location where the unit currently is (starts at i = 1)
		mainLoop: for (int i = 1; i < InitEngine.FLOOD_FILL_X.length; i++) {
			int dx = InitEngine.FLOOD_FILL_X[i];
			int dy = InitEngine.FLOOD_FILL_Y[i];
			MapLocation location = currentLocation.translate(dx, dy);
			if (!InitEngine.checkIfIsOnTheMap(location)) {
				continue;
			}
			if (!Cache.controller.canSenseLocation(location)) {
				break;
			}
			if (Cache.controller.senseFlooding(location)) {
				continue;
			}
			if (!GridChecker.isBuildLocation(location)) {
				continue;
			}
			int hqDistanceSquared = HQLocation.distanceSquaredTo(location);
			if (hqDistanceSquared <= InitEngine.ADJACENT_DISTANCE_SQUARED) {
				continue;
			}

			if (type == RobotType.DESIGN_SCHOOL || type == RobotType.FULFILLMENT_CENTER) {
				if (hqDistanceSquared > RobotType.HQ.sensorRadiusSquared) {
					continue;
				}
			}
			int distanceSquared = (int) (Math.sqrt(hqDistanceSquared) * 3 + Math.sqrt(currentLocation.distanceSquaredTo(location)));
			int elevation = Cache.controller.senseElevation(location);
			if (Math.abs(currentElevation - elevation) > GameConstants.MAX_DIRT_DIFFERENCE) {
				continue;
			}
			if (elevation >= targetElevation) {
				distanceSquared -= 1000;
			}
			if (distanceSquared < currentDistance) {
				if (Cache.NEARBY_ENEMY_ROBOTS.length > 8) {
					for (Direction direction : InitEngine.ADJACENT_DIRECTIONS) {
						MapLocation adjacent = location.add(direction);
						if (robotController.canSenseLocation(adjacent)) {
							RobotInfo robot = Cache.controller.senseRobotAtLocation(adjacent);
							if (robot != null && robot.getTeam() == Cache.ENEMY_TEAM && robot.getType() == RobotType.LANDSCAPER) {
								continue mainLoop;
							}
						} else {
							continue mainLoop;
						}
					}
				} else {
					for (RobotInfo enemy : Cache.NEARBY_ENEMY_ROBOTS) {
						if (enemy.getType() == RobotType.LANDSCAPER && enemy.getLocation().isAdjacentTo(location)) {
							continue mainLoop;
						}
					}
				}

				if (type != RobotType.DESIGN_SCHOOL || validateDesignSchoolLocation(location, elevation)) {
					RobotInfo robot = Cache.controller.senseRobotAtLocation(location);
					if (robot == null) {
						currentDistance = distanceSquared;
						buildLocation = location;
					}
				}
			}
		}
		return buildLocation;
	}
	public static boolean validateDesignSchoolLocation(MapLocation buildingLocation, int elevation) throws GameActionException {
		if (!Cache.controller.canSenseLocation(buildingLocation)) {
			return false;
		}
		for (Direction direction : InitEngine.BASIC_DIRECTIONS) {
			MapLocation location = buildingLocation.add(direction);
			if (!Cache.controller.canSenseLocation(location)) {
				return false;
			}
			if (Math.abs(Cache.controller.senseElevation(location) - elevation) <= GameConstants.MAX_DIRT_DIFFERENCE) {
				return true;
			}
		}
		return false;
	}
	public RobotType determineBuildTarget() throws GameActionException {
		int teamSoup = robotController.getTeamSoup();
		RobotType target = BuildOrder.getNextRobotToBuild();
		if (target == RobotType.LANDSCAPER && InformationBank.getDesignSchoolCount() <= 0) {
			if (teamSoup >= BuildOrder.getSoupThreshold(RobotType.LANDSCAPER)) {
				return RobotType.DESIGN_SCHOOL;
			} else {
				return null;
			}
		}
		if (target == RobotType.DELIVERY_DRONE && InformationBank.getFulfillmentCenterCount() <= 0) {
			if (teamSoup >= BuildOrder.getSoupThreshold(RobotType.DELIVERY_DRONE)) {
				return RobotType.FULFILLMENT_CENTER;
			} else {
				return null;
			}
		}
		if (teamSoup >= BuildOrder.getSoupThreshold(RobotType.VAPORATOR)) {
			return RobotType.VAPORATOR;
		} else {
			return null;
		}
	}
}
