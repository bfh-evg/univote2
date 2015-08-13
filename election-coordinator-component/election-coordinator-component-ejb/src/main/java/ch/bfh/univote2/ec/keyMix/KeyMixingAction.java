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
package ch.bfh.univote2.ec.keyMix;

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
import ch.bfh.univote2.component.core.message.Certificate;
import ch.bfh.univote2.component.core.message.Converter;
import ch.bfh.univote2.component.core.message.TrusteeCertificates;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import ch.bfh.univote2.ec.QueryFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class KeyMixingAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = KeyMixingAction.class.getSimpleName();
	private static final Logger logger = Logger.getLogger(KeyMixingAction.class.getName());

	@EJB
	ActionManager actionManager;
	@EJB
	InformationService informationService;
	@EJB
	UniboardService uniboardService;

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		this.informationService.informTenant(ack, "Created new context.");
		KeyMixingActionContext actionContext = new KeyMixingActionContext(ack);
		return actionContext;
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		try {
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForMixedKeys(actionContext.getSection()));
			return !result.getResult().getPost().isEmpty();
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not check post condition. UniBoard is not reachable.");
			return false;
		}
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		//TODO
		BoardPreconditionQuery bQuery = new BoardPreconditionQuery(QueryFactory.getQueryForMixingResults(
				actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
		actionContext.getPreconditionQueries().add(bQuery);
	}

	@Override
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof KeyMixingActionContext) {
			KeyMixingActionContext ceksac = (KeyMixingActionContext) actionContext;
			//TODO
		} else {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Unsupported context.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	@Override
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (actionContext instanceof KeyMixingActionContext) {
			KeyMixingActionContext ceksac = (KeyMixingActionContext) actionContext;
			if (notification instanceof PostDTO) {
				PostDTO post = (PostDTO) notification;
				//TODO
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

	protected JsonObject retrieveElectoralRoll(ActionContext actionContext) throws JsonException, UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForElectoralRoll(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Electoral roll not published yet.");
		}
		//Prepare JsonObject
		//TODO Replace
		String messageString = new String(result.getResult().getPost().get(0).getMessage(),
				Charset.forName("UTF-8"));
		JsonReader jsonReader = Json.createReader(new StringReader(messageString));
		return jsonReader.readObject();

	}

	protected void retrieveMixers(KeyMixingActionContext actionContext) throws
			UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForTrusteeCerts(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Trustees certificates not published yet.");
		}
		byte[] message = result.getResult().getPost().get(0).getMessage();
		TrusteeCertificates trusteeCertificates;
		try {
			trusteeCertificates = Converter.unmarshal(TrusteeCertificates.class, message);
		} catch (Exception ex) {
			throw new UnivoteException("Invalid trustees certificates message. Can not be unmarshalled.");
		}
		List<Certificate> mixers = trusteeCertificates.getMixerCertificates();
		if (mixers == null || mixers.isEmpty()) {
			throw new UnivoteException("Invalid trustees certificates message. mixerCertificates is missing.");
		}
		for (Certificate c : trusteeCertificates.getMixerCertificates()) {
			try {
				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
				InputStream in = new ByteArrayInputStream(c.getPem().getBytes());
				X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
				PublicKey pk = cert.getPublicKey();
				actionContext.getMixerKeys().put(c.getCommonName(), pk);
			} catch (CertificateException ex) {
				throw new UnivoteException("Invalid trustees certificates message. Could not load pem.", ex);
			}
		}
	}

	protected void createMixingRequest(KeyMixingActionContext actionContext, String mixer) {
		//TODO

	}

	protected void validteMixingResult(KeyMixingActionContext actionContext, Object mixingResult, String mixer) throws
			UnivoteException {
		//TODO
	}
}
