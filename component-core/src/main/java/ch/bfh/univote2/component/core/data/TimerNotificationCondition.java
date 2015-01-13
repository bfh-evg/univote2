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

import java.util.Date;

public class TimerNotificationCondition implements NotificationCondition {

	private final Date date;

	public TimerNotificationCondition(Date date) {
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

}
