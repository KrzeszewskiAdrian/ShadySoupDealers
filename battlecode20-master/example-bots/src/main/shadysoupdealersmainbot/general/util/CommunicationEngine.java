package shadysoupdealersmainbot.general.util;

import battlecode.common.*;

import java.util.Arrays;

public class CommunicationEngine {
	private static RobotController controller;
	
	private static int currentTurn;
	
	private static final int QUEUE_LENGTH = 100;
	
	private static int[][] messageQueue;
	private static int[] costQueue;
	
	private static int queueIndex;
	private static int queueSize;
	
	private static int minTransactionCost;
	private static int immediateRedoAttackCount = 0;

	public static void init(RobotController controller) {
		CommunicationEngine.controller = controller;
		currentTurn = 1;
		
		messageQueue = new int[QUEUE_LENGTH][];
		costQueue = new int[QUEUE_LENGTH];
		
		queueIndex = 0;
		queueSize = 0;
		
		minTransactionCost = 1;
	}
	
	public static void addMessageToQueue(int[] message, int cost) {
		if (message == null) {
			return;
		}
		int index = (queueIndex + queueSize) % QUEUE_LENGTH;
		
		messageQueue[index] = message;
		costQueue[index] = cost;
		
		queueSize++;
	}
	
	public static void addMessageToQueue(int[] message) {
		addMessageToQueue(message, minTransactionCost);
	}
	
	public static void sendAllMessages() throws GameActionException {
		while (queueSize > 0) {
			int index = queueIndex % QUEUE_LENGTH;
			int cost = costQueue[index];
			if (controller.getTeamSoup() >= costQueue[index]) {
				int[] message = messageQueue[index];
				if (controller.canSubmitTransaction(message, cost)) {
					controller.submitTransaction(message, cost);
				} else {
					Cache.controller.setIndicatorDot(Cache.CURRENT_LOCATION, 255, 0, 0);
					System.out.println("Cannot send a message: " + Arrays.toString(message) + " (cost=" + cost + ")");
				}
				
				queueIndex++;
				queueSize--;
			} else {
				break;
			}
		}
	}
	
	public static void processMainLoop() throws GameActionException {
		while (currentTurn < controller.getRoundNum()) {
			Transaction[] transactions = controller.getBlock(currentTurn);
			minTransactionCost = transactions.length == GameConstants.NUMBER_OF_TRANSACTIONS_PER_BLOCK ? Integer.MAX_VALUE : 1;
			for (int i = transactions.length; --i >= 0;) {
				processTransaction(transactions[i]);
				minTransactionCost = Math.min(minTransactionCost, transactions[i].getCost());
			}
			currentTurn++;
		}
	}
	
	public static void processTransaction(Transaction transaction) {
		
		int[] message = transaction.getMessage();
		int state = Communication.verifyMessage(message);
		
		switch (state) {
			case Communication.COM_STATE_UNKNOWN_HASH:
				if (Cache.ROBOT_TYPE == RobotType.HQ) {
					AttacksEngine.addEnemyMessage(message, transaction.getCost());
					if (InformationBank.getVaporatorCount() >= 5) {
						if (AttacksEngine.getAttackCount() < 10) {
							controller.setIndicatorDot(Cache.CURRENT_LOCATION, 128, 128, 128);
							if (immediateRedoAttackCount < 5) {
								AttacksEngine.sendRecentFalseMessageAttack();
								immediateRedoAttackCount++;
							} else {
								AttacksEngine.sendFalseMessageAttack();
							}
						}
					}
				}
				break;
			case Communication.COM_STATE_SUCCESS:
				InformationBank.processMessage(message);
				break;
		}
	}
	
	public static int getMinTransactionsCost() {
		return minTransactionCost;
	}
}
