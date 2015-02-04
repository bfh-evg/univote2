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

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public abstract class Task {

	private final String tenant;
	private final String section;

	public Task(String tenant, String section) {
		this.tenant = tenant;
		this.section = section;
	}

	public String getTenant() {
		return tenant;
	}

	public String getSection() {
		return section;
	}

}
