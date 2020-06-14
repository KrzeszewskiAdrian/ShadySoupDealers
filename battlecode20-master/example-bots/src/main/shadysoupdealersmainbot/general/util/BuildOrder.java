package shadysoupdealersmainbot.general.util;

import battlecode.common.RobotType;
import shadysoupdealersmainbot.general.HQ;

public class BuildOrder {
	public static final int MASS_SPAWN_VAPORATOR_THRESHOLD = 15;
	public static RobotType getNextRobotToBuild() {
		if (InformationBank.isSavingForNetgun) {
			return RobotType.NET_GUN;
		}
		switch (InformationBank.getMyHQState()) {
			case HQ.NO_ADDITIONAL_HELP_NEEDED:
				return RobotType.DELIVERY_DRONE;
			case HQ.NEEDS_HELP:
				if (InformationBank.landscapersBuilt >= 10 && InformationBank.dronesBuiltTotal == 0) {
					return RobotType.DELIVERY_DRONE;
				} else {
					return RobotType.LANDSCAPER;
				}
		}
		if (InformationBank.wallState == InformationBank.WALL_STATE_NEEDS) {
			return RobotType.LANDSCAPER;
		}
		else if (InformationBank.landscapersBuilt < 2) {
			// Includes Design School
			return RobotType.LANDSCAPER;
		}
		else if (InformationBank.getVaporatorCount() < 3) {
			return RobotType.VAPORATOR;
		}
		else if (InformationBank.landscapersBuilt < 4) {
			return RobotType.LANDSCAPER;
		}
		else if (InformationBank.dronesBuiltTotal < 3) {
			return RobotType.DELIVERY_DRONE;
		}
		else if (InformationBank.getVaporatorCount() < MASS_SPAWN_VAPORATOR_THRESHOLD) {
			return RobotType.VAPORATOR;
		}
		// Nothing special needed
		else if (InformationBank.landscapersBuilt <= InformationBank.dronesBuiltTotal) {
			return RobotType.LANDSCAPER;
		} else {
			return RobotType.DELIVERY_DRONE;
		}
	}
	
	public static int getSoupThreshold(RobotType type) {
		int threshold = 0;
		if (InformationBank.isSavingForNetgun) {
			threshold = threshold + RobotType.NET_GUN.cost;
			if (type == RobotType.NET_GUN) {
				return threshold;
			}
		}
		boolean droneCostDone = false;
		boolean landscaperCostDone = false;
		boolean vaporatorCostDone = false;
		switch (InformationBank.getMyHQState()) {
			case HQ.NO_ADDITIONAL_HELP_NEEDED:
				if (!droneCostDone) {
					threshold += RobotType.DELIVERY_DRONE.cost;
					droneCostDone = true;
				}
				if (type == RobotType.DELIVERY_DRONE) {
					return threshold;
				}
				break;
			case HQ.NEEDS_HELP:
				if (InformationBank.landscapersBuilt >= 10 && InformationBank.dronesBuiltTotal == 0) {
					if (!droneCostDone) {
						threshold += RobotType.DELIVERY_DRONE.cost;
						droneCostDone = true;
					}
					if (type == RobotType.DELIVERY_DRONE) {
						return threshold;
					}
				} else {
					if (!landscaperCostDone) {
						threshold += RobotType.LANDSCAPER.cost;
						landscaperCostDone = true;
					}
					if (type == RobotType.LANDSCAPER) {
						return threshold;
					}
				}
				break;
		}
		if (InformationBank.wallState == InformationBank.WALL_STATE_NEEDS) {
			if (!landscaperCostDone) {
				threshold += RobotType.LANDSCAPER.cost;
				landscaperCostDone = true;
			}
			if (type == RobotType.LANDSCAPER) {
				return threshold;
			}
		}
		if (InformationBank.landscapersBuilt < 2) {
			// Includes Design School
			if (!landscaperCostDone) {
				threshold += RobotType.LANDSCAPER.cost;
				landscaperCostDone = true;
			}
			if (type == RobotType.LANDSCAPER) {
				return threshold;
			}
		}
		if (InformationBank.getVaporatorCount() < 3) {
			if (!vaporatorCostDone) {
				threshold += RobotType.VAPORATOR.cost;
				vaporatorCostDone = true;
			}
			if (type == RobotType.VAPORATOR) {
				return threshold;
			}
		}
		if (InformationBank.landscapersBuilt < 4) {
			if (!landscaperCostDone) {
				threshold += RobotType.LANDSCAPER.cost;
				landscaperCostDone = true;
			}
			if (type == RobotType.LANDSCAPER) {
				return threshold;
			}
		}
		if (InformationBank.dronesBuiltTotal < 3) {
			if (!droneCostDone) {
				threshold += RobotType.DELIVERY_DRONE.cost;
				droneCostDone = true;
			}
			if (type == RobotType.DELIVERY_DRONE) {
				return threshold;
			}
		}
		if (InformationBank.getVaporatorCount() < MASS_SPAWN_VAPORATOR_THRESHOLD) {
			if (!vaporatorCostDone) {
				threshold += RobotType.VAPORATOR.cost;
				vaporatorCostDone = true;
			}
			if (type == RobotType.VAPORATOR) {
				return threshold;
			}
		}
		// Nothing special needed
		if (InformationBank.landscapersBuilt <= InformationBank.dronesBuiltTotal) {
			if (!landscaperCostDone) {
				threshold += RobotType.LANDSCAPER.cost;
				landscaperCostDone = true;
			}
			if (type == RobotType.LANDSCAPER) {
				return threshold;
			}
		} else {
			if (!droneCostDone) {
				threshold += RobotType.DELIVERY_DRONE.cost;
				droneCostDone = true;
			}
			if (type == RobotType.DELIVERY_DRONE) {
				return threshold;
			}
		}
		// Make a lot of it
		if (InformationBank.getVaporatorCount() < 120) {
			// But don't overdo it...
			if (Cache.controller.getTeamSoup() < 800) {
				if (!vaporatorCostDone) {
					threshold += RobotType.VAPORATOR.cost;
					vaporatorCostDone = true;
				}
				if (type == RobotType.VAPORATOR) {
					return threshold;
				}
			}
		}
		// make Landscapers
		if (!landscaperCostDone) {
			threshold += RobotType.LANDSCAPER.cost;
			landscaperCostDone = true;
		}
		if (type == RobotType.LANDSCAPER) {
			return threshold;
		}
		// make Drones
		if (!droneCostDone) {
			threshold += RobotType.DELIVERY_DRONE.cost;
			droneCostDone = true;
		}
		if (type == RobotType.DELIVERY_DRONE) {
			return threshold;
		}
		// ?
		//System.out.println("Why are you trying to build " + type + "?");
		return threshold;
	}
}
