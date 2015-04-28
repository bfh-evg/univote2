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

	protected boolean isInUse() {
		return inUse;
	}

	protected void setInUse(boolean inUse) {
		this.inUse = inUse;
	}

	protected LinkedBlockingQueue<Object> getQueuedNotifications() {
		return queuedNotifications;
	}

}
