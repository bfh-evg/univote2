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
package ch.bfh.univote2.ec.generic;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.common.message.Certificate;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import ch.bfh.univote2.common.query.MessageFactory;
import ch.bfh.univote2.common.query.QueryFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public abstract class GrantAccessRightEAAction implements NotifiableAction {

	@EJB
	private ActionManager actionManager;
	@EJB
	private InformationService informationService;
	@EJB
	private UniboardService uniboardService;

	protected abstract String getActionName();

	protected abstract GroupEnum getGroupName();

	protected abstract Logger getLogger();

	@Override
	public ActionContext prepareContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(this.getActionName(), tenant, section);
		GrantAccessRightEAActionContext actionContext = new GrantAccessRightEAActionContext(ack);
		boolean precondition = false;
		try {
			//Check precondition
			this.retrieveEA(actionContext);
			precondition = true;
		} catch (UnivoteException ex) {
			this.getLogger().log(Level.WARNING, "Could not request ea certificate.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not request ea certificate.");
		}

		if (precondition) {
			//If fullfilled check postcondition
			try {
				actionContext.setPostCondition(this.checkAccessRight(actionContext));
			} catch (UnivoteException ex) {
				this.getLogger().log(Level.WARNING, "Could not check post condition.", ex);
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Could not check post condition.");
			}
			//Set postcondition
		} else {
			//Set postcondition
			actionContext.setPostCondition(false);
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(QueryFactory.getQueryForTrusteeCerts(
					actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
		}

		return actionContext;
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof GrantAccessRightEAActionContext) {
			GrantAccessRightEAActionContext gareac = (GrantAccessRightEAActionContext) actionContext;
			if (gareac.getPublicKey() != null) {
				this.runInternal(gareac);
			} else {
				try {
					//Check precondition
					this.retrieveEA(gareac);
					gareac.getPreconditionQueries().clear();
					this.runInternal(gareac);

				} catch (UnivoteException ex) {
					this.getLogger().log(Level.WARNING, "Could not request ea certificate.", ex);
					this.informationService.informTenant(actionContext.getActionContextKey(),
							"Could not request ea certificate.");
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
		if (notification instanceof PostDTO) {
			if (actionContext instanceof GrantAccessRightEAActionContext) {
				GrantAccessRightEAActionContext gedac = (GrantAccessRightEAActionContext) actionContext;
				PostDTO post = (PostDTO) notification;
				try {
					this.parseEACert(post.getMessage(), gedac);
					this.runInternal(gedac);
				} catch (UnivoteException ex) {
					this.getLogger().log(Level.WARNING, "Could not parse ea certificate.", ex);
					this.informationService.informTenant(actionContext.getActionContextKey(),
							"Error reading ea certificate.");
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				}

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

	private void runInternal(GrantAccessRightEAActionContext actionContext) {
		byte[] message;
		try {
			//Get type(RSA/DSA)
			message = MessageFactory.createAccessRight(this.getGroupName(), actionContext.getPublicKey(), 1);
		} catch (UnivoteException ex) {
			this.getLogger().log(Level.WARNING, "Unsupported public key type: {0}", ex.getMessage());
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Unsupported public key type: " + actionContext.getPublicKey() + ".");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}

		//post message
		try {
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), message, actionContext.getTenant());
		} catch (UnivoteException ex) {
			this.getLogger().log(Level.WARNING, "Unsupported public key type: {0}", ex.getMessage());
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not post access right for " + this.getGroupName() + ".");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
		this.informationService.informTenant(actionContext.getActionContextKey(),
				"Posted access right for " + this.getGroupName() + ".");
		this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
	}

	private void retrieveEA(GrantAccessRightEAActionContext actionContext) throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForEACert(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("EA certificate not published yet.");
		}
		this.parseEACert(result.getResult().getPost().get(0).getMessage(), actionContext);
	}

	private void parseEACert(byte[] message, GrantAccessRightEAActionContext actionContext)
			throws UnivoteException {
		Certificate eaCertificate;
		try {
			eaCertificate = JSONConverter.unmarshal(Certificate.class, message);
		} catch (Exception ex) {
			throw new UnivoteException("Invalid ea certificate message. Can't be unmarshalled.", ex);
		}
		String pem = eaCertificate.getPem();
		if (pem == null) {
			throw new UnivoteException("Invalid certificate message. pem is missing.");
		}
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(pem.getBytes());
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
			PublicKey pk = cert.getPublicKey();
			actionContext.setPublicKey(pk);
		} catch (CertificateException ex) {
			throw new UnivoteException("Invalid trustees certificates message. Could not load pem.", ex);
		}

	}

	private boolean checkAccessRight(GrantAccessRightEAActionContext actionContext) throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForAccessRight(actionContext.getSection(),
						actionContext.getPublicKey(), this.getGroupName()));
		return !result.getResult().getPost().isEmpty();

	}

}
