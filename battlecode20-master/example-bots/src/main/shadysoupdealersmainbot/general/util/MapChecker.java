package shadysoupdealersmainbot.general.util;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class MapChecker {
	public static MapPart sharedSoupLocations = new MapPart();
	
	public static MapLocation sharedClosestWaterToMyHQLocation = null;
	public static MapLocation closestWaterToMyHQLocation = null;
	
	public static MapLocation sharedClosestWaterToEnemyHQLocation = null;
	public static MapLocation closestWaterToEnemyHQLocation = null;

	public static void appendWaterLocation(MapLocation location) {
		MapLocation myHQLocation = InformationBank.getMyHQLocation();
		if (closestWaterToMyHQLocation == null || myHQLocation != null && location.distanceSquaredTo(myHQLocation) < closestWaterToMyHQLocation.distanceSquaredTo(myHQLocation)) {
			closestWaterToMyHQLocation = location;
		}
		
		MapLocation enemyHQLocation = InformationBank.getEnemyHQLocation();
		if (closestWaterToEnemyHQLocation == null || enemyHQLocation != null && location.distanceSquaredTo(enemyHQLocation) < closestWaterToEnemyHQLocation.distanceSquaredTo(enemyHQLocation)) {
			closestWaterToEnemyHQLocation = location;
		}
	}
	
	public static void appendSharedWaterLocation(MapLocation location) {
		MapLocation myHQ = InformationBank.getMyHQLocation();
		if (sharedClosestWaterToMyHQLocation == null || myHQ != null && location.distanceSquaredTo(myHQ) < sharedClosestWaterToMyHQLocation.distanceSquaredTo(myHQ)) {
			sharedClosestWaterToMyHQLocation = location;
			InformationBank.broadcastWaterState(true, location);
			return;
		}
		
		MapLocation enemyHQ = InformationBank.getEnemyHQLocation();
		if (sharedClosestWaterToEnemyHQLocation == null || enemyHQ != null && location.distanceSquaredTo(enemyHQ) < sharedClosestWaterToEnemyHQLocation.distanceSquaredTo(enemyHQ)) {
			sharedClosestWaterToEnemyHQLocation = location;
			InformationBank.broadcastWaterState(true, location);
			return;
		}
	}
	
	public static void checkAndBroadcastOfWaterLocations() throws GameActionException {
		if (sharedClosestWaterToMyHQLocation != null) {
			if (Cache.controller.canSenseLocation(sharedClosestWaterToMyHQLocation)) {
				if (!Cache.controller.senseFlooding(sharedClosestWaterToMyHQLocation)) {
					InformationBank.broadcastWaterState(false, sharedClosestWaterToMyHQLocation);
					sharedClosestWaterToMyHQLocation = null;
				}
			}
		}
		if (closestWaterToMyHQLocation != null) {
			if (Cache.controller.canSenseLocation(closestWaterToMyHQLocation)) {
				if (!Cache.controller.senseFlooding(closestWaterToMyHQLocation)) {
					closestWaterToMyHQLocation = null;
				}
			}
		}
		
		if (sharedClosestWaterToEnemyHQLocation != null) {
			if (Cache.controller.canSenseLocation(sharedClosestWaterToEnemyHQLocation)) {
				if (!Cache.controller.senseFlooding(sharedClosestWaterToEnemyHQLocation)) {
					InformationBank.broadcastWaterState(false, sharedClosestWaterToEnemyHQLocation);
					sharedClosestWaterToEnemyHQLocation = null;
				}
			}
		}
		if (closestWaterToEnemyHQLocation != null) {
			if (Cache.controller.canSenseLocation(closestWaterToEnemyHQLocation)) {
				if (!Cache.controller.senseFlooding(closestWaterToEnemyHQLocation)) {
					closestWaterToEnemyHQLocation = null;
				}
			}
		}
	}
	
}
