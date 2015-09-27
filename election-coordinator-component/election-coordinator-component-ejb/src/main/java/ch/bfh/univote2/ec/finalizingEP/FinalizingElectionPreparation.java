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
package ch.bfh.univote2.ec.finalizingEP;

import ch.bfh.uniboard.data.ResultDTO;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.ElectionDefinition;
import ch.bfh.univote2.common.message.ElectionDetails;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.MixedKeys;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.MessageFactory;
import ch.bfh.univote2.common.query.QueryFactory;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import sun.security.provider.DSAPublicKeyImpl;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class FinalizingElectionPreparation extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = "FinalizingElectionPreparation";
	private static final Logger logger = Logger.getLogger(FinalizingElectionPreparation.class.getName());

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
		logger.log(Level.INFO, "Created new context for" + ACTION_NAME);
		return new FinalizingElectionPreparationContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		try {
			//VotingData available?
			ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForVotingData(actionContext.getSection())).getResult();
			if (result.getPost().isEmpty()) {
				throw new UnivoteException("voting data not yet available.");
			}
			return true;
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(), "voting data not yet available");
			Logger.getLogger(FinalizingElectionPreparation.class.getName()).log(Level.SEVERE, null, ex);
		}
		return false;
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		if (!(actionContext instanceof FinalizingElectionPreparationContext)) {
			return;
		}
		FinalizingElectionPreparationContext context = (FinalizingElectionPreparationContext) actionContext;
		try {
			{
				ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						QueryFactory.getQueryForMixedKeys(actionContext.getSection())).getResult();
				if (result.getPost().isEmpty()) {
					throw new UnivoteException("Mixed keys not yet available");
				}
				MixedKeys mixedKeys = JSONConverter.unmarshal(MixedKeys.class, result.getPost().get(0).getMessage());
				context.setMixedKeys(mixedKeys);
			}
			{
				ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						QueryFactory.getQueryForCryptoSetting(actionContext.getSection())).getResult();
				if (result.getPost().isEmpty()) {
					throw new UnivoteException("Crypto setting not yet published.");
				}
				CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, result.getPost().get(0).getMessage());
				context.setCryptoSetting(cryptoSetting);
			}
			{
				ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						QueryFactory.getQueryForElectionDefinition(actionContext.getSection())).getResult();
				if (result.getPost().isEmpty()) {
					throw new UnivoteException("Election definition not yet published.");
				}
				ElectionDefinition electionDefinition = JSONConverter.unmarshal(ElectionDefinition.class, result.getPost().get(0).getMessage());
				context.setElectionDefinition(electionDefinition);
			}
			{
				ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						QueryFactory.getQueryForElectionDetails(actionContext.getSection())).getResult();
				if (result.getPost().isEmpty()) {
					throw new UnivoteException("Election Details not yet published.");
				}
				ElectionDetails electionDetails = JSONConverter.unmarshal(ElectionDetails.class, result.getPost().get(0).getMessage());
				context.setElectionDetails(electionDetails);
			}

		} catch (UnivoteException ex) {
			Logger.getLogger(FinalizingElectionPreparation.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void run(ActionContext actionContext) {
		if (!(actionContext instanceof FinalizingElectionPreparationContext)) {
			return;
		}
		FinalizingElectionPreparationContext context = (FinalizingElectionPreparationContext) actionContext;
		if (!context.gotAllNotifications()) {
			// Uuups some pre-requisits are missing....
			this.actionManager.runFinished(context, ResultStatus.FAILURE);
		}

		try {
			internalRun(context);
		} catch (UnivoteException ex) {
			Logger.getLogger(FinalizingElectionPreparation.class.getName()).log(Level.SEVERE, null, ex);
			this.informationService.informTenant(actionContext.getActionContextKey(), "Failed: " + ex);

			this.actionManager.runFinished(context, ResultStatus.FAILURE);
		}
	}

	@Override
	public void notifyAction(ActionContext actionContext, Object notification) {
		//Nothing to do here
	}

	private void internalRun(FinalizingElectionPreparationContext context) throws UnivoteException {

		CryptoSetting cryptoSetting = context.getCryptoSetting();
		CyclicGroup signatureGroup = CryptoProvider.getSignatureSetup(cryptoSetting.getSignatureSetting()).cryptoGroup;
		Element generator = CryptoProvider.getSignatureSetup(cryptoSetting.getSignatureSetting()).cryptoGenerator;

		MixedKeys mixedKeys = context.getMixedKeys();
		// Add AccessRight for new mixed vk^_i
		try {
			for (String mixedKey : mixedKeys.getMixedKeys()) {

				PublicKey pk = new DSAPublicKeyImpl(new BigInteger(mixedKey, 10), signatureGroup.getOrder(), signatureGroup.getZModOrder().getOrder(), generator.convertToBigInteger());
				byte[] message = MessageFactory.createAccessRight(GroupEnum.BALLOT, pk, 1);
				this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
						GroupEnum.ACCESS_RIGHT.getValue(), message, context.getTenant());

			}
			this.informationService.informTenant(context.getActionContextKey(),
					"new AccessRight for voters granted.");

			//TODO: Post VotingData
		} catch (InvalidKeyException ex) {
			Logger.getLogger(FinalizingElectionPreparation.class.getName()).log(Level.SEVERE, null, ex);
		}

	}
}
