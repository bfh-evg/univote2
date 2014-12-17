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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class NotificationCodeActionDataMapping {

	private final Map<String, ActionData> notificationMappings;
	private final Map<ActionData, List<String>> actionMappings;

	public NotificationCodeActionDataMapping() {
		this.notificationMappings = new HashMap<>();
		this.actionMappings = new HashMap<>();
	}

	public ActionData findByNotificationCode(String notificationCode) {
		return this.notificationMappings.get(notificationCode);
	}

	public List<String> findByActionData(ActionData actionData) {
		return this.actionMappings.get(actionData);

	}

	public void removeByNotificationCode(String notificationCode) {
		ActionData actionData = this.notificationMappings.remove(notificationCode);
		this.actionMappings.remove(actionData);
	}

	public void removeByActionData(ActionData actionData) {
		List<String> notificationCodes = this.actionMappings.remove(actionData);
		for (String nc : notificationCodes) {
			this.notificationMappings.remove(nc);
		}
	}

	public void add(String notificationCode, ActionData actionData) {
		this.notificationMappings.put(notificationCode, actionData);
		if (this.actionMappings.containsKey(actionData)) {
			this.actionMappings.get(actionData).add(notificationCode);
		} else {
			List<String> notificationCodes = new ArrayList<>();
			notificationCodes.add(notificationCode);
			this.actionMappings.put(actionData, notificationCodes);
		}
	}

	public boolean containsNotificationCode(String notificationCode) {
		return this.notificationMappings.containsKey(notificationCode);
	}

	public Set<String> getAllNotificationCodes() {
		return this.notificationMappings.keySet();
	}
}
