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
package ch.bfh.univote2.ec.pubTC;

import ch.bfh.univote2.ec.defineEA.*;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.data.UserInputPreconditionQuery;
import ch.bfh.univote2.component.core.data.UserInputTask;
import ch.bfh.univote2.component.core.manager.ConfigurationManager;
import ch.bfh.univote2.component.core.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.QueryFactory;
import java.util.ArrayList;
import java.util.List;
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
public class PublishTrusteeCertsAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = "PublishTrusteeCertsAction";
	private static final String INPUT_NAME = "EAName";
	private static final String UNIVOTE_BOARD = "univote-board";
	private static final String UNICERT_BOARD = "unicert-board";
	private static final Logger logger = Logger.getLogger(PublishTrusteeCertsAction.class.getName());

	@EJB
	ActionManager actionManager;
	@EJB
	InformationService informationService;
	@EJB
	UniboardService uniboardService;
	@EJB
	ConfigurationManager configurationManager;

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		List<PreconditionQuery> preconditionsQuerys = new ArrayList<>();
		this.informationService.informTenant(ack, "Created new context.");
		return new PublishTrusteeCertsActionContext(ack, preconditionsQuerys);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {

		try {
			ResultContainerDTO result = this.uniboardService.get(UNIVOTE_BOARD,
					QueryFactory.getQueryForTrusteeCerts(actionContext.getSection()));
			return !result.getResult().getPost().isEmpty();
		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not request trustees certificates.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not check post condition.");
			return false;
		}
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		//Check trustee certs
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof PublishTrusteeCertsActionContext) {
			PublishTrusteeCertsActionContext ptcac = (PublishTrusteeCertsActionContext) actionContext;

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
			if (actionContext instanceof PublishTrusteeCertsActionContext) {
				PublishTrusteeCertsActionContext deaa = (PublishTrusteeCertsActionContext) actionContext;
				deaa.setName(aeui.getName());
				this.runInternal(deaa);
			} else {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Unsupported context.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			}
		} else {
			this.informationService.informTenant(actionContext.getActionContextKey(), "Unknown notification: "
					+ notification.toString());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}

	}

	private void runInternal(PublishTrusteeCertsActionContext actionContext) {
		try {
			//Get Certificate from UniCert
			ResultContainerDTO result = this.uniboardService.get(UNICERT_BOARD,
					QueryFactory.getQueryFormUniCertForCert(actionContext.getName()));
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
            //TODO Check signature of unicert?

			//Create message from the retrieved certificate
			byte[] message = post.getMessage();
			//Post message
			this.uniboardService.post(UNIVOTE_BOARD, actionContext.getSection(),
					GroupEnum.ADMIN_CERT.getValue(), message, actionContext.getTenant());
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not post message.");
			logger.log(Level.WARNING, "Could not post message. context: {0}. ex: {1}",
					new Object[]{actionContext.getActionContextKey(), ex.getMessage()});
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

}
