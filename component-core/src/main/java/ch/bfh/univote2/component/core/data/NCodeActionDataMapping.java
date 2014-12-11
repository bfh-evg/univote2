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

import java.util.Map;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class NCodeActionDataMapping {

	private Map<String, ActionData> notificationMappings;
	private Map<ActionData, String> actionMappings;

	public ActionData findByNotificationCode(String notificationCode) {
		return null;
	}

	public String findByActionData(ActionData actionData) {
		return "";

	}
}
