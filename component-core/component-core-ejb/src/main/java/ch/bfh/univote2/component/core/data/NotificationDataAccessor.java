/*
 * UniVote2
 *
 *  UniVote2(tm): An Internet-based, verifiable e-voting system for student elections in Switzerland
 *  Copyright (c) 2015 Bern University of Applied Sciences (BFH),
 *  Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *  Quellgasse 21, CH-2501 Biel, Switzerland
 *
 *  Licensed under Dual License consisting of:
 *  1. GNU Affero General Public License (AGPL) v3
 *  and
 *  2. Commercial license
 *
 *
 *  1. This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Affero General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Affero General Public License for more details.
 *
 *   You should have received a copy of the GNU Affero General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  2. Licensees holding valid commercial licenses for UniVote2 may use this file in
 *   accordance with the commercial license agreement provided with the
 *   Software or, alternatively, in accordance with the terms contained in
 *   a written agreement between you and Bern University of Applied Sciences (BFH),
 *   Research Institute for Security in the Information Society (RISIS), E-Voting Group (EVG),
 *   Quellgasse 21, CH-2501 Biel, Switzerland.
 *
 *
 *   For further information contact <e-mail: univote@bfh.ch>
 *
 *
 * Redistributions of files must retain the above copyright notice.
 */
package ch.bfh.univote2.component.core.data;

import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
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
	private final Map<ActionContextKey, List<NotificationData>> actionMappings;

	public NotificationDataAccessor() {
		this.notificationMappings = new HashMap<>();
		this.actionMappings = new HashMap<>();
	}

	public NotificationData findByNotificationCode(String notificationCode) {
		return this.notificationMappings.get(notificationCode);
	}

	public List<NotificationData> findByActionContextKey(ActionContextKey actionContext) {
		return this.actionMappings.get(actionContext);

	}

	public void removeByNotificationCode(String notificationCode) {
		NotificationData notificationData = this.notificationMappings.remove(notificationCode);
		this.actionMappings.remove(notificationData.getActionContextKey());
	}

	public void removeByActionContextKey(ActionContextKey actionContext) {
		List<NotificationData> notificationData = this.actionMappings.remove(actionContext);
		for (NotificationData nd : notificationData) {
			this.notificationMappings.remove(nd.getNotifictionCode());
		}
	}

	public void addNotificationData(NotificationData notificationData) {
		this.notificationMappings.put(notificationData.getNotifictionCode(), notificationData);
		if (this.actionMappings.containsKey(notificationData.getActionContextKey())) {
			this.actionMappings.get(notificationData.getActionContextKey()).add(notificationData);
		} else {
			List<NotificationData> newNotificaitonData = new ArrayList<>();
			newNotificaitonData.add(notificationData);
			this.actionMappings.put(notificationData.getActionContextKey(), newNotificaitonData);
		}
	}

	public boolean containsNotificationCode(String notificationCode) {
		return this.notificationMappings.containsKey(notificationCode);
	}

	public boolean containsActionContextKey(ActionContextKey actionContext) {
		return this.actionMappings.containsKey(actionContext);
	}

	public Set<String> getAllNotificationCodes() {
		return this.notificationMappings.keySet();
	}

	public void purge() {
		this.actionMappings.clear();
		this.notificationMappings.clear();
	}
}
