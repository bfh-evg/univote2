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

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.common.message.Certificate;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.PreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.TrusteeCertificates;
import ch.bfh.univote2.common.message.Trustees;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.MessageFactory;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import ch.bfh.univote2.common.query.QueryFactory;
import ch.bfh.univote2.component.core.manager.TenantManager;
import java.nio.charset.Charset;
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

	private static final String ACTION_NAME = PublishTrusteeCertsAction.class.getSimpleName();
	private static final Logger logger = Logger.getLogger(PublishTrusteeCertsAction.class.getName());

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
		List<PreconditionQuery> preconditionsQuerys = new ArrayList<>();
		return new PublishTrusteeCertsActionContext(ack, preconditionsQuerys);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {

		try {
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
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
		try {
			PublishTrusteeCertsActionContext ptcac = (PublishTrusteeCertsActionContext) actionContext;
			this.fillContext(ptcac, this.retrieveTrustees(ptcac));
		} catch (UnivoteException ex) {
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
					QueryFactory.getQueryForTrustees(actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
			logger.log(Level.WARNING, "Could not get trustees.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Error retrieving trustees: " + ex.getMessage());
		}
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof PublishTrusteeCertsActionContext) {
			PublishTrusteeCertsActionContext ptcac = (PublishTrusteeCertsActionContext) actionContext;
			if (!ptcac.getMixers().isEmpty() && !ptcac.getTalliers().isEmpty()) {
				this.runInternal(ptcac);
			} else {
				try {
					this.fillContext(ptcac, this.retrieveTrustees(ptcac));
					this.runInternal(ptcac);
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
			PublishTrusteeCertsActionContext ptcac = (PublishTrusteeCertsActionContext) actionContext;
			if (notification instanceof PostDTO) {
				PostDTO post = (PostDTO) notification;

				try {
					this.fillContext(ptcac, post.getMessage());
					this.runInternal(ptcac);
				} catch (UnivoteException ex) {
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

	protected byte[] retrieveTrustees(ActionContext actionContext) throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForTrustees(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Trustees not published yet.");
		}
		return result.getResult().getPost().get(0).getMessage();

	}

	protected void fillContext(PublishTrusteeCertsActionContext ptcac, byte[] message) throws UnivoteException {
		Trustees trustees = JSONConverter.unmarshal(Trustees.class, message);
		ptcac.getMixers().addAll(trustees.getMixerIds());
		ptcac.getTalliers().addAll(trustees.getTallierIds());
	}

	private void runInternal(PublishTrusteeCertsActionContext actionContext) {

		List<String> missingMixers = new ArrayList<>();
		List<Certificate> mixerCerts = new ArrayList<>();
		for (String mixer : actionContext.getMixers()) {
			//Get Certificate from UniCert
			ResultContainerDTO result;
			try {
				result = this.uniboardService.get(BoardsEnum.UNICERT.getValue(),
						QueryFactory.getQueryFormUniCertForTrusteeCert(mixer));
			} catch (UnivoteException ex) {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Could not retrieve cert for mixer :" + mixer);
				missingMixers.add(mixer);
				break;
			}
			if (result.getResult().getPost().isEmpty()) {
				missingMixers.add(mixer);
			}
			PostDTO post = result.getResult().getPost().get(0);
			try {
				mixerCerts.add(JSONConverter.unmarshal(Certificate.class, post.getMessage()));
			} catch (UnivoteException ex) {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Post was not a valid certificate:" + mixer);
				logger.log(Level.WARNING, ex.getMessage());
				missingMixers.add(mixer);
			}
		}
		List<String> missingTalliers = new ArrayList<>();
		List<Certificate> tallierCerts = new ArrayList<>();
		for (String tallier : actionContext.getMixers()) {
			//Get Certificate from UniCert
			ResultContainerDTO result;
			try {
				result = this.uniboardService.get(BoardsEnum.UNICERT.getValue(),
						QueryFactory.getQueryFormUniCertForTrusteeCert(tallier));
			} catch (UnivoteException ex) {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Could not retrieve cert for tallier :" + tallier);
				missingTalliers.add(tallier);
				break;
			}
			if (result.getResult().getPost().isEmpty()) {
				missingTalliers.add(tallier);
			}
			PostDTO post = result.getResult().getPost().get(0);
			try {
				tallierCerts.add(JSONConverter.unmarshal(Certificate.class, post.getMessage()));
			} catch (UnivoteException ex) {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Post was not a valid certificate:" + tallier);
				missingTalliers.add(tallier);
			}
		}
		if (missingTalliers.isEmpty() && missingMixers.isEmpty()) {

			try {
				//Grant urself the right to post
				this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
						GroupEnum.ACCESS_RIGHT.getValue(),
						MessageFactory.createAccessRight(GroupEnum.TRUSTEE_CERTIFICATES,
								this.tenantManager.getPublicKey(actionContext.getTenant()), 1),
						actionContext.getTenant());
			} catch (UnivoteException ex) {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Could not post accessRight.");
				logger.log(Level.WARNING, "Could not post accessRight. context: " + actionContext.getActionContextKey()
						+ ". ex: " + ex.getMessage(), ex);
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}

			//Create message from the retrieved certificate
			TrusteeCertificates trusteeCertificates = new TrusteeCertificates(mixerCerts, tallierCerts);
			//Post message
			String message = "";
			try {
				message = JSONConverter.marshal(trusteeCertificates);
				this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
						GroupEnum.TRUSTEE_CERTIFICATES.getValue(),
						message.getBytes(Charset.forName("UTF-8")),
						actionContext.getTenant());
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Posted trustee certs. Action finished.");
				this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
			} catch (UnivoteException ex) {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Could not post message.");
				logger.log(Level.WARNING, "Could not post message. context: {0}. ex: {1}",
						new Object[]{actionContext.getActionContextKey(), ex.getMessage()});
				logger.log(Level.INFO, "Message: {0}", message);
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			}
		} else {
			for (String tallier : missingTalliers) {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Could not find certificates for tallier:" + tallier);
			}
			for (String mixer : missingMixers) {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Could not find certificates for mixer:" + mixer);
			}
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}

	}

}
