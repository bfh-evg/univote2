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
package ch.bfh.univote2.ec.grantVK;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.ResultDTO;
import ch.bfh.uniboard.data.TransformException;
import ch.bfh.uniboard.data.Transformer;
import ch.bfh.uniboard.service.Attributes;
import ch.bfh.uniboard.service.StringValue;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModPrime;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.common.crypto.CryptoSetup;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.common.message.AccessRight;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.DL;
import ch.bfh.univote2.common.message.ElectionDefinition;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.MixedKeys;
import ch.bfh.univote2.common.query.AlphaEnum;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import ch.bfh.univote2.common.query.QueryFactory;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.JsonException;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class GrantAccessRightBallotAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = GrantAccessRightBallotAction.class.getSimpleName();

	private static final Logger logger = Logger.getLogger(GrantAccessRightBallotAction.class.getName());

	@EJB
	ActionManager actionManager;
	@EJB
	TenantManager tenantManager;
	@EJB
	InformationService informationService;
	@EJB
	private UniboardService uniboardService;

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		return new GrantAccessRightBallotActionContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		//TODO: Check if there are as many rights to post a ballot as there are mixed keys...
		return false;
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		if (!(actionContext instanceof GrantAccessRightBallotActionContext)) {
			logger.log(Level.SEVERE, "The actionContext was not the expected one.");
			return;
		}
		GrantAccessRightBallotActionContext skcac = (GrantAccessRightBallotActionContext) actionContext;
		this.checkAndSetCryptoSetting(skcac);
		this.checkAndSetMixedKeys(skcac);

		try {
			ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForElectionDefinition(actionContext.getSection())).getResult();
			if (result.getPost().isEmpty()) {
				throw new UnivoteException("Election definition not yet published.");
			}
			ElectionDefinition electionDefinition = JSONConverter.unmarshal(ElectionDefinition.class,
					result.getPost().get(0).getMessage());
			skcac.setElectionDefinition(electionDefinition);
		} catch (UnivoteException ex) {
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
					QueryFactory.getQueryForElectionDefinition(actionContext.getSection()),
					BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
		}

	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		if (!(actionContext instanceof GrantAccessRightBallotActionContext)) {
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		GrantAccessRightBallotActionContext garbac = (GrantAccessRightBallotActionContext) actionContext;
		//The following if is strange, as the run should not happen in this case?!
		if (garbac.isPreconditionReached() == null) {
			logger.log(Level.WARNING, "Run was called but preCondition is unknown in Context.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);

			return;
		}
		if (Objects.equals(garbac.isPreconditionReached(), Boolean.FALSE)) {
			logger.log(Level.WARNING, "Run was called but preCondition is not yet reached.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		try {
			CryptoSetting cryptoSetting = garbac.getCryptoSetting();
			MixedKeys mixedKeys = garbac.getMixedKeys();
			CryptoSetup sSetup = CryptoProvider.getSignatureSetup(cryptoSetting.getSignatureSetting());
			GStarModPrime signatureGroup = (GStarModPrime) sSetup.cryptoGroup;
			for (String mixedKey : mixedKeys.getMixedKeys()) {
				AccessRight ar = new AccessRight();
				ar.setCrypto(new DL(signatureGroup.getModulus().toString(), signatureGroup.getOrder().
						toString(), mixedKeys.getGenerator(), mixedKey));
				ar.setGroup(GroupEnum.BALLOT.getValue());
				ar.setAmount(1);
				ar.setStartTime(garbac.getElectionDefinition().getVotingPeriodBegin());
				ar.setEndTime(garbac.getElectionDefinition().getVotingPeriodEnd());
				byte[] message = JSONConverter.marshal(ar).getBytes();
				this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), garbac.getSection(),
						GroupEnum.ACCESS_RIGHT.getValue(), message, garbac.getTenant());

			}
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Granted access right for all registred voters. Action finished.");
			this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Unsupported public key type: {0}", ex.getMessage());
			//	this.informationService.informTenant(actionContext.getActionContextKey(),
			//			"Unsupported public key type: " + actionContext.getPublicKey() + ".");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}

	}

	@Override
	@Asynchronous
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (!(actionContext instanceof GrantAccessRightBallotActionContext)) {
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		GrantAccessRightBallotActionContext skcac = (GrantAccessRightBallotActionContext) actionContext;

		this.informationService.informTenant(actionContext.getActionContextKey(), "Notified.");

		if (!(notification instanceof PostDTO)) {
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		PostDTO post = (PostDTO) notification;
		try {
			Attributes attr = Transformer.convertAttributesDTOtoAttributes(post.getAlpha());
			attr.containsKey(AlphaEnum.GROUP.getValue());

			if (skcac.getCryptoSetting() == null && (attr.containsKey(AlphaEnum.GROUP.getValue())
					&& attr.getValue(AlphaEnum.GROUP.getValue()) instanceof StringValue
					&& GroupEnum.CRYPTO_SETTING.getValue()
					.equals(((StringValue) attr.getValue(AlphaEnum.GROUP.getValue())).getValue()))) {
				CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, post.getMessage());
				skcac.setCryptoSetting(cryptoSetting);
			}
			if (skcac.getMixedKeys() == null && (attr.containsKey(AlphaEnum.GROUP.getValue())
					&& attr.getValue(AlphaEnum.GROUP.getValue()) instanceof StringValue
					&& GroupEnum.MIXED_KEYS.getValue()
					.equals(((StringValue) attr.getValue(AlphaEnum.GROUP.getValue())).getValue()))) {
				MixedKeys mixedKeysDTO = JSONConverter.unmarshal(MixedKeys.class, post.getMessage());
				skcac.setMixedKeys(mixedKeysDTO);
			}
			if (skcac.getElectionDefinition() == null && (attr.containsKey(AlphaEnum.GROUP.getValue())
					&& attr.getValue(AlphaEnum.GROUP.getValue()) instanceof StringValue
					&& GroupEnum.ELECTION_DEFINITION.getValue()
					.equals(((StringValue) attr.getValue(AlphaEnum.GROUP.getValue())).getValue()))) {
				ElectionDefinition electionDefinition
						= JSONConverter.unmarshal(ElectionDefinition.class, post.getMessage());
				skcac.setElectionDefinition(electionDefinition);
			}
			run(actionContext);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		} catch (TransformException ex) {
			Logger.getLogger(GrantAccessRightBallotAction.class.getName()).log(Level.SEVERE, null, ex);
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	public void checkAndSetCryptoSetting(GrantAccessRightBallotActionContext actionContext) {
		ActionContextKey actionContextKey = actionContext.getActionContextKey();
		String section = actionContext.getSection();
		try {
			CryptoSetting cryptoSetting = actionContext.getCryptoSetting();

			//Add Notification
			if (cryptoSetting == null) {
				cryptoSetting = retrieveCryptoSetting(actionContext);
				actionContext.setCryptoSetting(cryptoSetting);
			}

		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not get securitySetting.", ex);
			informationService.informTenant(actionContextKey,
					"Error retrieving securitySetting: " + ex.getMessage());
		} catch (JsonException ex) {
			logger.log(Level.WARNING, "Could not parse securitySetting.", ex);
			informationService.informTenant(actionContextKey,
					"Error reading securitySetting.");
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Could not parse securitySetting.", ex);
			informationService.informTenant(actionContextKey,
					"Error reading securitySetting.");
		}
		if (actionContext.getCryptoSetting() == null) {
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
					QueryFactory.getQueryForCryptoSetting(section), BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
		}

	}

	public CryptoSetting retrieveCryptoSetting(ActionContext actionContext)
			throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForCryptoSetting(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Cryptosetting not published yet.");

		}
		CryptoSetting cryptoSetting
				= JSONConverter.unmarshal(CryptoSetting.class, result.getResult().getPost().get(0).getMessage());
		return cryptoSetting;

	}

	public void checkAndSetMixedKeys(GrantAccessRightBallotActionContext actionContext) {
		ActionContextKey actionContextKey = actionContext.getActionContextKey();
		String section = actionContext.getSection();
		try {
			MixedKeys mixedKeys = actionContext.getMixedKeys();

			//Add Notification
			if (mixedKeys == null) {
				mixedKeys = this.retrieveMixedKeys(actionContext);
				actionContext.setMixedKeys(mixedKeys);
			}

		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not get mixedKeys.", ex);
			informationService.informTenant(actionContextKey,
					"Error retrieving mixedKeys: " + ex.getMessage());
		} catch (JsonException ex) {
			logger.log(Level.WARNING, "Could not parse MixedKeys.", ex);
			informationService.informTenant(actionContextKey,
					"Error reading mixedKeys.");
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Could not parse mixed keys.", ex);
			informationService.informTenant(actionContextKey,
					"Error reading mixed keys.");
		}
		if (actionContext.getCryptoSetting() == null) {
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
					QueryFactory.getQueryForMixedKeys(section), BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
		}

	}

	public MixedKeys retrieveMixedKeys(ActionContext actionContext)
			throws UnivoteException {
		ResultContainerDTO result = uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForMixedKeys(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("mixed keys not published yet.");

		}
		MixedKeys mixedKeys
				= JSONConverter.unmarshal(MixedKeys.class, result.getResult().getPost().get(0).getMessage());
		return mixedKeys;

	}

}
