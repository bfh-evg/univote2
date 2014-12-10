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
package ch.bfh.univote2.component.core.manager;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class NotificationData {

	private String action;
	private String tenant;
	private String section;

	public NotificationData(String action, String tenant, String section) {
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

}
