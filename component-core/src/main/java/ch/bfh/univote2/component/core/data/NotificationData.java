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
import java.util.Objects;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class NotificationData {

	private final String notifictionCode;
	private final ActionContextKey actionContextKey;

	/**
	 *
	 * @param notifictionCode
	 * @param action
	 */
	public NotificationData(String notifictionCode, ActionContextKey actionContextKey) {
		this.notifictionCode = notifictionCode;
		this.actionContextKey = actionContextKey;
	}

	public ActionContextKey getActionContextKey() {
		return actionContextKey;
	}

	public String getNotifictionCode() {
		return notifictionCode;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 71 * hash + Objects.hashCode(this.notifictionCode);
		hash = 71 * hash + Objects.hashCode(this.actionContextKey);
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final NotificationData other = (NotificationData) obj;
		if (!Objects.equals(this.notifictionCode, other.notifictionCode)) {
			return false;
		}
		return Objects.equals(this.actionContextKey, other.actionContextKey);
	}

}
