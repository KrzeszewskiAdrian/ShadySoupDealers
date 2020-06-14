package shadysoupdealersmainbot.general;

import battlecode.common.GameActionException;

@FunctionalInterface
public interface RobotBehavior {
	boolean execute() throws GameActionException;
}
