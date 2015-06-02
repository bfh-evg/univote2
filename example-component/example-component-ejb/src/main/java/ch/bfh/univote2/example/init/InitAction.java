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
package ch.bfh.univote2.example.init;

import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.data.UserInputPreconditionQuery;
import ch.bfh.univote2.component.core.data.UserInputTask;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class InitAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = "ExampleInitAction";
	private static final String INPUT_NAME = "InitInput";

	@EJB
	ActionManager actionManager;

	@Override
	@Asynchronous
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (!(notification instanceof InitUserInput)) {
			//TODO error
		}
		InitUserInput userInput = (InitUserInput) notification;
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, actionContext.getActionContextKey().getTenant(),
				userInput.getSection());
		ActionContext newContext = new InitActionContext(ack, null);
		this.actionManager.runFinished(newContext, ResultStatus.FINISHED);
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	}

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		List<PreconditionQuery> preconditionsQuerys = new ArrayList<>();
		return new InitActionContext(ack, preconditionsQuerys);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		return false;
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		UserInputPreconditionQuery query = new UserInputPreconditionQuery(new UserInputTask(INPUT_NAME,
				actionContext.getActionContextKey().getTenant(),
				actionContext.getActionContextKey().getSection()));
		actionContext.getPreconditionQueries().add(query);
	}

}
