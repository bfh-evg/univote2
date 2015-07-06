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
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import ch.bfh.univote2.ec.MessageFactory;
import ch.bfh.univote2.ec.QueryFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public abstract class GrantAccessRightEAAction extends AbstractAction implements NotifiableAction {

	@EJB
	ActionManager actionManager;
	@EJB
	InformationService informationService;
	@EJB
	UniboardService uniboardService;

	protected abstract String getActionName();

	protected abstract GroupEnum getGroupName();

	protected abstract Logger getLogger();

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(this.getActionName(), tenant, section);
		List<PreconditionQuery> preconditionsQuerys = new ArrayList<>();
		this.informationService.informTenant(ack, "Created new context.");
		return new GrantAccessRightEAActionContext(ack, preconditionsQuerys);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		try {
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForElectionDefinition(actionContext.getSection()));
			return !result.getResult().getPost().isEmpty();

		} catch (UnivoteException ex) {
			this.getLogger().log(Level.WARNING, "Could not request election definition.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not check post condition.");
			return false;
		}
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		try {
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForEACert(actionContext.getSection()));
			if (!result.getResult().getPost().isEmpty()) {
				//Load pem from message from post
				String messageString = new String(result.getResult().getPost().get(0).getMessage(),
						Charset.forName("UTF-8"));
				JsonReader jsonReader = Json.createReader(new StringReader(messageString));
				JsonObject message = jsonReader.readObject();
				String pem = message.getString("pem");
				GrantAccessRightEAActionContext gedac = (GrantAccessRightEAActionContext) actionContext;
				gedac.setPem(pem);
				return;
			}
		} catch (UnivoteException ex) {
			this.getLogger().log(Level.WARNING, "Could not get ea certificate.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Error retrieving ea certificate.");
		} catch (JsonException ex) {
			this.getLogger().log(Level.WARNING, "Could not parse ea certificate.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Error reading ea certificate.");
		}
		//Add UserInput
		BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
				QueryFactory.getQueryForEACert(actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
		actionContext.getPreconditionQueries().add(bQuery);
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof GrantAccessRightEAActionContext) {
			GrantAccessRightEAActionContext gedac = (GrantAccessRightEAActionContext) actionContext;
			if (!gedac.getPem().isEmpty()) {
				this.runInternal(gedac);
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
				//TODO Load pem from message from post
				String messageString = new String(post.getMessage(),
						Charset.forName("UTF-8"));
				try {
					JsonReader jsonReader = Json.createReader(new StringReader(messageString));
					JsonObject message = jsonReader.readObject();
					String pem = message.getString("pem");
					gedac.setPem(pem);
					this.runInternal(gedac);
				} catch (JsonException ex) {
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
			//Load certificate from pem string
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(actionContext.getPem().getBytes());
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
			PublicKey pk = cert.getPublicKey();
			//Get type(RSA/DSA)
			message = MessageFactory.createAccessRight(this.getGroupName(), pk, 1);
		} catch (CertificateException ex) {
			this.getLogger().log(Level.WARNING, "Unsupported certificate: {0}", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not read ea certificate.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		} catch (UnivoteException ex) {
			this.getLogger().log(Level.WARNING, "Unsupported public key type: {0}", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not post access right for " + this.getGroupName() + ".");
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
		}
		this.informationService.informTenant(actionContext.getActionContextKey(),
				"Posted access right for " + this.getGroupName() + ".");
		this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
	}

}
