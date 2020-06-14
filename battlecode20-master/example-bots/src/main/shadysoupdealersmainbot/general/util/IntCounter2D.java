package shadysoupdealersmainbot.general.util;

public class IntCounter2D {
	private int[][] array;
	private int counter;
	private int baseValue;
	
	public IntCounter2D(int width, int height) {
		this.array = new int[width][height];
		this.counter = 0;
		this.baseValue = 0;
	}
	
	public void add(int x, int y) {
		this.array[x][y] = counter++;
	}
	
	public boolean contains(int x, int y) {
		return this.array[x][y] >= baseValue;
	}
	
	public int get(int x, int y) {
		int num = this.array[x][y] - baseValue;
		return num < 0 ? -1 : num;
	}
	
	public void reset() {
		this.baseValue = counter;
	}
	
	public int getCounter() {
		return counter - baseValue;
	}
	
	public void updateBaseTrail(int n) {
		this.baseValue = counter - n;
	}
}