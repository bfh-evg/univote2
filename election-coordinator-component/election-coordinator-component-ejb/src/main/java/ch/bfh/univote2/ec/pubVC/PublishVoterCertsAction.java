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
package ch.bfh.univote2.ec.pubVC;

import ch.bfh.uniboard.clientlib.AttributeHelper;
import ch.bfh.uniboard.data.AttributesDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.common.message.Certificate;
import ch.bfh.univote2.common.message.ElectoralRoll;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.VoterCertificates;
import ch.bfh.univote2.common.query.AlphaEnum;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.MessageFactory;
import ch.bfh.univote2.common.query.QueryFactory;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
@Stateless
public class PublishVoterCertsAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = PublishVoterCertsAction.class.getSimpleName();
	private static final Logger logger = Logger.getLogger(PublishVoterCertsAction.class.getName());

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
		return new PublishVoterCertsActionContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		try {
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForVoterCertificates(actionContext.getSection()));
			return !result.getResult().getPost().isEmpty();
		} catch (UnivoteException ex) {
			return false;
		}
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		PublishVoterCertsActionContext ceksac = (PublishVoterCertsActionContext) actionContext;
		try {
			this.retrieveElectoralRoll(ceksac);
		} catch (UnivoteException ex) {
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
					QueryFactory.getQueryForElectoralRoll(actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
			logger.log(Level.INFO, "Could not get electoral roll.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Electoral roll not yet published.");
		}
	}

	@Override
	public void run(ActionContext actionContext) {
		if (!(actionContext instanceof PublishVoterCertsActionContext)) {
			return;
		}
		PublishVoterCertsActionContext context = (PublishVoterCertsActionContext) actionContext;
		try {
			if (context.getElectoralRoll() == null) {
				this.retrieveElectoralRoll(context);
			}
			internalRun(context);
		} catch (UnivoteException ex) {
			Logger.getLogger(PublishVoterCertsAction.class.getName()).log(Level.SEVERE, null, ex);
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	@Override
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (actionContext instanceof PublishVoterCertsActionContext) {
			PublishVoterCertsActionContext pvcac = (PublishVoterCertsActionContext) actionContext;
			if (notification instanceof PostDTO) {
				PostDTO post = (PostDTO) notification;
				AttributesDTO.AttributeDTO group
						= AttributeHelper.searchAttribute(post.getAlpha(), AlphaEnum.GROUP.getValue());
				String groupStr = ((StringValueDTO) group.getValue()).getValue();
				//Check Type
				if (groupStr.equals(GroupEnum.ELECTORAL_ROLL.getValue())) {
					try {
						//ER: save ER
						ElectoralRoll electoralRoll = JSONConverter.unmarshal(ElectoralRoll.class, post.getMessage());
						pvcac.setElectoralRoll(electoralRoll);
						this.internalRun(pvcac);
					} catch (UnivoteException ex) {
						this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
						this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					}
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

	private void internalRun(PublishVoterCertsActionContext context) throws UnivoteException {

		PublicKey publicKey = tenantManager.getPublicKey(context.getTenant());
		// Add 1 AccessRight for EC to voter certificates
		byte[] message = MessageFactory.createAccessRight(GroupEnum.VOTER_CERTIFICATES, publicKey, 1);
		this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
				GroupEnum.ACCESS_RIGHT.getValue(), message, context.getTenant());
		this.informationService.informTenant(context.getActionContextKey(),
				"new AccessRight for 'voter certificates' granted.");

		VoterCertificates voterCertificates = new VoterCertificates();

		for (String voterId : context.getElectoralRoll().getVoterIds()) {
			try {
				ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNICERT.getValue(),
						QueryFactory.getQueryFormUniCertForVoterCert(voterId));
				if (!result.getResult().getPost().isEmpty()) {
					PostDTO post = result.getResult().getPost().get(0);
					Certificate certi = JSONConverter.unmarshal(Certificate.class, post.getMessage());
					voterCertificates.getVoterCertificates().add(certi);

				} else {
					logger.log(Level.FINE, "No certificate available for: {0}", voterId);
				}
			} catch (UnivoteException ex) {
				this.informationService.informTenant(context.getActionContextKey(),
						"Could not request voter certificate: " + voterId);
				logger.log(Level.INFO, ex.getMessage());
			}

		}

		try {
			logger.log(Level.INFO, JSONConverter.marshal(voterCertificates));
			byte[] message2 = JSONConverter.marshal(voterCertificates).getBytes(("UTF-8"));
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.VOTER_CERTIFICATES.getValue(), message2, context.getTenant());
			this.informationService.informTenant(context.getActionContextKey(),
					"Voter certificates published.");
			this.actionManager.runFinished(context, ResultStatus.FINISHED);
		} catch (UnivoteException | UnsupportedEncodingException ex) {
			this.informationService.informTenant(context.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(context, ResultStatus.FAILURE);
		}
	}

	protected void retrieveElectoralRoll(PublishVoterCertsActionContext actionContext) throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForElectoralRoll(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Electoral roll not published yet.");
		}
		byte[] message = result.getResult().getPost().get(0).getMessage();
		ElectoralRoll electoralRoll = JSONConverter.unmarshal(ElectoralRoll.class, message);
		actionContext.setElectoralRoll(electoralRoll);
	}
}
