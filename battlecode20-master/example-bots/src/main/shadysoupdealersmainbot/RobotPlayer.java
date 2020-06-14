package shadysoupdealersmainbot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import shadysoupdealersmainbot.general.*;
import shadysoupdealersmainbot.general.util.Cache;
import shadysoupdealersmainbot.general.util.InitEngine;

public class RobotPlayer {
	public static RobotController robotController;

	public static void run(RobotController robotController) throws GameActionException {
		RobotPlayer.robotController = robotController;
		RunnableBot runnableBot;
		switch (robotController.getType()) {
			case HQ:
				runnableBot = new HQ(robotController);
				break;
			case MINER:
				runnableBot = new Miner(robotController);
				break;
			case LANDSCAPER:
				runnableBot = new Landscaper(robotController);
				break;
			case DELIVERY_DRONE:
				runnableBot = new Drone(robotController);
				break;
			case DESIGN_SCHOOL:
				runnableBot = new DesignSchool(robotController);
				break;
			case REFINERY:
				runnableBot = new Refinery(robotController);
				break;
			case NET_GUN:
				runnableBot = new NetGun(robotController);
				break;
			case FULFILLMENT_CENTER:
				runnableBot = new FulfillmentCenter(robotController);
				break;
			case VAPORATOR:
				runnableBot = new Vaporator(robotController);
				break;
			default:
				throw new IllegalStateException("Unimplemented!");
		}
		InitEngine.init(robotController);
		runnableBot.init();
		boolean encounteredTroubles = false;
		while (true) {
			try {
				while (true) {
					if (encounteredTroubles) {
						robotController.setIndicatorDot(Cache.CURRENT_LOCATION, 255, 0, 0);
					}
					int currentTurn = robotController.getRoundNum();
					InitEngine.mainLoop();
					runnableBot.turn();
					InitEngine.MessagesLoop();
					if (robotController.getRoundNum() != currentTurn) {
						// We ran out of bytecodes! - MAGENTA
						robotController.setIndicatorDot(Cache.CURRENT_LOCATION, 255, 0, 255);
					}
					Clock.yield();
				}
			} catch (Exception e) {
				e.printStackTrace();
				encounteredTroubles = true;
				Clock.yield();
			}
		}
	}
}
