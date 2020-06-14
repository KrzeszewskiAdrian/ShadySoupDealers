package shadysoupdealersmainbot.general;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import shadysoupdealersmainbot.RunnableBot;
import shadysoupdealersmainbot.general.util.InformationBank;

public class Vaporator implements RunnableBot {
	private RobotController robotController;
	public Vaporator(RobotController robotController) {
		this.robotController = robotController;
	}
	@Override
	public void init() throws GameActionException {

	}
	boolean updatedCount = false;
	@Override
	public void turn() throws GameActionException {
		if (!updatedCount) {
			InformationBank.broadcastVaporatorCountIncrement();
			updatedCount = true;
		}
	}
}
