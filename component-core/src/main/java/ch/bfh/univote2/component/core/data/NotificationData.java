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

import java.util.Objects;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class NotificationData {

	private final String notifictionCode;
	private final String action;
	private final String tenant;
	private final String section;

	public NotificationData(String notifictionCode, String action, String tenant, String section) {
		this.notifictionCode = notifictionCode;
		this.action = action;
		this.tenant = tenant;
		this.section = section;
	}

	public String getAction() {
		return action;
	}

	public String getTenant() {
		return tenant;
	}

	public String getSection() {
		return section;
	}

	public String getNotifictionCode() {
		return notifictionCode;
	}

	@Override
	public int hashCode() {
		int hash = 5;
		hash = 71 * hash + Objects.hashCode(this.notifictionCode);
		hash = 71 * hash + Objects.hashCode(this.action);
		hash = 71 * hash + Objects.hashCode(this.tenant);
		hash = 71 * hash + Objects.hashCode(this.section);
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
		if (!Objects.equals(this.action, other.action)) {
			return false;
		}
		if (!Objects.equals(this.tenant, other.tenant)) {
			return false;
		}
		if (!Objects.equals(this.section, other.section)) {
			return false;
		}
		return true;
	}

}
