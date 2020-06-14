package shadysoupdealersmainbot.general.util;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

public class GridChecker {
	
	private static final Direction[][] HOLE_DIRECTION = {
			{Direction.CENTER},
			{Direction.NORTH, Direction.SOUTH},
			{Direction.WEST, Direction.EAST},
			{Direction.NORTHWEST, Direction.NORTHEAST, Direction.SOUTHWEST, Direction.SOUTHEAST}
	};
	
	public static boolean checkIfIsHole(MapLocation location) {
		return (location.x % 2 == InformationBank.getMyHQPairedByX() &&
				location.y % 2 == InformationBank.getMyHQPairedByY()) ||
				(InformationBank.myHQNearCorner && InitEngine.checkIfIsInCorner(location));
	}
	
	public static boolean isBuildLocation(MapLocation location) {
		return (location.x % 2 != InformationBank.getMyHQPairedByX()) &&
				(location.y % 2 != InformationBank.getMyHQPairedByY());
	}
	
	public static Direction[] getHoleDirections(MapLocation location) {
		int pairedByX = (location.x - InformationBank.getMyHQPairedByX() + 2) % 2;
		int pairedByY = (location.y - InformationBank.getMyHQPairedByY() + 2) % 2;
		switch (pairedByX) {
			case 0:
				switch(pairedByY) {
					case 0:
						return HOLE_DIRECTION[0];
					case 1:
						return HOLE_DIRECTION[1];
				}
			case 1:
				switch(pairedByY) {
					case 0:
						return HOLE_DIRECTION[2];
					case 1:
						return HOLE_DIRECTION[3];
				}
		}
		throw new IllegalStateException("not possible");
	}
}
