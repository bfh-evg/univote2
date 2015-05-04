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

import ch.bfh.univote2.component.core.data.PreconditionQuery;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public abstract class ActionContext {

	private final ActionContextKey actionContextKey;
	private final List<PreconditionQuery> preconditionQueries;
	LinkedBlockingQueue<Object> queuedNotifications;
	private boolean postCondition;
	private boolean inUse = false;

	public ActionContext(ActionContextKey actionContextKey, List<PreconditionQuery> preconditionQueries) {
		this.actionContextKey = actionContextKey;
		this.preconditionQueries = preconditionQueries;
		this.queuedNotifications = new LinkedBlockingQueue<>();
	}

	public ActionContextKey getActionContextKey() {
		return actionContextKey;
	}

	public List<PreconditionQuery> getPreconditionQueries() {
		return preconditionQueries;
	}

	public boolean checkPostCondition() {
		return postCondition;
	}

	public void setPostCondition(boolean postCondition) {
		this.postCondition = postCondition;
	}

	boolean isInUse() {
		return inUse;
	}

	void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	LinkedBlockingQueue<Object> getQueuedNotifications() {
		return queuedNotifications;
	}

}
