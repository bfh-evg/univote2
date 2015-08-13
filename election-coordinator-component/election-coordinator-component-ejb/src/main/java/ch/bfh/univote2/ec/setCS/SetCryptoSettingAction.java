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
package ch.bfh.univote2.ec.setCS;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.message.Converter;
import ch.bfh.univote2.component.core.message.SecurityLevel;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import ch.bfh.univote2.ec.QueryFactory;
import ch.bfh.univote2.ec.pubTC.PublishTrusteeCertsActionContext;
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
public class SetCryptoSettingAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = SetCryptoSettingAction.class.getSimpleName();
	private static final Logger logger = Logger.getLogger(SetCryptoSettingAction.class.getName());

	@EJB
	private ActionManager actionManager;
	@EJB
	private InformationService informationService;
	@EJB
	private UniboardService uniboardService;
	// TODO Add field or remove next two comment lines
	//@EJB
	//private ConfigurationManager configurationManager;

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		this.informationService.informTenant(ack, "Created new context.");
		return new SetCryptoSettingActionContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		try {
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForCryptoSetting(actionContext.getSection()));
			return !result.getResult().getPost().isEmpty();
		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not request crypto setting.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not check post condition.");
			return false;
		}
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		try {
			SetCryptoSettingActionContext scsac = (SetCryptoSettingActionContext) actionContext;
			this.fillContext(scsac, this.retrieveSecurityLevel(scsac));
		} catch (UnivoteException ex) {
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
					QueryFactory.getQueryForTrustees(actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
			logger.log(Level.WARNING, "Could not get securityLevel.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Error retrieving securityLevel: " + ex.getMessage());
		}
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof PublishTrusteeCertsActionContext) {
			SetCryptoSettingActionContext scsac = (SetCryptoSettingActionContext) actionContext;
			if (scsac.getSecurityLevel() != null) {
				this.runInternal(scsac);
			} else {
				try {
					this.fillContext(scsac, this.retrieveSecurityLevel(scsac));
					this.runInternal(scsac);
				} catch (UnivoteException ex) {
					this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				}
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
		this.informationService.informTenant(actionContext.getActionContextKey(), "Notified.");
		if (actionContext instanceof PublishTrusteeCertsActionContext) {
			SetCryptoSettingActionContext scsac = (SetCryptoSettingActionContext) actionContext;
			if (notification instanceof PostDTO) {
				PostDTO post = (PostDTO) notification;

				try {
					SecurityLevel securityLevel = Converter.unmarshal(SecurityLevel.class, post.getMessage());
					this.fillContext(scsac, securityLevel);
					this.runInternal(scsac);
				} catch (Exception ex) {
					this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				}

			} else {
				this.informationService.informTenant(actionContext.getActionContextKey(), "Unknown notification: "
						+ notification.toString());
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			}
		} else {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Unsupported context.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}

	}

	protected SecurityLevel retrieveSecurityLevel(ActionContext actionContext) throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForSecurityLevel(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Security level not published yet.");
		}
		SecurityLevel securityLevel;
		try {
			securityLevel = Converter.unmarshal(SecurityLevel.class, result.getResult().getPost().get(0).getMessage());
		} catch (Exception ex) {
			throw new UnivoteException("Could not unmarshal securityLevel", ex);
		}
		return securityLevel;

	}

	protected void fillContext(SetCryptoSettingActionContext scsac, SecurityLevel secLevel) throws UnivoteException {
		scsac.setSecurityLevel(secLevel.getSecurityLevel());
	}

	private void runInternal(SetCryptoSettingActionContext actionContext) {
		//Where to retrieve the settings from?
	}
}
