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
public class NotificationDataAccessor {

	private final Map<String, NotificationData> notificationMappings;
	private final Map<String, List<NotificationData>> actionMappings;

	public NotificationDataAccessor() {
		this.notificationMappings = new HashMap<>();
		this.actionMappings = new HashMap<>();
	}

	public NotificationData findByNotificationCode(String notificationCode) {
		return this.notificationMappings.get(notificationCode);
	}

	public List<NotificationData> findByActionName(String actionName) {
		return this.actionMappings.get(actionName);

	}

	public void removeByNotificationCode(String notificationCode) {
		NotificationData notificationData = this.notificationMappings.remove(notificationCode);
		this.actionMappings.remove(notificationData.getAction());
	}

	public void removeByActionName(String actionName) {
		List<NotificationData> notificationData = this.actionMappings.remove(actionName);
		for (NotificationData nd : notificationData) {
			this.notificationMappings.remove(nd.getNotifictionCode());
		}
	}

	public void add(NotificationData notificationData) {
		this.notificationMappings.put(notificationData.getNotifictionCode(), notificationData);
		if (this.actionMappings.containsKey(notificationData.getAction())) {
			this.actionMappings.get(notificationData.getAction()).add(notificationData);
		} else {
			List<NotificationData> newNotificaitonData = new ArrayList<>();
			newNotificaitonData.add(notificationData);
			this.actionMappings.put(notificationData.getAction(), newNotificaitonData);
		}
	}

	public boolean containsNotificationCode(String notificationCode) {
		return this.notificationMappings.containsKey(notificationCode);
	}

	public Set<String> getAllNotificationCodes() {
		return this.notificationMappings.keySet();
	}
}
