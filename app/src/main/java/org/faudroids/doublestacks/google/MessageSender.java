package org.faudroids.doublestacks.google;

import java.io.Serializable;

/**
 * Something which can send messages!
 */
public interface MessageSender {

	void sendMessage(Serializable data, boolean reliable);

}
