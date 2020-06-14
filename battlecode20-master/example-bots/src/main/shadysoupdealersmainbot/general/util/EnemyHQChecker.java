package shadysoupdealersmainbot.general.util;

import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class EnemyHQChecker {
	private static RobotController controller;
	private static MapLocation[] guesses;

	// how many scenarios?
	// mode = 0: know #1 is not hq
	// mode = 1: know #2 is not hq
	// mode = 2: know #3 is not hq
	// mode = 3: don't know anything

	public static final int UNKNOWN_MODE = 3;

	public static void init(RobotController controller) {
		EnemyHQChecker.controller = controller;
	}

	/**
	 * Input: Our HQ Location. Creates guesses based on our hq location and symmetry
	 */
	public static void makeGuesses(int x, int y) {
		//3, cuz any of symmetry options can be used
		guesses = new MapLocation[3];
		int a = Cache.MAP_WIDTH - x - 1;
		int b = Cache.MAP_HEIGHT - y - 1;
		// Symmetry around x axis
		guesses[0] = new MapLocation(x, b);
		// Symmetry around y axis
		guesses[1] = new MapLocation(a, y);
		// Symmetry 180 degrees
		guesses[2] = new MapLocation(a, b);
	}
	
	public static void findEnemyHQLoop() {
		if (guesses == null) {
			return;
		}
		int mode = InformationBank.getEnemyHQGuesserMode();
		// Let's check if any guesses can be sensed
		for (int i = guesses.length; --i >= 0;) {
			if (mode == i) {
				continue;
			}

			if (controller.canSenseLocation(guesses[i])) {
				// HQ is not at this location (not in SharedInfo)
				if (markUnseen(i)) {
					return;
				}
			}
		}
	}

	public static MapLocation getEnemyHQGuessLocation(int index) {
		if (index < 0) {
			return null;
		}
		return guesses[index];
	}
	
	public static boolean markUnseen(int index) {
		if (InformationBank.getEnemyHQGuesserMode() == UNKNOWN_MODE) {
			InformationBank.broadcastEnemyGuessMode(index);
			return false;
		} else {
			int other = getOtherGuesses(InformationBank.getEnemyHQGuesserMode(), index);
			InformationBank.broadcastEnemyHQ(guesses[other]);
			return true;
		}
	}
	
	private static int getOtherGuesses(int a, int b) {

		switch(a) {
			case 0:
				switch(b) {
					case 1:
						return 2;
					case 2:
						return 1;
					default:
						throw new IllegalArgumentException("Unknown");
				}
			case 1:
				switch(b) {
					case 0:
						return 2;
					case 2:
						return 0;
					default:
						throw new IllegalArgumentException("Unknown");
				}
			case 2:
				switch(b) {
					case 0:
						return 1;
					case 1:
						return 0;
					default:
						throw new IllegalArgumentException("Unknown");
				}
			default:
				throw new IllegalArgumentException("Unknown");
		}
	}
	public static int getRandomGuessIndex() {
		// Questionable tactics :>
		double random = InitEngine.getOriginalRandom().nextDouble();
		switch(InformationBank.getEnemyHQGuesserMode()) {
			case 0:
				if (random < 0.5) {
					return 1;
				} else {
					return 2;
				}
			case 1:
				if (random < 0.5) {
					return 0;
				} else {
					return 2;
				}
			case 2:
				if (random < 0.5) {
					return 0;
				} else {
					return 1;
				}
			case UNKNOWN_MODE:
				return (int) (random * 3);
			default:
				return -1;
		}
	}
}
