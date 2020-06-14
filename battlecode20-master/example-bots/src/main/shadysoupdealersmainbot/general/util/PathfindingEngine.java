package shadysoupdealersmainbot.general.util;

import battlecode.common.*;
import shadysoupdealersmainbot.general.Landscaper;

import java.util.Arrays;
import java.util.Comparator;

public class PathfindingEngine {
	public static boolean ignoreNetGuns = false;
	private static RobotController robotController;
	private static IntCounter2D visited;
	private static MapLocation lastTarget;

	public static void init(RobotController controller) {
		PathfindingEngine.robotController = controller;
		visited = new IntCounter2D(Cache.MAP_WIDTH, Cache.MAP_HEIGHT);
	}
	
	public static void execute(MapLocation target) throws GameActionException {
		if (lastTarget == null || !lastTarget.equals(target)) {
			lastTarget = target;
			visited.reset();
		}
		
		if (Cache.ROBOT_TYPE == RobotType.DELIVERY_DRONE) {
			visited.updateBaseTrail(5); // Drones only think of 5 last visited tiles
		}
		
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		robotController.setIndicatorLine(currentLocation, target, 0, 255, 0);
		
		if (currentLocation.equals(target)) {
			return;
		}
		
		if (!robotController.isReady()) {
			return;
		}
		
		visited.add(currentLocation.x, currentLocation.y);
		Direction goToDirection = currentLocation.directionTo(target);
		for (Direction direction : InitEngine.getAttemptOrder(goToDirection)) {
			MapLocation location = currentLocation.add(direction);
			if (!InitEngine.checkIfIsOnTheMap(location)) {
				continue;
			}
			if (visited.contains(location.x, location.y)) {
				continue;
			}
			if (basicMove(direction)) {
				return;
			}
		}
		// We are stucked in hole. Look for the lowest non-negative
		Direction[] directions = InitEngine.getAttemptOrder(goToDirection);
		Integer[] indices = new Integer[8];
		int[] counters = new int[8];

		for (int i = counters.length; --i >= 0;) {
			MapLocation location = currentLocation.add(directions[i]);
			
			if (InitEngine.checkIfIsOnTheMap(location)) {
				counters[i] = visited.get(location.x, location.y);
			} else {
				counters[i] = Integer.MAX_VALUE;
			}
			indices[i] = i;
		}
		Arrays.sort(indices, Comparator.comparingInt(i -> counters[i]));
		for (int i = 0; i < indices.length; i++) {
			if (basicMove(directions[indices[i]])) {
				return;
			}
		}
	}
	
	// A general rules about how to move
	public static boolean basicMove(Direction direction) throws GameActionException {
		MapLocation location = Cache.CURRENT_LOCATION.add(direction);
		if (!robotController.canSenseLocation(location)) {
			return false;
		}
		if (InitEngine.checkIfIsBlocked(location)) {
			return false;
		}
		if (Cache.ROBOT_TYPE != RobotType.DELIVERY_DRONE && GridChecker.checkIfIsHole(location)) {
			if (Cache.ROBOT_TYPE != RobotType.MINER || InitEngine.hasLattice) {
				return false;
			}
		}
		// Avoid pollution
		if (robotController.sensePollution(location) > 24000) {
			return false;
		}
		//Avoid net guns if drone
		if (Cache.ROBOT_TYPE == RobotType.DELIVERY_DRONE) {
			if (!ignoreNetGuns) {
				switch (direction) {
					case NORTHEAST:
					case NORTHWEST:
					case SOUTHEAST:
					case SOUTHWEST:
						return false;
				}
				for (int i = Cache.NEARBY_ENEMY_NET_GUNS_SIZE; --i >= 0;) {
					MapLocation netgunLocation = Cache.NEARBY_ENEMY_NET_GUNS[i];
					if (location.isWithinDistanceSquared(netgunLocation, GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED)) {
						return false;
					}
				}
			}
		} 
		else {
			int currentElevation = robotController.senseElevation(Cache.CURRENT_LOCATION);
			int toElevation = robotController.senseElevation(location);
			int lower = Landscaper.getTargetElevation();
			int upper = lower + GameConstants.MAX_DIRT_DIFFERENCE;
			if (Math.abs(currentElevation - toElevation) > GameConstants.MAX_DIRT_DIFFERENCE) {
				if (Cache.ROBOT_TYPE == RobotType.LANDSCAPER) {
					// Try to terraform
					int currentDifference = calculateElevationDelta(currentElevation, lower, upper);
					int toDifference = calculateElevationDelta(toElevation, lower, upper);
					int threshold = (robotController.getRoundNum() < 200 ? 20 : Landscaper.LANDSCAPING_LIMIT);
					if (currentDifference > threshold || toDifference > threshold) {
						// No need to terafform
						return false;
					}
					// We will terraform
					if (toDifference > currentDifference) {
						if (attemptTerraformation(location, toElevation, lower, upper)) {
							return true;
						}
						if (attemptTerraformation(Cache.CURRENT_LOCATION, currentElevation, lower, upper)) {
							return true;
						}
					} 
					else {
						if (attemptTerraformation(Cache.CURRENT_LOCATION, currentElevation, lower, upper)) {
							return true;
						}
						if (attemptTerraformation(location, toElevation, lower, upper)) {
							return true;
						}
					}
					
					if (currentElevation < lower || toElevation < lower) {
						// No dirt
						Landscaper.dig();
						return true;
					}
					if (currentElevation > upper || toElevation > upper) {
						// Full of dirt
						Landscaper.depositDirt();
						return true;
					}
				} 
				else {
					return false;
				}
			}
			if (Cache.ROBOT_TYPE == RobotType.MINER) {
				if (toElevation < currentElevation) {
					if (robotController.getRoundNum() > 180) {
						if (toElevation < GameConstants.getWaterLevel(Cache.controller.getRoundNum()) + 1) {
							// We don't know how to get back to HQ vision range - shouldn't happen in reality
							return false;
						}
					}
				}
			}
			// Avoid drones
			if (Cache.ROBOT_TYPE == RobotType.MINER || Cache.ROBOT_TYPE == RobotType.LANDSCAPER) {
				for (RobotInfo enemy : Cache.NEARBY_ENEMY_ROBOTS) {
					if (enemy.getType() == RobotType.DELIVERY_DRONE && enemy.getLocation().isAdjacentTo(location)) {
						return false;
					}
				}
			}
		}
		if (robotController.canMove(direction)) {
			robotController.move(direction);
		}
		//success :>
		return true;
	}
	
	private static int calculateElevationDelta(int elevation, int lower, int upper) {
		if (elevation < lower) {
			return lower - elevation;
		}
		if (elevation > upper) {
			return elevation - upper;
		}
		return 0;
	}
	
	private static boolean attemptTerraformation(MapLocation location, int elevation, int lower, int upper) throws GameActionException {
		Direction direction = Cache.CURRENT_LOCATION.directionTo(location);
		if (elevation < lower) {
			if (robotController.canDepositDirt(direction)) {
				robotController.depositDirt(direction);
				return true;
			}
		} else if (elevation > upper) {
			if (robotController.canDigDirt(direction)) {
				robotController.digDirt(direction);
				return true;
			}
		}
		return false;
	}
	
	public static int getTurnsCounter() {
		return visited.getCounter();
	}
	
}
