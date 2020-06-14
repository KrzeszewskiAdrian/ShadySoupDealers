package shadysoupdealersmainbot.general.util;


public class InformationMap {

	private long[][] mapInfo;
	public InformationMap(int width, int height) {
		this.mapInfo = new long[width][height];
	}
	public void set(int x, int y, long value) {
		mapInfo[x][y] = value;
	}
	public long get(int x, int y) {
		return mapInfo[x][y];
	}
}
