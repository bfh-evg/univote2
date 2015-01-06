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

import java.io.Serializable;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class TimerInfo implements Serializable {

	private final String notificationCode;
	private final String tenant;
	private final String section;
	private final String action;

	public TimerInfo(String notificationCode, String tenant, String section, String action) {
		this.notificationCode = notificationCode;
		this.tenant = tenant;
		this.section = section;
		this.action = action;
	}

	public String getNotificationCode() {
		return notificationCode;
	}

	public String getTenant() {
		return tenant;
	}

	public String getSection() {
		return section;
	}

	public String getAction() {
		return action;
	}

}
