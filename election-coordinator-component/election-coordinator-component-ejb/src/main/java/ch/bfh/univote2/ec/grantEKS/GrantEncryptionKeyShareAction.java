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
package ch.bfh.univote2.ec.grantEKS;

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
import ch.bfh.univote2.common.message.TrusteeCertificates;
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
import javax.json.JsonException;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class GrantEncryptionKeyShareAction implements NotifiableAction {

	private static final String ACTION_NAME = GrantEncryptionKeyShareAction.class.getSimpleName();
	private static final Logger logger = Logger.getLogger(GrantEncryptionKeyShareAction.class.getName());

	@EJB
	private ActionManager actionManager;
	@EJB
	private InformationService informationService;
	@EJB
	private UniboardService uniboardService;

	@Override
	public ActionContext prepareContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		this.informationService.informTenant(ack, "Created new context.");
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		boolean precondition = false;
		try {
			//Check precondition
			this.retrieveTalliers(actionContext);
			precondition = true;
		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not request trustee certificates.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not request trustee certificates.");
		}

		if (precondition) {
			//If fullfilled check postcondition
			boolean finished = true;
			for (AccessRightCandidate candidate : actionContext.getTalliers()) {
				try {
					if (this.checkAccessRight(actionContext, candidate.getPublicKey())) {
						candidate.setGranted(AccessRightStatus.GRANTED);
					} else {
						candidate.setGranted(AccessRightStatus.NOT_GRANTED);
						finished = false;
					}
				} catch (UnivoteException ex) {
					candidate.setGranted(AccessRightStatus.UNKOWN);
					finished = false;
					logger.log(Level.WARNING, "Could not check post condition.", ex);
					this.informationService.informTenant(actionContext.getActionContextKey(),
							"Could not check post condition.");
				}
			}
			//Set postcondition
			actionContext.setPostCondition(finished);
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
		if (actionContext instanceof GrantEncryptionKeyShareActionContext) {
			GrantEncryptionKeyShareActionContext gedac = (GrantEncryptionKeyShareActionContext) actionContext;
			//Check if trustee certs were retrieved
			if (gedac.getPreconditionQueries().isEmpty()) {
				this.runInternal(gedac);
			} //Try to retrieve trustee certs
			else {
				try {
					//Check precondition
					this.retrieveTalliers(gedac);
					gedac.getPreconditionQueries().clear();
					this.runInternal(gedac);

				} catch (UnivoteException ex) {
					logger.log(Level.WARNING, "Could not request trustee certificates.", ex);
					if (ex.getCause() != null) {
						logger.log(Level.WARNING, ex.getCause().getMessage());
					}
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
		if (actionContext instanceof GrantEncryptionKeyShareActionContext) {
			GrantEncryptionKeyShareActionContext geksac = (GrantEncryptionKeyShareActionContext) actionContext;
			if (notification instanceof PostDTO) {
				PostDTO post = (PostDTO) notification;
				try {
					byte[] message = post.getMessage();
					TrusteeCertificates trusteeCertificates;
					try {
						trusteeCertificates = JSONConverter.unmarshal(TrusteeCertificates.class, message);
					} catch (Exception ex) {
						throw new UnivoteException("Invalid trustees certificates message. Can't be unmarshalled.", ex);
					}
					this.parseTrusteeCerts(trusteeCertificates, geksac);
					this.runInternal(geksac);
				} catch (UnivoteException | JsonException ex) {
					this.informationService.informTenant(actionContext.getActionContextKey(),
							ex.getMessage());
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

	protected void retrieveTalliers(GrantEncryptionKeyShareActionContext actionContext) throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForTrusteeCerts(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Trustees certificates not published yet.");
		}

		byte[] message = result.getResult().getPost().get(0).getMessage();
		TrusteeCertificates trusteeCertificates;
		try {
			trusteeCertificates = JSONConverter.unmarshal(TrusteeCertificates.class, message);
		} catch (Exception ex) {
			throw new UnivoteException("Invalid trustees certificates message. Can not be unmarshalled.", ex);
		}
		this.parseTrusteeCerts(trusteeCertificates, actionContext);
	}

	protected void parseTrusteeCerts(TrusteeCertificates trusteeCertificates,
			GrantEncryptionKeyShareActionContext actionContext)
			throws UnivoteException {
		for (Certificate tallier : trusteeCertificates.getTallierCertificates()) {
			String pem = tallier.getPem();
			if (pem == null) {
				throw new UnivoteException("Invalid trustees certificates message. Pem missing.");
			}
			try {
				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
				InputStream in = new ByteArrayInputStream(pem.getBytes());
				X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
				PublicKey pk = cert.getPublicKey();
				actionContext.getTalliers().add(new AccessRightCandidate(pk));
			} catch (CertificateException ex) {
				throw new UnivoteException("Invalid trustees certificates message. Could not load pem.", ex);
			}
		}
	}

	protected boolean checkAccessRight(ActionContext actionContext, PublicKey publicKey) throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForAccessRight(actionContext.getSection(),
						publicKey, GroupEnum.ENCRYPTION_KEY_SHARE));
		return !result.getResult().getPost().isEmpty();
	}

	protected void runInternal(GrantEncryptionKeyShareActionContext actionContext) {
		boolean finished = true;
		for (AccessRightCandidate candidate : actionContext.getTalliers()) {
			switch (candidate.getGranted()) {
				case GRANTED:
					//NOP
					break;
				case NOT_GRANTED:
					//Try to grant access right
					if (!this.grantAccessRight(actionContext, candidate.getPublicKey())) {
						finished = false;
					} else {
						candidate.setGranted(AccessRightStatus.GRANTED);
					}
					break;
				case UNKOWN: {
					try {
						//Check access right
						//If not granted try to grant access right
						if (this.checkAccessRight(actionContext, candidate.getPublicKey())) {
							candidate.setGranted(AccessRightStatus.GRANTED);
						} else {
							candidate.setGranted(AccessRightStatus.NOT_GRANTED);
							if (!this.grantAccessRight(actionContext, candidate.getPublicKey())) {
								finished = false;
							} else {
								candidate.setGranted(AccessRightStatus.GRANTED);
							}
						}
					} catch (UnivoteException ex) {
						logger.log(Level.WARNING, "Could not check accessRight.", ex);
						this.informationService.informTenant(actionContext.getActionContextKey(),
								"Could not check access right for: " + candidate.getPublicKey());
						finished = false;
					}
				}
				break;
			}
		}
		if (finished) {
			this.informationService.informTenant(actionContext.getActionContextKey(), "Granted all accessRights.");
			this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
		} else {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not grant all accessRights.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	protected boolean grantAccessRight(ActionContext actionContext, PublicKey publickey) {
		try {
			byte[] message = MessageFactory.createAccessRight(GroupEnum.ENCRYPTION_KEY_SHARE, publickey, 1);
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), message, actionContext.getTenant());
			return true;
		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, ex.getMessage());
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not post access right for encryption key share.");
			return false;
		}
	}

}
