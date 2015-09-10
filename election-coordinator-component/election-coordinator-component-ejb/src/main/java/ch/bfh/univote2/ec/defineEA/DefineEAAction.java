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
package ch.bfh.univote2.ec.defineEA;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.data.UserInputPreconditionQuery;
import ch.bfh.univote2.component.core.data.UserInputTask;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.MessageFactory;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import ch.bfh.univote2.common.query.QueryFactory;
import ch.bfh.univote2.component.core.manager.TenantManager;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class DefineEAAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = "DefineEAAction";
	private static final String INPUT_NAME = "EAName";
	private static final Logger logger = Logger.getLogger(DefineEAAction.class.getName());

	@EJB
	private ActionManager actionManager;
	@EJB
	private InformationService informationService;
	@EJB
	private UniboardService uniboardService;
	@EJB
	private TenantManager tenantManager;

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		this.informationService.informTenant(ack, "Created new context for " + ACTION_NAME);
		return new DefineEAActionContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {

		try {
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForEACert(actionContext.getSection()));
			return !result.getResult().getPost().isEmpty();
		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "No certificate for EA found on board, or another error occured.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"No certificate for EA found on board, or another error occured.");
			return false;
		}
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		//Add UserInput
		UserInputPreconditionQuery uiQuery = new UserInputPreconditionQuery(new UserInputTask(INPUT_NAME,
				actionContext.getActionContextKey().getTenant(),
				actionContext.getActionContextKey().getSection()));
		actionContext.getPreconditionQueries().add(uiQuery);
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof DefineEAActionContext) {
			DefineEAActionContext deaa = (DefineEAActionContext) actionContext;
			if (deaa.getName() != null) {
				this.runInternal(deaa);
			} else {
				logger.log(Level.INFO, actionContext.toString());
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"No name set for EA.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			}
		} else {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Unsupported context.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	@Override
	@Asynchronous
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (notification instanceof EANameUserInput) {
			EANameUserInput aeui = (EANameUserInput) notification;
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Entred value: " + aeui.getName());
			if (actionContext instanceof DefineEAActionContext) {
				DefineEAActionContext deaa = (DefineEAActionContext) actionContext;
				deaa.setName(aeui.getName());
				this.runInternal(deaa);
			} else {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Unsupported context.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			}
		} else {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Unknown notification: " + notification.toString());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}

	}

	private void runInternal(DefineEAActionContext actionContext) {
		try {
			//Get Certificate from UniCert
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNICERT.getValue(),
					QueryFactory.getQueryFormUniCertForEACert(actionContext.getName()));
			if (result.getResult().getPost().isEmpty()) {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"No certificate found for the specified EA name.");
				UserInputPreconditionQuery uiQuery = new UserInputPreconditionQuery(new UserInputTask(INPUT_NAME,
						actionContext.getActionContextKey().getTenant(),
						actionContext.getActionContextKey().getSection()));
				this.actionManager.reRequireUserInput(actionContext, uiQuery);
				this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
				return;
			}
			PostDTO post = result.getResult().getPost().get(0);

			//Grant urself the right to post
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), MessageFactory.createAccessRight(GroupEnum.ADMIN_CERT,
							this.tenantManager.getPublicKey(actionContext.getTenant()), 1), actionContext.getTenant());

			//Create message from the retrieved certificate
			byte[] message = post.getMessage();
			//Post message
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.ADMIN_CERT.getValue(), message, actionContext.getTenant());
			this.informationService.informTenant(actionContext.getActionContextKey(), "Posted EA.");
			this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not post message.");
			logger.log(Level.WARNING, "Could not post message. context: " + actionContext.getActionContextKey()
					+ ". ex: " + ex.getMessage(), ex);
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

}
