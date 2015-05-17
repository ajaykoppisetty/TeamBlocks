package org.faudroids.doublestacks.core;

import org.faudroids.doublestacks.google.MessageSender;

import javax.inject.Inject;

public class MessageManager {

	private final MessageSender messageSender;

	private int senderEpoch = 0;
	private int senderSeqNum = 0;

	private int receiverEpoch = 0;
	private int receiverSeqNum = 0;

	@Inject
	MessageManager(MessageSender messageSender) {
		this.messageSender = messageSender;
	}


	public void sendMessage(FieldUpdate update, boolean isReliable) {
		if (isReliable) {
			++senderEpoch;
			senderSeqNum = 0;
		} else {
			++senderSeqNum;
		}

		update.setEpoch(senderEpoch);
		update.setSeqNum(senderSeqNum);
		messageSender.sendMessage(update, isReliable);
	}


	/**
	 * @return whether the passed in update should be processed or not.
	 */
	public boolean receiveMessage(FieldUpdate update, boolean isReliable) {
		if (isReliable) {
			receiverEpoch = update.getEpoch();
			receiverSeqNum = 0;
			return true;
		} else {
			if (update.getEpoch() < receiverEpoch) return false;
			if (update.getSeqNum() < receiverSeqNum) return false;
			receiverSeqNum = update.getSeqNum();
			return true;
		}
	}

}
