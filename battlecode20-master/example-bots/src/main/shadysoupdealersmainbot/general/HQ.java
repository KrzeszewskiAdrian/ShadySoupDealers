package shadysoupdealersmainbot.general;

import battlecode.common.*;
import shadysoupdealersmainbot.RunnableBot;
import shadysoupdealersmainbot.general.util.Cache;
import shadysoupdealersmainbot.general.util.InformationBank;
import shadysoupdealersmainbot.general.util.InitEngine;

public class HQ implements RunnableBot {
		
	public static final int NO_HELP_NEEDED = 0; // All OK, wall is under construction (if needed)
	public static final int NO_ADDITIONAL_HELP_NEEDED = 1; // Need to build wall
	public static final int NEEDS_HELP = 2; // Enemy is close
	
	private RobotController robotController;
	private int spawnCounter;
	private int turnCounter = 0;
	private int turnsToPickupLandscapers = 0;
	private int attackWaves = 0;
	private boolean allNeighborsOccupied = false;
	private MapLocation[] vaporatorLocations = new MapLocation[20];
	private int vaporatorLocationsIndex = 0;
	private boolean hasSeenVaporatorFlooded = false;
	
	private static final int LANDSCAPERS_THESHOLD_WALL_OK = 4;
	private static final int VAPORATOR_TRESHOLD_FOR_WALL = 8;

	public HQ(RobotController robotController) {
		this.robotController = robotController;
		this.spawnCounter = 0;
	}
	@Override
	public void init() throws GameActionException {
		InformationBank.broadcastMyHQ(Cache.CURRENT_LOCATION, robotController.senseElevation(Cache.CURRENT_LOCATION));
	}
	@Override
	public void turn() throws GameActionException {
		MapLocation currentLocation = Cache.CURRENT_LOCATION;
		
		// Complete tactics for drone sabotage and drone assault
		if (robotController.getRoundNum() > 800) {
			turnCounter++;
			turnsToPickupLandscapers++;
			int newAttackState = -1;
			if (InformationBank.getAttackState() == InformationBank.ATTACK_STATE_ENEMY_HQ_IGNORE_NETGUNS) {
				if (turnCounter > 70) {
					newAttackState = InformationBank.ATTACK_STATE_NONE;
				}
			} else if (turnsToPickupLandscapers >= 0 && (InformationBank.dronesReady >= 30 || InformationBank.dronesReady >= 15 && turnCounter > 250)) {
				newAttackState = InformationBank.ATTACK_STATE_ENEMY_HQ_IGNORE_NETGUNS;
				turnCounter = 0;
				attackWaves++;
			} else if ((InformationBank.dronesBuilt >= 5 && InformationBank.getMyHQState() == NO_HELP_NEEDED) || InformationBank.dronesBuilt >= 10) {
				if (attackWaves % 2 == 1) {
					newAttackState = InformationBank.ATTACK_STATE_ENEMY_HQ;
				} else {
					newAttackState = InformationBank.ATTACK_STATE_ENEMY_HQ_WITH_LANDSCAPERS;
				}
			}
			if (newAttackState != -1) {
				if (newAttackState != InformationBank.getAttackState()) {
					InformationBank.broadcastAttackState(newAttackState);
					if (newAttackState == InformationBank.ATTACK_STATE_ENEMY_HQ_WITH_LANDSCAPERS) {
						turnsToPickupLandscapers = -20;
					}
				}
			}
		}

		//Refresh vaporator list, but only if any got flooded
		if (!hasSeenVaporatorFlooded) {
			for (int i = 0; i < vaporatorLocationsIndex; i++) {
				if (robotController.canSenseLocation(vaporatorLocations[i])) {
					if (robotController.senseFlooding(vaporatorLocations[i])) {
						hasSeenVaporatorFlooded = true;
						break;
					}
				}
			}
			vaporatorLocationsIndex = 0;
			for (RobotInfo friend : Cache.NEARBY_FRIENDLY_ROBOTS) {
				if (friend.getType() == RobotType.VAPORATOR) {
					vaporatorLocations[vaporatorLocationsIndex] = friend.getLocation();
					vaporatorLocationsIndex++;
					if (vaporatorLocationsIndex == vaporatorLocations.length) {
						break;
					}
				}
			}
		}
		
		//Check if all neighbors occupied
		if (robotController.canSenseRadiusSquared(InitEngine.ADJACENT_DISTANCE_SQUARED)) {
			int HQElevation = robotController.senseElevation(Cache.CURRENT_LOCATION);
			allNeighborsOccupied = true;
			for (Direction direction : InitEngine.ADJACENT_DIRECTIONS) {
				MapLocation location = currentLocation.add(direction);
				if (robotController.onTheMap(location)) {
					int elevation = robotController.senseElevation(location);
					if (!robotController.isLocationOccupied(location) && Math.abs(HQElevation - elevation) <= 15) {
						allNeighborsOccupied = false;
						break;
					}
				}
			}
		}
		
		
		if (robotController.getRoundNum() >= 3) {
			int state;
			
			//Decide whether we need help
			if (FulfillmentCenter.findEnemyUnit() != null) {
				if (allNeighborsOccupied) {
					state = NO_ADDITIONAL_HELP_NEEDED;
				} else {
					int count = 0;
					for (RobotInfo robot : Cache.NEARBY_FRIENDLY_ROBOTS) {
						if (robot.getLocation().isAdjacentTo(currentLocation) && robot.getType() == RobotType.LANDSCAPER) {
							count++;
						}
					}
					state = count >= LANDSCAPERS_THESHOLD_WALL_OK ? NO_ADDITIONAL_HELP_NEEDED : NEEDS_HELP;
				}
			} else {
				state = NO_HELP_NEEDED;
			}
			
			if (InformationBank.getMyHQState() != state) {
				InformationBank.broadcastMyHQState(state);
			}
			
			// Update wall state
			if (hasSeenVaporatorFlooded || InformationBank.getVaporatorCount() >= VAPORATOR_TRESHOLD_FOR_WALL) {
				int newWallState = InformationBank.wallState;
				if (InformationBank.wallState == InformationBank.WALL_STATE_NONE) {
					newWallState = InformationBank.WALL_STATE_NEEDS;
				}
				if (newWallState == InformationBank.WALL_STATE_NEEDS) {
					if (robotController.canSenseRadiusSquared(InitEngine.ADJACENT_DISTANCE_SQUARED)) {
						boolean wallSpotsOccupied = true;
						for (Direction direction : InitEngine.ADJACENT_DIRECTIONS) {
							MapLocation location = currentLocation.add(direction);
							if (InitEngine.checkIfIsOnTheMap(location)) {
								if (InformationBank.myHQNearCorner && InitEngine.checkIfIsInCorner(location)) {
									continue;
								}
								int elevation = robotController.senseElevation(location);
								if (elevation > 200) {
									// TODO
									continue;
								}
								RobotInfo robot = robotController.senseRobotAtLocation(location);
								if (robot == null || robot.getTeam() == Cache.ENEMY_TEAM || robot.getType() != RobotType.LANDSCAPER) {
									wallSpotsOccupied = false;
									break;
								}
							}
						}
						if (wallSpotsOccupied) {
							newWallState = InformationBank.WALL_STATE_STAYS;
						}
					}
				}
				if (InformationBank.wallState != newWallState) {
					InformationBank.broadcastWallState(newWallState);
				}
			}
			
			int designSchoolCount = 0;
			int fulfillmentCenterCount = 0;
			for (RobotInfo robot : Cache.NEARBY_FRIENDLY_ROBOTS) {
				if (robot.getType() == RobotType.DESIGN_SCHOOL) {
					designSchoolCount++;
				}
				if (robot.getType() == RobotType.FULFILLMENT_CENTER) {
					fulfillmentCenterCount++;
				}
			}
			if (designSchoolCount != InformationBank.getDesignSchoolCount() ||
					fulfillmentCenterCount != InformationBank.getFulfillmentCenterCount()) {
				InformationBank.broadcastMyHQUnitCount(designSchoolCount, fulfillmentCenterCount);
			}
		}
		
		if (!robotController.isReady()) {
			return;
		}
		
		if (shootDrone()) {
			return;
		}
		
		//Pick direction to spawn
		if (spawnCounter < 5) {
			for (int i = 1; i < InitEngine.FLOOD_FILL_X.length; i++) { // Skip first index to prevent idealDirection = CENTER
				MapLocation location = currentLocation.translate(InitEngine.FLOOD_FILL_X[i], InitEngine.FLOOD_FILL_Y[i]);
				if (!robotController.canSenseLocation(location)) {
					break;
				}
				if (robotController.senseSoup(location) > 0) {
					Direction idealDirection = InitEngine.getDirection(InitEngine.FLOOD_FILL_X_COMPRESSED[i], InitEngine.FLOOD_FILL_Y_COMPRESSED[i]);
					for (Direction direction : InitEngine.getAttemptOrder(idealDirection)) {
						if (InitEngine.checkCanSafeBuildRobot(RobotType.MINER, direction)) {
							robotController.buildRobot(RobotType.MINER, direction);
							this.spawnCounter++;
							break;
						}
					}
					return;
				}
			}
			buildMiner();
		}
	}
	
	public void buildMiner() throws GameActionException {
		Direction direction = InitEngine.getRandomAdjacentDirection();
		if (InitEngine.checkCanSafeBuildRobot(RobotType.MINER, direction)) {
			robotController.buildRobot(RobotType.MINER, direction);
			this.spawnCounter++;
		}
	}
	
	//Shot closest drone if possible
	public boolean shootDrone() throws GameActionException {
		RobotInfo[] enemies = robotController.senseNearbyRobots(Cache.CURRENT_LOCATION,
				GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED, robotController.getTeam().opponent());
		int currentPriority = 0;
		int currentDistance = Integer.MAX_VALUE;
		RobotInfo target = null;
		for (RobotInfo enemy : enemies) {
			if (enemy.getType() == RobotType.DELIVERY_DRONE) {
				int enemyID = enemy.getID();
				int enemyPriority = getPriority(enemy);
				int enemyDistance = Cache.CURRENT_LOCATION.distanceSquaredTo(enemy.getLocation());
				if (enemyPriority > currentPriority || ((enemyPriority == currentPriority) && enemyDistance < currentDistance)) {
					if (robotController.canShootUnit(enemyID)) {
						currentPriority = enemyPriority;
						currentDistance = enemyDistance;
						target = enemy;
					}
				}
			}
		}
		if (target != null) {
			robotController.shootUnit(target.getID());
			return true;
		}
		return false;
	}
	
	public int getPriority(RobotInfo enemy) throws GameActionException {
		if (enemy.isCurrentlyHoldingUnit()) {
			if (allNeighborsOccupied) {
				return 1;
			} else {
				return 2;
			}
		} else {
			if (allNeighborsOccupied) {
				return 2;
			} else {
				return 1;
			}
		}
	}
}
