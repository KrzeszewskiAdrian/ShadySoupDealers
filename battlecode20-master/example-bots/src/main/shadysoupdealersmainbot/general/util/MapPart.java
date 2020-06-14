package shadysoupdealersmainbot.general.util;
import battlecode.common.MapLocation;

import java.util.ArrayList;

public class MapPart {
	private ArrayList<MapLocation> locations = new ArrayList<>();

	public boolean isEmpty() {
		return locations.isEmpty();
	}
	public void add(MapLocation location) {
		locations.add(location);
		}

	public void remove(MapLocation location) {
		for (int i = locations.size(); --i >= 0;) {
			if (location.equals(locations.get(i))) {
				locations.remove(i);
				break;
			}
		}
	}
	public boolean contains(MapLocation location) {
		return locations.contains(location);
	}
	public MapLocation closestSoup(MapLocation currentLocation) {
		MapLocation closestLocation = null;
		int smallestDistance = Integer.MAX_VALUE;

		for (int i = locations.size(); --i >= 0;) {
			if (locations.get(i) != null) {
				int distance = currentLocation.distanceSquaredTo(locations.get(i));
				if (distance < smallestDistance) {
					closestLocation = locations.get(i);
					smallestDistance = distance;
				}
			}
		}
		return closestLocation;
	}
}
