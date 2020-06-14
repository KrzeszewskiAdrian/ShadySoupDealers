package shadysoupdealersmainbot.general.util;

public class Hash {
	// Murmur hash constants
	private static final int c1 = 0xcc9e2d51;
	private static final int c2 = 0x1b873593;

	public static int hash(int seed, int operand) {
		int h1 = Integer.rotateLeft(seed ^ (Integer.rotateLeft(operand * c1, 15) * c2), 13) * 5 + 0xe6546b64;
		return (((((h1 ^ 32) ^ (h1 >>> 16)) * 0x85ebca6b) ^ (h1 >>> 13)) * 0xc2b2ae35) ^ (h1 >> 16);
	}
}
