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
 * Key Class for the ActionContext used to search for an ActionContext in map etc.
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class ActionContextKey {

	private final String action;
	private final String tenant;
	private final String section;

	public ActionContextKey(String action, String tenant, String section) {
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
		int hash = 3;
		hash = 97 * hash + Objects.hashCode(this.action);
		hash = 97 * hash + Objects.hashCode(this.tenant);
		hash = 97 * hash + Objects.hashCode(this.section);
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
		final ActionContextKey other = (ActionContextKey) obj;
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
