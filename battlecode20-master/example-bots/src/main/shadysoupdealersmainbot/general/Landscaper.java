package shadysoupdealersmainbot.general;

import battlecode.common.*;
import shadysoupdealersmainbot.RunnableBot;
import shadysoupdealersmainbot.general.util.*;

public class Landscaper implements RunnableBot {
	public static final int LANDSCAPING_LIMIT = 120;
	private RobotController robotController;
	private RobotBehavior[] behaviors;
	private static int targetElevation = 1;

	public Landscaper(RobotController robotController) {
		this.robotController = robotController;
	}
	@Override
	public void init() {
		// Order of behaviors to be executed
		behaviors = new RobotBehavior[] {
				this::emergencyHQHealing,
				InitEngine::attemptRunAwayFromAdjacentDrones,
				this::buryEnemyBuilding,
				this::defend,
				() -> heal(InformationBank.getMyHQLocation()),
				() -> heal(findClosestBuilding(Cache.NEARBY_FRIENDLY_ROBOTS)),
				this::adjustHQElevation,
				this::wall,
				this::terraform,
				() -> attack(findClosestBuilding(Cache.NEARBY_ENEMY_ROBOTS)),
				() -> attack(InformationBank.getEnemyHQLocation()),
				InitEngine::goExploreRandomDirection
		};
	}

	@Override
	public void turn() throws GameActionException {
		while (InitEngine.getTurnsToFlooded(targetElevation) - robotController.getRoundNum() < 100) {
			if (targetElevation >= InitEngine.TURNS_TO_FLOODED.length) {
				targetElevation = Integer.MAX_VALUE / 2;
				break;
			}
			targetElevation++;
		}
		if (!robotController.isReady()) {
			return;
		}
		for (RobotBehavior behavior : behaviors) {
			if (behavior.execute()) {
				//Success
				return;
			}
		}
	}
	public boolean emergencyHQHealing() throws GameActionException {
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		MapLocation ourHQLocation = InformationBank.getMyHQLocation();
		if (ourHQLocation == null) {
			return false;
		}
		if (currentLocation.isAdjacentTo(ourHQLocation)) {
			if (robotController.canSenseLocation(ourHQLocation)) {
				int HQHealth = RobotType.HQ.dirtLimit - robotController.senseRobotAtLocation(ourHQLocation).dirtCarrying;
				if (HQHealth <= 10) {
					Direction direction = currentLocation.directionTo(ourHQLocation);
					if (robotController.canDigDirt(direction)) {
						robotController.digDirt(direction);
					} else {
						depositDirt();
					}
					return true;
				}
			}
		}
		return false;
	}
	public boolean buryEnemyBuilding() throws GameActionException {
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		Direction currentDirection = null;
		int currentPriority = 0;
		if (robotController.canSenseRadiusSquared(InitEngine.ADJACENT_DISTANCE_SQUARED)) {
			for (Direction direction : InitEngine.ADJACENT_DIRECTIONS) {
				MapLocation location = currentLocation.add(direction);
				if (InitEngine.checkIfIsOnTheMap(location)) {
					RobotInfo robot = robotController.senseRobotAtLocation(location);
					if (robot != null && robot.getTeam() == Cache.ENEMY_TEAM && robot.getType().isBuilding()) {
						int priority = getPriority(robot.getType());
						if (priority > currentPriority) {
							currentPriority = priority;
							currentDirection = direction;
						}
					}
				}
			}
		}
		if (currentDirection != null) {
			if (robotController.canDepositDirt(currentDirection)) {
				robotController.depositDirt(currentDirection);
			} else {
				dig();
				robotController.setIndicatorDot(currentLocation, 255, 255, 0);
			}
			robotController.setIndicatorLine(currentLocation, currentLocation.add(currentDirection), 255, 128, 0);
			return true;
		}
		return false;
	}
	public int getPriority(RobotType type) {
		switch (type) {
			case FULFILLMENT_CENTER:
				return 6;
			case NET_GUN:
				return 5;
			case DESIGN_SCHOOL:
				return 4;
			case VAPORATOR:
				return 3;
			case REFINERY:
				return 2;
			case HQ:
				return 1;
			default:
				return 0;
		}
	}
	public boolean defend() throws GameActionException {
		int ourHQState = InformationBank.getMyHQState();
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		MapLocation ourHQLocation = InformationBank.getMyHQLocation();
		if (ourHQLocation == null) {
			return false;
		}
		if (ourHQState == HQ.NO_HELP_NEEDED) {
			return false;
		}
		if (currentLocation.isAdjacentTo(ourHQLocation)) {
			// Try dig towards HQ
			Direction direction = currentLocation.directionTo(ourHQLocation);
			if (robotController.canDigDirt(direction)) {
				robotController.digDirt(direction);
			} else {
				if (!wall()) {
					depositDirt();
				}
			}
			return true;
		} else {
			if (ourHQState == HQ.NO_ADDITIONAL_HELP_NEEDED) {
				RobotInfo enemyBuilding = null;
				int currentDistance = Integer.MAX_VALUE;
				for (RobotInfo enemy : Cache.NEARBY_ENEMY_ROBOTS) {
					MapLocation location = enemy.getLocation();
					if (location.isWithinDistanceSquared(ourHQLocation, RobotType.HQ.sensorRadiusSquared)) {
						if (enemy.getType() == RobotType.FULFILLMENT_CENTER || enemy.getType() == RobotType.NET_GUN) {
							int distance = currentLocation.distanceSquaredTo(location);
							if (distance < currentDistance) {
								currentDistance = distance;
								enemyBuilding = enemy;
							}
						}
					}
				}
				if (enemyBuilding != null) {
					return attack(enemyBuilding.getLocation());
				}
			} else {
				PathfindingEngine.execute(ourHQLocation);
				return true;
			}
		}
		return false;
	}
	public boolean wall() throws GameActionException {
		MapLocation ourHQLocation = InformationBank.getMyHQLocation();
		if (ourHQLocation == null) {
			return false;
		}
		switch (InformationBank.wallState) {
			case InformationBank.WALL_STATE_NONE:
				return false;
			case InformationBank.WALL_STATE_NEEDS:
				if (ourHQLocation.isAdjacentTo(Cache.CURRENT_LOCATION)) {
					// Shuffle around
					Direction direction = InitEngine.getRandomAdjacentDirection();
					MapLocation location = Cache.CURRENT_LOCATION.add(direction);
					if (location.isAdjacentTo(ourHQLocation) && InitEngine.checkCanSafeMove(direction) && (!InitEngine.checkIfIsInCorner(location))) {
						robotController.move(direction);
					}
				} else {
					PathfindingEngine.execute(ourHQLocation);
				}
				return true;
			case InformationBank.WALL_STATE_STAYS:
				if (ourHQLocation.isAdjacentTo(Cache.CURRENT_LOCATION)) {
					Direction lowestDirection = null;
					int lowestElevation = Integer.MAX_VALUE;
					for (Direction direction : InitEngine.ALL_DIRECTIONS) {
						MapLocation location = Cache.CURRENT_LOCATION.add(direction);
						if (robotController.canSenseLocation(location) && location.isAdjacentTo(ourHQLocation) &&
								(!location.equals(ourHQLocation)) && (!InitEngine.checkIfIsInCorner(location))) {
							int elevation = robotController.senseElevation(location);
							if (InitEngine.canPotentiallyBeFlooded(elevation)) {
								if (robotController.canDepositDirt(direction)) {
									robotController.depositDirt(direction);
									return true;
								}
							}
							if (elevation < lowestElevation) {
								lowestDirection = direction;
								lowestElevation = elevation;
							}
						}
					}
					if (!dig()) {
						if (lowestDirection != null) {
							if (robotController.canDepositDirt(lowestDirection)) {
								robotController.depositDirt(lowestDirection);
							}
						}
					}
					return true;
				}
				break;
		}
		return false;
	}
	public boolean heal(MapLocation location) throws GameActionException {
		if (location != null) {
			MapLocation currentLocation = Cache.CURRENT_LOCATION;
			if (currentLocation.isAdjacentTo(location)) {
				Direction direction = currentLocation.directionTo(location);
				if (robotController.canDigDirt(direction)) {
					robotController.digDirt(direction);
					return true;
				}
			}
		}
		return false;
	}
	public boolean attack(MapLocation location) throws GameActionException {
		if (location != null) {
			if (Cache.CURRENT_LOCATION.isAdjacentTo(location)) {
				if (robotController.getDirtCarrying() > 0) {
					Direction direction = Cache.CURRENT_LOCATION.directionTo(location);
					if (robotController.canDepositDirt(direction)) {
						robotController.depositDirt(direction);
					}
				} else {
					if (robotController.canDigDirt(Direction.CENTER)) {
						robotController.digDirt(Direction.CENTER);
					}
				}
			} else {
				PathfindingEngine.execute(location);
			}
			return true;
		}
		return false;
	}
	public MapLocation findClosestBuilding(RobotInfo[] robots) {
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		int currentDistance = Integer.MAX_VALUE;
		MapLocation closestBuilding = null;
		for (RobotInfo enemy : robots) {
			// TODO: Square distance instead of r^2 distance?
			if (enemy.getType().isBuilding()) {
				int distance = enemy.getLocation().distanceSquaredTo(currentLocation);
				if (distance < currentDistance) {
					currentDistance = distance;
					closestBuilding = enemy.getLocation();
				}
			}
		}
		return closestBuilding;
	}
	public static int getTargetElevation() {
		return Math.max((targetElevation - targetElevation % 3) + 5, InformationBank.getMyHQElevation());
	}
	public static boolean testStep(MapLocation location, int elevation, int targetStepElevation) throws GameActionException {
		if (!Cache.controller.senseFlooding(location)) {
			boolean flag = false;
			for (Direction direction : InitEngine.ADJACENT_DIRECTIONS) {
				MapLocation adjacent = location.add(direction);
				if (!InitEngine.checkIfIsOnTheMap(adjacent)) {
					continue;
				}
				if (!Cache.controller.canSenseLocation(adjacent)) {
					return false;
				}
				if (Cache.controller.senseFlooding(adjacent)) {
					continue;
				}
				if (GridChecker.checkIfIsHole(adjacent)) {
					continue;
				}
				if (Cache.controller.senseRobotAtLocation(adjacent) != null) {
					continue;
				}
				int adjacentElevation = Cache.controller.senseElevation(adjacent);
				if (Math.abs(adjacentElevation - targetElevation) > LANDSCAPING_LIMIT) {
					continue;
				}
				if (adjacentElevation < targetStepElevation) {
					flag = true;
					break;
				}
			}
			if (flag && elevation >= targetStepElevation) {
				Cache.controller.setIndicatorDot(location, 0, 0, 0);
				return false;
			}
			Cache.controller.setIndicatorDot(location, 255, 255, 255);
		}
		return true;
	}
	public boolean terraform() throws GameActionException {
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		MapLocation ourHQLocation = InformationBank.getMyHQLocation();
		if (ourHQLocation == null) {
			return false;
		}
		int targetElevation = getTargetElevation();
		int upperTargetElevation = targetElevation + GameConstants.MAX_DIRT_DIFFERENCE;
		int targetStepElevation = targetElevation - GameConstants.MAX_DIRT_DIFFERENCE;
		boolean creatingWall = InformationBank.wallState != InformationBank.WALL_STATE_NONE;
		MapLocation chosenTerraformLocation = null;
		int currentDistance = Integer.MAX_VALUE;
		boolean toElevate = false;
		for (int i = 0; i < InitEngine.FLOOD_FILL_X.length; i++) {
			int dx = InitEngine.FLOOD_FILL_X[i];
			int dy = InitEngine.FLOOD_FILL_Y[i];
			MapLocation location = currentLocation.translate(dx, dy);
			if (!InitEngine.checkIfIsOnTheMap(location)) {
				continue;
			}
			if (!Cache.controller.canSenseLocation(location)) {
				break;
			}
			if (!GridChecker.checkIfIsHole(location)) {
				int elevation = Cache.controller.senseElevation(location);
				// Fill up to target elevation or dig to targetElevation + 3
				if (elevation < targetElevation) {
					if (targetElevation - elevation <= LANDSCAPING_LIMIT) {
						// Try to raise elevation
						int distance = (int) (Math.sqrt(ourHQLocation.distanceSquaredTo(location)) + Math.sqrt(currentLocation.distanceSquaredTo(location)));
						if (distance < currentDistance) {
							RobotInfo robot = Cache.controller.senseRobotAtLocation(location);
							if (robot != null && robot.getTeam() == Cache.MY_TEAM && robot.getType().isBuilding()) {
								continue;
							}
							if (targetElevation <= 8 && (!testStep(location, elevation, targetStepElevation))) {
								continue;
							}
							currentDistance = distance;
							chosenTerraformLocation = location;
							toElevate = true;
						}
					}
				} else if (elevation > upperTargetElevation) {
					// Check if we're next to hq and we don't want to do that anymore
					if (creatingWall && ourHQLocation.isAdjacentTo(location)) {
						continue;
					}
					if (elevation - upperTargetElevation <= LANDSCAPING_LIMIT) {
						// Try to lower elevation
						int distance = (int) (Math.sqrt(ourHQLocation.distanceSquaredTo(location)) + Math.sqrt(currentLocation.distanceSquaredTo(location)));
						if (distance < currentDistance) {
							RobotInfo robot = Cache.controller.senseRobotAtLocation(location);
							if (robot != null && robot.getTeam() == Cache.MY_TEAM && robot.getType().isBuilding()) {
								continue;
							}
							currentDistance = distance;
							chosenTerraformLocation = location;
							toElevate = false;
						}
					}
				}
			}
			// TODO: Bytecode control
			if (chosenTerraformLocation != null && Clock.getBytecodesLeft() < 1000) {
				break;
			}
		}
		if (chosenTerraformLocation != null) {
			Cache.controller.setIndicatorDot(chosenTerraformLocation, 0, 255, 255);
			if (toElevate) {
				elevate(chosenTerraformLocation);
			} else {
				lower(chosenTerraformLocation);
			}
			return true;
		}
		return false;
	}
	public boolean adjustHQElevation() throws GameActionException {
		if (InformationBank.getMyHQLocation() == null) {
			return false;
		}
		int targetElevation = getTargetElevation();
		MapLocation ourHQLocation = InformationBank.getMyHQLocation();
		for (Direction direction : Direction.values()) {
			MapLocation location = Cache.CURRENT_LOCATION.add(direction);
			if (InitEngine.checkIfIsOnTheMap(location) && Cache.controller.canSenseLocation(location)) {
				if (location.isAdjacentTo(ourHQLocation) && (!location.equals(ourHQLocation))) {
					int elevation = Cache.controller.senseElevation(location);
					if (!GridChecker.checkIfIsHole(location)) {
						if (elevation < targetElevation) {
							if (robotController.canDepositDirt(direction)) {
								robotController.depositDirt(direction);
								robotController.setIndicatorLine(Cache.CURRENT_LOCATION, location, 128, 128, 128);
							} else {
								dig();
							}
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	public void elevate(MapLocation location) throws GameActionException {
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		if (currentLocation.isWithinDistanceSquared(location, 2)) {
			Direction direction = currentLocation.directionTo(location);
			if (robotController.canDepositDirt(direction)) {
				robotController.depositDirt(direction);
			} else {
				dig();
			}
		} else {
			PathfindingEngine.execute(location);
		}
	}
	public void lower(MapLocation location) throws GameActionException {
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		if (currentLocation.isWithinDistanceSquared(location, 2)) {
			Direction direction = currentLocation.directionTo(location);
			if (robotController.canDigDirt(direction)) {
				robotController.digDirt(direction);
			} else {
				depositDirt();
			}
		} else {
			PathfindingEngine.execute(location);
		}
	}
	public static boolean dig() throws GameActionException {
		for (Direction holeDirection : GridChecker.getHoleDirections(Cache.CURRENT_LOCATION)) {
			MapLocation location = Cache.CURRENT_LOCATION.add(holeDirection);
			if (Cache.controller.canSenseLocation(location)) {
				RobotInfo robot = Cache.controller.senseRobotAtLocation(location);
				if (robot != null) {
					// Don't trap our own units or help the enemy team
					if (robot.getTeam() == Cache.MY_TEAM || robot.getType().isBuilding()) {
						continue;
					}
				}
				if (Cache.controller.canDigDirt(holeDirection)) {
					Cache.controller.digDirt(holeDirection);
					return true;
				}
			}
		}
		// Dig from very high elevations (but not around our hq)
		int threshold = Math.min(getTargetElevation(), 100);
		MapLocation ourHQ = InformationBank.getMyHQLocation();
		if (ourHQ != null) {
			for (Direction direction : Direction.values()) {
				MapLocation location = Cache.CURRENT_LOCATION.add(direction);
				if (Cache.controller.canSenseLocation(location)) {
					// Don't destroy our own wall
					if (ourHQ.isAdjacentTo(location)) {
						continue;
					}
					RobotInfo robot = Cache.controller.senseRobotAtLocation(location);
					if (robot != null && robot.getTeam() == Cache.ENEMY_TEAM && robot.getType().isBuilding()) {
						// Don't help enemy team
						continue;
					}
					int elevation = Cache.controller.senseElevation(location);
					if (elevation > threshold) {
						if (Cache.controller.canDigDirt(direction)) {
							Cache.controller.digDirt(direction);
							return true;
						}
					}
				}
			}
		}
		// Dig from corner
		if (InformationBank.myHQNearCorner) {
			if (InitEngine.checkIfIsNearCorner(Cache.CURRENT_LOCATION)) {
				Direction direction = InitEngine.getDirectionToNearestCorner(Cache.CURRENT_LOCATION);
				if (Cache.controller.canDigDirt(direction)) {
					Cache.controller.digDirt(direction);
					return true;
				}
			}
		}
		return false;
	}
	public static void depositDirt() throws GameActionException {
		for (Direction holeDirection : GridChecker.getHoleDirections(Cache.CURRENT_LOCATION)) {
			MapLocation location = Cache.CURRENT_LOCATION.add(holeDirection);
			if (Cache.controller.canSenseLocation(location)) {
				RobotInfo robot = Cache.controller.senseRobotAtLocation(location);
				if (robot != null && robot.getType().isBuilding() && robot.getTeam() == Cache.MY_TEAM) {
					continue;
				}
				if (Cache.controller.canDepositDirt(holeDirection)) {
					Cache.controller.depositDirt(holeDirection);
				}
			}
		}

	}
}
