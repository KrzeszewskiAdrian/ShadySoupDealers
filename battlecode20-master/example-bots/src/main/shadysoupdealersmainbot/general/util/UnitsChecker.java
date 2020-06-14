package shadysoupdealersmainbot.general.util;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class UnitsChecker {
	private static final long BITMASK_32 = 0b11111111111111111111111111111111L;
	private static final long BITMASK_16 = 0b1111111111111111L;
	private static RobotController robotController;
	private static InformationMap informationMap;
	public static void init(RobotController robotController) {
		UnitsChecker.robotController = robotController;
		informationMap = new InformationMap(Cache.MAP_WIDTH, Cache.MAP_HEIGHT);
	}
	public static boolean hasBlockingUnit(MapLocation location) {
		long round = robotController.getRoundNum();
		long locationInfo = informationMap.get(location.x, location.y);
		long a = locationInfo >>> 48;
		long b = (locationInfo >>> 32) & BITMASK_16;
		boolean building = (locationInfo & BITMASK_32) == 1;
		return ((round - a) < 2 && (round - b) < 2) || building;
	}
	public static void loop() {
		long round = robotController.getRoundNum();
		boolean isRoundNumEven = (round % 2) == 0;
		long and = isRoundNumEven ? (~(BITMASK_16 << 48)) : (~(BITMASK_16 << 32));
		long or = isRoundNumEven ? (round << 48) : (round << 32);
		RobotInfo[] nearbyRobots = Cache.NEARBY_ROBOTS;
		for (RobotInfo robot : nearbyRobots) {
			MapLocation location = robot.getLocation();
			int isBuilding = robot.getType().isBuilding() ? 1 : 0;
			informationMap.set(location.x, location.y, ((informationMap.get(location.x, location.y) & and | or) & ~0b1) | isBuilding);
		}
	}
}
