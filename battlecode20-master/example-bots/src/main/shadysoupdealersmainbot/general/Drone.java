package shadysoupdealersmainbot.general;

import battlecode.common.*;
import shadysoupdealersmainbot.RunnableBot;
import shadysoupdealersmainbot.general.util.Cache;
import shadysoupdealersmainbot.general.util.MapChecker;
import shadysoupdealersmainbot.general.util.PathfindingEngine;
import shadysoupdealersmainbot.general.util.InformationBank;
import shadysoupdealersmainbot.general.util.InitEngine;

public class Drone implements RunnableBot {
	public static final int PREPARED_TO_RUSH_DISTANCE_SQUARED = 100;
	public static boolean readyToAttack = false;
	private boolean message = false;
	private RobotController robotController;
	private boolean carryingFriendlyLandscaper;
	public Drone(RobotController robotController) {
		this.robotController = robotController;
		this.carryingFriendlyLandscaper = false;
	}
	@Override
	public void init() {

	}
	@Override
	public void turn() throws GameActionException {
		if (!message) {
			InformationBank.broadcastBuiltDrone();
			message = true;
		}
		if (!readyToAttack) {
			MapLocation enemyHQLocation = InformationBank.getEnemyHQLocation();
			if (enemyHQLocation != null &&
					Cache.CURRENT_LOCATION.isWithinDistanceSquared(enemyHQLocation, PREPARED_TO_RUSH_DISTANCE_SQUARED)) {
				InformationBank.broadcastDroneReady();
				readyToAttack = true;
			}
		}
		MapLocation currentLocation = Cache.CURRENT_LOCATION;

		MapLocation nearestWater = null;
		for (int i = 1; i < InitEngine.FLOOD_FILL_X.length; i++) {
			int dx = InitEngine.FLOOD_FILL_X[i];
			int dy = InitEngine.FLOOD_FILL_Y[i];
			MapLocation location = currentLocation.translate(dx, dy);
			if (!InitEngine.checkIfIsOnTheMap(location)) {
				continue;
			}
			if (!robotController.canSenseLocation(location)) {
				break;
			}
			if (robotController.senseFlooding(location)) {
				if (nearestWater == null) {
					nearestWater = location;
				}
				MapChecker.appendWaterLocation(location);
			}
		}


		if (!robotController.isReady()) {
			return;
		}

		if (!PathfindingEngine.ignoreNetGuns) {
			int maxNetGunDistance = Integer.MAX_VALUE;
			MapLocation closestNetGun = null;
			for (int i = Cache.NEARBY_ENEMY_NET_GUNS_SIZE; --i >= 0; ) {
				MapLocation nearbyEnemyNetGunLocation = Cache.NEARBY_ENEMY_NET_GUNS[i];
				int distanceSquared = currentLocation.distanceSquaredTo(nearbyEnemyNetGunLocation);
				if (distanceSquared < maxNetGunDistance) {
					maxNetGunDistance = distanceSquared;
					closestNetGun = nearbyEnemyNetGunLocation;
				}
			}
			//RUUUUUUUUUN!!!!
			if (maxNetGunDistance <= GameConstants.NET_GUN_SHOOT_RADIUS_SQUARED) {
				InitEngine.attemptRunAwayFromLocation(closestNetGun, false);
				return;
			}
		}


		if (robotController.isCurrentlyHoldingUnit()) {
			if (carryingFriendlyLandscaper) {
				// Try to drop the landscaper next to the enemy hq
				MapLocation enemyHQLocation = InformationBank.getEnemyHQLocation();
				if (enemyHQLocation == null) {
					InitEngine.goExploreRandomDirection();
					return;
				}
				for (Direction direction : InitEngine.ADJACENT_DIRECTIONS) {
					MapLocation location = Cache.CURRENT_LOCATION.add(direction);
					if (enemyHQLocation.isWithinDistanceSquared(location, InitEngine.ADJACENT_DISTANCE_SQUARED)) {
						if (robotController.canDropUnit(direction)) {
							robotController.dropUnit(direction);
							carryingFriendlyLandscaper = false;
							return;
						}
					}
				}
				goTowardsHQ("enemy");
			} else {
				// Find adjacent water
				if (robotController.canSenseRadiusSquared(InitEngine.ADJACENT_DISTANCE_SQUARED)) {
					for (Direction direction : InitEngine.ADJACENT_DIRECTIONS) {
						MapLocation location = currentLocation.add(direction);
						if (InitEngine.checkIfIsOnTheMap(location) && robotController.senseFlooding(location)) {
							if (robotController.canDropUnit(direction)) {
								robotController.dropUnit(direction);
								MapChecker.appendSharedWaterLocation(location);
								return;
							}
						}
					}
				}
				if (nearestWater != null) {
					PathfindingEngine.execute(nearestWater);
					return;
				}
				MapLocation location = closestFoundWaterTile();
				if (location == null) {
					InitEngine.goRandomDirection();
				} else {
					PathfindingEngine.execute(location);
				}
			}
		} else {
			RobotInfo target = findTarget();
			if (InformationBank.getMyHQState() == HQ.NO_HELP_NEEDED) {
				if (target == null) {
					target = targetCow();
				}
			}
			int attackState = InformationBank.getAttackState();
			if (target == null) {
				if (attackState == InformationBank.ATTACK_STATE_ENEMY_HQ_WITH_LANDSCAPERS) {
					int shortestDistance = Integer.MAX_VALUE;
					RobotInfo foundLandscaper = null;
					MapLocation ourHQLocation = InformationBank.getMyHQLocation();
					if (ourHQLocation != null) {
						for (RobotInfo friend : Cache.NEARBY_FRIENDLY_ROBOTS) {
							if (!friend.getType().equals(RobotType.LANDSCAPER)) {
								continue;
							}
							MapLocation friendLocation = friend.getLocation();
							if (ourHQLocation.isAdjacentTo(friendLocation)) {
								continue;
							}
							int distance = Cache.CURRENT_LOCATION.distanceSquaredTo(friendLocation);
							if (distance < shortestDistance) {
								foundLandscaper = friend;
								shortestDistance = distance;
							}
						}
						if (foundLandscaper != null) {
							if (currentLocation.isAdjacentTo(foundLandscaper.getLocation())) {
								if (robotController.canPickUpUnit(foundLandscaper.getID())) {
									robotController.pickUpUnit(foundLandscaper.getID());
									carryingFriendlyLandscaper = true;
								}
							} else {
								PathfindingEngine.execute(foundLandscaper.getLocation());
							}
							return;
						}
					}
				}
			}
			if (target == null) {
				if (attackState != InformationBank.ATTACK_STATE_NONE) {
					goTowardsHQ("enemy");
				} else {
					if (InformationBank.getMyHQState() != HQ.NO_HELP_NEEDED) {
						goTowardsHQ("our");
					} else {
						goTowardsHQ("enemy");
					}
				}
			} else {
				if (currentLocation.isAdjacentTo(target.getLocation())) {
					if (robotController.canPickUpUnit(target.getID())) {
						robotController.pickUpUnit(target.getID());
						carryingFriendlyLandscaper = false;
					}
				} else {
					PathfindingEngine.execute(target.getLocation());
				}
			}
		}
	}

	public void goTowardsHQ(String HQType) throws GameActionException {
		if( HQType == "enemy") {
			MapLocation enemyHQ = InformationBank.getEnemyHQLocation();
			if (enemyHQ == null) {
				InitEngine.goExploreRandomDirection();
			} else {
				PathfindingEngine.execute(enemyHQ);
			}
		}
		else{
			MapLocation ourHQ = InformationBank.getMyHQLocation();
			if (ourHQ == null) {
				InitEngine.goExploreRandomDirection();
			} else {
				PathfindingEngine.execute(ourHQ);
			}
		}
	}
	public RobotInfo findTarget() {
		RobotInfo target = null;
		int currentPriority = -1;
		int currentDistance = -1;
		for (RobotInfo enemy : Cache.NEARBY_ENEMY_ROBOTS) {
			int priority = determinePriority(enemy.getLocation(), enemy.getType());
			if (priority == 0) {
				continue;
			}
			int distance = Cache.CURRENT_LOCATION.distanceSquaredTo(enemy.getLocation());
			if (distance <= InitEngine.ADJACENT_DISTANCE_SQUARED) {
				priority += 1000;
			}
			if (target == null || priority > currentPriority || (priority == currentPriority && distance < currentDistance)) {
				target = enemy;
				currentPriority = priority;
				currentDistance = distance;
			}
		}
		return target;
	}
	public RobotInfo targetCow() {
		MapLocation ourHQ = InformationBank.getMyHQLocation();
		MapLocation enemyHQ = InformationBank.getEnemyHQLocation();
		if (ourHQ == null || enemyHQ == null) {
			return null;
		}
		RobotInfo cow = null;
		int currentDistance = Integer.MAX_VALUE;
		for (RobotInfo robot : Cache.NEARBY_ROBOTS) {
			if (robot.getTeam() == Team.NEUTRAL) {
				MapLocation location = robot.getLocation();
				int distanceSquared = Cache.CURRENT_LOCATION.distanceSquaredTo(robot.getLocation());
				if (distanceSquared < currentDistance) {
					// Enemy's side enemy's problem
					//TODO:deal with it
					if (Cache.controller.getRoundNum() > 1000 ||
							(ourHQ.distanceSquaredTo(location) < enemyHQ.distanceSquaredTo(location))) {
						cow = robot;
						currentDistance = distanceSquared;
					}
				}
			}
		}
		return cow;
	}
	public int determinePriority(MapLocation location, RobotType type) {
		if (robotController.getRoundNum() < 2500) {
			MapLocation ourHQ = InformationBank.getMyHQLocation();
			MapLocation enemyHQ = InformationBank.getEnemyHQLocation();
			if (ourHQ != null && enemyHQ != null) {
				if (enemyHQ.distanceSquaredTo(location) < ourHQ.distanceSquaredTo(location)) {
					switch (type) {
						case MINER:
							return 2;
						case LANDSCAPER:
							return 1;
						default:
							// Cannot pick up
							return 0;
					}
				}
			}
		}
		switch (type) {
			case LANDSCAPER:
				return 2;
			case MINER:
				return 1;
			default:
				// Cannot pick up
				return 0;
		}
	}
	public MapLocation closestFoundWaterTile() {
		MapLocation waterLocation = null;
		int currentDistance = Integer.MAX_VALUE;
		if (MapChecker.closestWaterToMyHQLocation != null) {
			int distance = MapChecker.closestWaterToMyHQLocation.distanceSquaredTo(Cache.CURRENT_LOCATION);
			if (distance < currentDistance) {
				waterLocation = MapChecker.closestWaterToMyHQLocation;
				currentDistance = distance;
			}
		}
		if (MapChecker.closestWaterToEnemyHQLocation != null) {
			int distance = MapChecker.closestWaterToEnemyHQLocation.distanceSquaredTo(Cache.CURRENT_LOCATION);
			if (distance < currentDistance) {
				waterLocation = MapChecker.closestWaterToEnemyHQLocation;
				currentDistance = distance;
			}
		}
		if (MapChecker.sharedClosestWaterToMyHQLocation != null) {
			int distance = MapChecker.sharedClosestWaterToMyHQLocation.distanceSquaredTo(Cache.CURRENT_LOCATION);
			if (distance < currentDistance) {
				waterLocation = MapChecker.sharedClosestWaterToMyHQLocation;
				currentDistance = distance;
			}
		}
		if (MapChecker.sharedClosestWaterToEnemyHQLocation != null) {
			int distance = MapChecker.sharedClosestWaterToEnemyHQLocation.distanceSquaredTo(Cache.CURRENT_LOCATION);
			if (distance < currentDistance) {
				waterLocation = MapChecker.sharedClosestWaterToEnemyHQLocation;
				currentDistance = distance;
			}
		}
		return waterLocation;
	}
}
