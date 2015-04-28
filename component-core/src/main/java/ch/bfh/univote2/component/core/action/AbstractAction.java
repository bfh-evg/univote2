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
package ch.bfh.univote2.component.core.action;

import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import java.util.List;

public abstract class AbstractAction implements Action {

	@Override
	public ActionContext prepareContext(String tenant, String section) {
		ActionContext actionContext = this.createContext(tenant, section);
		if (this.checkPostCondition(actionContext)) {
			actionContext.setPostCondition(true);
			return actionContext;
		}
		actionContext.setPostCondition(false);

		this.definePreconditions(actionContext);

		return actionContext;
	}

	protected abstract ActionContext createContext(String tenant, String section);

	protected abstract boolean checkPostCondition(ActionContext actionContext);

	protected abstract List<PreconditionQuery> definePreconditions(ActionContext actionContext);
}
