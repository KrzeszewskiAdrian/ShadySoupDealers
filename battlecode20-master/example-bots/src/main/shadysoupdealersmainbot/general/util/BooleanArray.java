package shadysoupdealersmainbot.general.util;

public class BooleanArray {
	private final long[] booleans;
	/**
	 *
	 * @param length ensure you divide by 64
	 */
	public BooleanArray(int length) {
		this.booleans = new long[length];
	}
	public void set(int index) {
		booleans[index / 64] |= (1L << (index % 64));
	}
	public boolean get(int index) {
		return ((booleans[index / 64] >>> (index % 64)) & 1) == 1;
	}
}
