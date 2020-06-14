package shadysoupdealersmainbot.general.util;

import battlecode.common.RobotController;
import battlecode.common.Team;

public class Communication {
	public static final int COM_STATE_SUCCESS = 0;
	public static final int COM_STATE_UNKNOWN_HASH = 1;
	public static final int COM_STATE_REDO_ATTACK = 2;
	
	private static int COM_SEED = 20851893;

	// Bloom filter
	private static final int BLOOM_FILTER_SIZE = 64 * 4096;
	private static final int BITS_PER_LONG = 64;
	private static final BooleanArray BLOOM_FILTER = new BooleanArray(BLOOM_FILTER_SIZE / BITS_PER_LONG);
	private static final int[] BLOOM_FILTER_SEEDS = new int[] {707946, 784205, 847237}; // 3 hash functions
	
	public static void init(RobotController controller) {
		// XOR seed by size of the map
		int gameAdjust = (Cache.MAP_WIDTH << 6) | Cache.MAP_HEIGHT;
		gameAdjust = (gameAdjust << 1) | ((controller.getTeam() == Team.A) ? 0 : 1);
		COM_SEED ^= gameAdjust;
		// preloads static variables
	}
	
	public static int verifyMessage(int[] message) {
		int signature = message[6];
		if (Hash.hash(COM_SEED, message[5]) != signature) {
			return COM_STATE_UNKNOWN_HASH;
		}
		boolean isVerified = false;
		for (int i = BLOOM_FILTER_SEEDS.length; --i >= 0;) {
			int id = Math.abs(Hash.hash(BLOOM_FILTER_SEEDS[i], signature) % BLOOM_FILTER_SIZE);
			if (!BLOOM_FILTER.get(id)) {
				BLOOM_FILTER.set(id);
				isVerified = true;
			}
		}
		return isVerified ? COM_STATE_SUCCESS : COM_STATE_REDO_ATTACK;
	}
	
	private static final int RANDOM_MASK = 0b11111111111111111111000000000000;
	
	public static void encryptMessage(int[] message) {
		int footer = (InitEngine.getOriginalRandom().nextInt() & RANDOM_MASK) ^ Cache.controller.getRoundNum();
		message[5] = footer; // footer
		message[6] = Hash.hash(COM_SEED, footer); // signature
	}
	
}
