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

public class RunActionTask extends Task {

	private final String actionName;

	public RunActionTask(String actionName, String tenant, String section) {
		super(tenant, section);
		this.actionName = actionName;
	}

	public String getActionName() {
		return actionName;
	}

}
