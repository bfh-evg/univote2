/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniVote2.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote2.component.core.data;

import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class BoardNotificationData extends NotificationData {

	private final String board;

	public BoardNotificationData(String board, String notifictionCode, ActionContextKey actionContextKey) {
		super(notifictionCode, actionContextKey);
		this.board = board;
	}

	public String getBoard() {
		return board;
	}

}
