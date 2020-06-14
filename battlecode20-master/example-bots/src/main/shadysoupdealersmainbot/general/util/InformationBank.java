package shadysoupdealersmainbot.general.util;

import battlecode.common.*;
import shadysoupdealersmainbot.general.Drone;
import shadysoupdealersmainbot.general.HQ;

public class InformationBank {
	public static final int MY_HQ_TRANSACTION_COST = 3;

	private static final int MY_HQ_CODE = -27151659;
	private static final int MY_HQ_STATE_CODE = 25268051;
	private static final int MY_HQ_UNIT_COUNT_CODE = -29526296;
	
	private static final int ENEMY_HQ_CODE = 21037987;
	private static final int ENEMY_HQ_MODE_CODE = -23313775;
	
	private static final int NEW_SOUP_CODE = 22852377;
	private static final int SOUP_GONE_CODE = -45231471;
	
	private static final int NEW_LANDSCAPER_CODE = -40232093;
	private static final int NEW_DRONE_CODE = 13778338;
	private static final int DRONE_READY_CODE = 46745101;
	private static final int ATTACK_STATE_CODE = 6695566;
	
	private static final int VAPORATOR_COUNT_INCREMENT_CODE = -37016341;
	private static final int TOGGLE_SAVE_FOR_NETGUN_CODE = 20686877;
	private static final int WALLSTATE_CHANGE_CODE = -47397619;
	private static final int WATER_CODE = -11756518;

	private static RobotController controller;
	private static MapLocation myHQLocation;
	private static int myHQElevation;
	private static int myHQParityX = -1;
	private static int myHQParityY = -1;
	public static boolean myHQNearCorner = false;
	private static MapLocation enemyHQLocation;
	private static int enemyHQGuesserMode = EnemyHQChecker.UNKNOWN_MODE;
	private static int myHQState = HQ.NO_HELP_NEEDED;

	public static int landscapersBuilt = 0;
	
	// Build order
	private static int designSchoolCount = 0;
	private static int fulfillmentCenterCount = 0;
	private static int vaporatorCount = 0;
	
	public static boolean isSavingForNetgun = false;
	
	
	// Wall state
	public static int wallState = 0;
	public static final int WALL_STATE_NONE = 0;
	public static final int WALL_STATE_NEEDS = 1;
	public static final int WALL_STATE_STAYS = 2;
	
	
	// Drone attack
	public static final int ATTACK_STATE_NONE = 0;
	public static final int ATTACK_STATE_ENEMY_HQ = 1;
	public static final int ATTACK_STATE_ENEMY_HQ_WITH_LANDSCAPERS = 2;
	public static final int ATTACK_STATE_ENEMY_HQ_IGNORE_NETGUNS = 3;
	
	private static int attackState = ATTACK_STATE_NONE;
	
	public static int dronesBuiltTotal = 0;
	public static int dronesBuilt = 0;
	public static int dronesReady = 0;
	

	public static void init(RobotController controller) {
		InformationBank.controller = controller;
		EnemyHQChecker.init(controller);
	}
	
	public static void broadcastMyHQ(MapLocation location, int elevation) {
		setMyHQLocation(location, elevation);
		int[] message = new int[] {
				MY_HQ_CODE, location.x, location.y, elevation, 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message, MY_HQ_TRANSACTION_COST);
	}
	
	public static void broadcastMyHQState(int state) {
		setMyHQState(state);
		int[] message = new int[] {
				MY_HQ_STATE_CODE, state, 0, 0, 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void broadcastMyHQUnitCount(int designSchoolCount, int fulfillmentCenterCount) {
		setMyHQUnitCount(designSchoolCount, fulfillmentCenterCount);
		int[] message = new int[] {
				MY_HQ_UNIT_COUNT_CODE, designSchoolCount, fulfillmentCenterCount, 0, 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void broadcastEnemyHQ(MapLocation location) {
		setEnemyHQLocation(location);
		int[] message = new int[] {
				ENEMY_HQ_CODE, enemyHQLocation.x, enemyHQLocation.y, 0, 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void broadcastEnemyGuessMode(int mode) {
		setEnemyHQGuesserMode(mode);
		int[] message = new int[] {
				ENEMY_HQ_MODE_CODE, mode, 0, 0, 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void broadcastAttackState(int attackState) {
		setAttackState(attackState);
		int[] message = new int[] {
				ATTACK_STATE_CODE, attackState, 0, 0, 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void broadcastSoupLoacation(MapLocation location) {
		int[] message = new int[] {
				NEW_SOUP_CODE, location.x, location.y, 0, 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void broadcastSoupGone(MapLocation location) {
		int[] message = new int[] {
				SOUP_GONE_CODE, location.x, location.y, 0, 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void broadcastWallState(int state) {
		int[] message = new int[] {
				WALLSTATE_CHANGE_CODE, 0, 0, 0, state, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void broadcastWaterState(boolean flooding, MapLocation location) {
		int[] message = new int[] {
				WATER_CODE, flooding ? 1 : 0, 0, location.x, location.y, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void broadcastBuildLandscaper() {
		int[] message = new int[] {
				NEW_LANDSCAPER_CODE, 0, 0, 0, 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void broadcastBuiltDrone() {
		int[] message = new int[] {
				NEW_DRONE_CODE, 0, 0, 0, 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void broadcastDroneReady() {
		int[] message = new int[] {
				DRONE_READY_CODE, 0, 0, 0, 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void broadcastVaporatorCountIncrement() {
		int[] message = new int[] {
				VAPORATOR_COUNT_INCREMENT_CODE, 0, 0, 0, 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	public static void broadcastSaveForNetgunSignal(boolean saving) {
		int[] message = new int[] {
				TOGGLE_SAVE_FOR_NETGUN_CODE, 0, 0, (saving ? 1 : 0), 0, 0, 0
		};
		Communication.encryptMessage(message);
		CommunicationEngine.addMessageToQueue(message);
	}
	
	public static void processMessage(int[] message) {
		switch(message[0]) {
			case MY_HQ_CODE:
				setMyHQLocation(new MapLocation(message[1], message[2]), message[3]);
				break;
			case MY_HQ_STATE_CODE:
				setMyHQState(message[1]);
				break;
			case MY_HQ_UNIT_COUNT_CODE:
				setMyHQUnitCount(message[1], message[2]);
				break;
			case ENEMY_HQ_CODE:
				setEnemyHQLocation(new MapLocation(message[1], message[2]));
				break;
			case ENEMY_HQ_MODE_CODE:
				setEnemyHQGuesserMode(message[1]);
				break;
			case ATTACK_STATE_CODE:
				setAttackState(message[1]);
				break;
			case NEW_SOUP_CODE:
				MapChecker.sharedSoupLocations.add(new MapLocation(message[1], message[2]));
				break;
			case SOUP_GONE_CODE:
				MapChecker.sharedSoupLocations.remove(new MapLocation(message[1], message[2]));
				break;
			case WALLSTATE_CHANGE_CODE:
				wallState = message[4];
				break;
			case WATER_CODE:
				MapLocation location = new MapLocation(message[3], message[4]);
				// water state
				if (message[1] == 1) {
					// add water state
					if (MapChecker.sharedClosestWaterToMyHQLocation == null) {
						MapChecker.sharedClosestWaterToMyHQLocation = location;
					} else {
						MapLocation hqLocation = InformationBank.getMyHQLocation();
						if (hqLocation != null) {
							if (location.distanceSquaredTo(hqLocation) < MapChecker.sharedClosestWaterToMyHQLocation.distanceSquaredTo(hqLocation)) {
								MapChecker.sharedClosestWaterToMyHQLocation = location;
							}
						}
					}
					if (MapChecker.sharedClosestWaterToEnemyHQLocation == null) {
						MapChecker.sharedClosestWaterToEnemyHQLocation = location;
					} else {
						MapLocation enemyHQLocation = InformationBank.getEnemyHQLocation();
						if (enemyHQLocation != null) {
							if (location.distanceSquaredTo(enemyHQLocation) < MapChecker.sharedClosestWaterToEnemyHQLocation.distanceSquaredTo(enemyHQLocation)) {
								MapChecker.sharedClosestWaterToEnemyHQLocation = location;
							}
						}
					}
				} else {
					// clear water state
					if (location.equals(MapChecker.sharedClosestWaterToMyHQLocation)) {
						MapChecker.sharedClosestWaterToMyHQLocation = null;
					}
					if (location.equals(MapChecker.sharedClosestWaterToEnemyHQLocation)) {
						MapChecker.sharedClosestWaterToEnemyHQLocation = null;
					}
				}
				break;
			case NEW_LANDSCAPER_CODE:
				landscapersBuilt++;
				break;
			case NEW_DRONE_CODE:
				dronesBuilt++;
				dronesBuiltTotal++;
				break;
			case DRONE_READY_CODE:
				dronesReady++;
				break;
			case VAPORATOR_COUNT_INCREMENT_CODE:
				vaporatorCount++;
				break;
			case TOGGLE_SAVE_FOR_NETGUN_CODE:
				isSavingForNetgun = message[3] == 1;
				break;
		}
	}
	private static void setMyHQLocation(MapLocation location, int elevation) {
		EnemyHQChecker.makeGuesses(location.x, location.y);
		myHQLocation = location;
		myHQParityX = location.x % 2;
		myHQParityY = location.y % 2;
		
		myHQElevation = elevation;
		myHQNearCorner = InitEngine.checkIfIsNearCorner(location);
	}
	public static MapLocation getMyHQLocation() {
		return myHQLocation;
	}
	public static int getMyHQPairedByX() {
		return myHQParityX;
	}
	public static int getMyHQPairedByY() {
		return myHQParityY;
	}
	public static int getMyHQElevation() {
		return myHQElevation;
	}
	private static void setEnemyHQLocation(MapLocation location) {
		enemyHQLocation = location;
	}
	public static MapLocation getEnemyHQLocation() {
		return enemyHQLocation;
	}
	private static void setEnemyHQGuesserMode(int mode) {
		enemyHQGuesserMode = mode;
	}
	public static int getEnemyHQGuesserMode() {
		return enemyHQGuesserMode;
	}
	private static void setMyHQState(int state) {
		myHQState = state;
	}
	public static int getMyHQState() {
		return myHQState;
	}
	private static void setMyHQUnitCount(int designSchoolCount, int fulfillmentCenterCount) {
		InformationBank.designSchoolCount = designSchoolCount;
		InformationBank.fulfillmentCenterCount = fulfillmentCenterCount;
	}
	public static int getDesignSchoolCount() {
		return designSchoolCount;
	}
	public static int getFulfillmentCenterCount() {
		return fulfillmentCenterCount;
	}
	public static int getVaporatorCount() {
		return vaporatorCount;
	}
	private static void setAttackState(int attackState) {
		InformationBank.attackState = attackState;
		if (Drone.readyToAttack) {
			// We should only rush if we're there
			PathfindingEngine.ignoreNetGuns = (attackState == ATTACK_STATE_ENEMY_HQ_IGNORE_NETGUNS);
		}
		if (attackState == ATTACK_STATE_ENEMY_HQ_IGNORE_NETGUNS) {
			dronesBuilt -= dronesReady;
			dronesReady = 0;
		}
	}
	public static int getAttackState() {
		return attackState;
	}
}
