package shadysoupdealersmainbot.general.util;

import battlecode.common.GameConstants;

public class AttacksEngine {
	private static final int MAX_QUEUE_SIZE = 20;
	private static int[][] queueMessages;
	private static int[] newestMessage;
	private static int newestCost;
	private static int[] queueCosts;
	private static int sizeOfQueue;
	private static int queueID;
	private static int falseMessageAttackCounter;
	public static void init() {
		queueMessages = new int[MAX_QUEUE_SIZE][];
		queueCosts = new int[MAX_QUEUE_SIZE];
		falseMessageAttackCounter = 0;
		newestMessage = null;
		newestCost = Integer.MAX_VALUE;
		sizeOfQueue = 0;
		queueID = 0;
	}
	
	public static int getAttackCount() {
		return falseMessageAttackCounter;
	}
	public static void sendFalseMessageAttack() {
		// Communication attack in a set order
		switch (falseMessageAttackCounter) {
			case 0:
				CommunicationEngine.addMessageToQueue(getFalseMessageBitFlipAttack(getEnemyMessage(0)), 1);
				CommunicationEngine.addMessageToQueue(getFalseMessageBitFlipAttack(getEnemyMessage(1)), 1);
				CommunicationEngine.addMessageToQueue(getFalseMessageBitFlipAttack(getEnemyMessage(2)), 1);
				CommunicationEngine.addMessageToQueue(getFalseMessageBitFlipAttack(getEnemyMessage(3)), 1);
				CommunicationEngine.addMessageToQueue(getFalseMessageBitFlipAttack(getEnemyMessage(4)), 1);
				sendRandomBitFlipFalseMessageAttack();
				sendRandomBitFlipFalseMessageAttack();
				break;
			case 1:
				sendRecentDOSAttack();
			case 2:
				sendRandomBitFlipFalseMessageAttack();
				sendRandomBitFlipFalseMessageAttack();
			case 3:
				sendFalseMessageBytecodeDOS();
			default:
				switch(InitEngine.getOriginalRandom().nextInt(6)) {
					case 0:
						sendRecentDOSAttack();
						break;
					case 1:
						sendFalseMessageBytecodeDOS();
						break;
					case 2:
					case 3:
					case 4:
						sendRandomBitFlipFalseMessageAttack();
						sendRandomBitFlipFalseMessageAttack();
						sendRandomBitFlipFalseMessageAttack();
						break;
					case 5:
						sendRecentFalseMessageAttack();
						break;
				}
		}
		falseMessageAttackCounter++;
	}
	public static void sendRecentFalseMessageAttack() {
		if (newestCost <= 2) {
			CommunicationEngine.addMessageToQueue(newestMessage, newestCost);
		}
	}
	public static void sendRecentDOSAttack() {
		if (newestCost <= 2) {
			CommunicationEngine.addMessageToQueue(newestMessage, newestCost);
			CommunicationEngine.addMessageToQueue(newestMessage, newestCost);
			CommunicationEngine.addMessageToQueue(newestMessage, newestCost);
			CommunicationEngine.addMessageToQueue(newestMessage, newestCost);
			CommunicationEngine.addMessageToQueue(newestMessage, newestCost);
			CommunicationEngine.addMessageToQueue(newestMessage, newestCost);
			CommunicationEngine.addMessageToQueue(newestMessage, newestCost);
		}
	}
	public static void sendRandomBitFlipFalseMessageAttack() {
		CommunicationEngine.addMessageToQueue(getFalseMessageBitFlipAttack(getRandomEnemyMessage()), 1);
	}
	public static void sendFalseMessageBytecodeDOS() {
		// Sends a bunch of replay attacks - block from random offset
		if (sizeOfQueue >= GameConstants.NUMBER_OF_TRANSACTIONS_PER_BLOCK) {
			int randomOffset = InitEngine.getOriginalRandom().nextInt(sizeOfQueue);
			for (int i = 0; i < GameConstants.NUMBER_OF_TRANSACTIONS_PER_BLOCK; i++) {
				int index = (queueID + (randomOffset + i) % sizeOfQueue) % MAX_QUEUE_SIZE;
				CommunicationEngine.addMessageToQueue(queueMessages[index], Math.min(queueCosts[index], 3));
			}
		}
	}
	public static void addEnemyMessage(int[] message, int cost) {
		if (sizeOfQueue < MAX_QUEUE_SIZE) {
			queueMessages[(queueID + sizeOfQueue) % MAX_QUEUE_SIZE] = message;
			queueCosts[(queueID + sizeOfQueue++) % MAX_QUEUE_SIZE] = cost;
		}
		newestMessage = message;
		newestCost = cost;
	}
	public static int[] getEnemyMessage(int n) {
		return queueMessages[(queueID + n) % MAX_QUEUE_SIZE];
	}
	public static int[] getRandomEnemyMessage() {
		return queueMessages[(queueID + InitEngine.getOriginalRandom().nextInt(sizeOfQueue)) % MAX_QUEUE_SIZE];
	}
	public static int[] getFalseMessageBitFlipAttack(int[] message) {
		if (message == null) {
			return null;
		}
		int i = InitEngine.getOriginalRandom().nextInt(message.length);
		int[] newMessage = copy(message);
		newMessage[i] = newMessage[i] ^ (0b1 << InitEngine.getOriginalRandom().nextInt(32));
		return newMessage;
	}
	public static int[] copy(int[] message) {
		int[] newMessage = new int[message.length];
		for (int i = message.length; --i >= 0;) {
			newMessage[i] = message[i];
		}
		return newMessage;
	}
}
