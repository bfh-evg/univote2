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
package ch.bfh.univote2.component.core.actionmanager;

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

	@Override
	public String toString() {
		return "ActionContextKey{" + "action=" + action + ", tenant=" + tenant + ", section=" + section + '}';
	}

}
