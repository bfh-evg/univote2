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
public class ActionContext {

	private final String action;
	private final String tenant;
	private final String section;

	public ActionContext(String action, String tenant, String section) {
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

	@Override
	public int hashCode() {
		int hash = 7;
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
		final ActionContext other = (ActionContext) obj;
		if (!this.action.equals(other.action)) {
			return false;
		}
		if (!this.tenant.equals(other.tenant)) {
			return false;
		}
		return this.section.equals(other.section);
	}

}
