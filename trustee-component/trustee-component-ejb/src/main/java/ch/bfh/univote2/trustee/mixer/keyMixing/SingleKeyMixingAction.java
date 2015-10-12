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
package ch.bfh.univote2.trustee.mixer.keyMixing;

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultDTO;
import ch.bfh.uniboard.data.Transformer;
import ch.bfh.uniboard.service.Attributes;
import ch.bfh.uniboard.service.StringValue;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.FiatShamirSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.classes.EqualityPreimageProofSystem;
import ch.bfh.unicrypt.helper.converter.classes.ConvertMethod;
import ch.bfh.unicrypt.helper.converter.classes.biginteger.ByteArrayToBigInteger;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.BigIntegerToByteArray;
import ch.bfh.unicrypt.helper.converter.classes.bytearray.StringToByteArray;
import ch.bfh.unicrypt.helper.converter.interfaces.Converter;
import ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import ch.bfh.unicrypt.helper.hash.HashMethod;
import ch.bfh.unicrypt.helper.math.Alphabet;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringElement;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.StringMonoid;
import ch.bfh.unicrypt.math.algebra.general.classes.Pair;
import ch.bfh.unicrypt.math.algebra.general.classes.Triple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.unicrypt.math.function.classes.GeneratorFunction;
import ch.bfh.unicrypt.math.function.interfaces.Function;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.common.crypto.CryptoSetup;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.SigmaProof;
import ch.bfh.univote2.common.message.SingleKeyMixingRequest;
import ch.bfh.univote2.common.message.SingleKeyMixingResult;
import ch.bfh.univote2.common.query.AlphaEnum;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.QueryFactory;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.SecurePersistenceService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.trustee.BoardsEnum;
import ch.bfh.univote2.trustee.TrusteeActionHelper;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
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
public class SingleKeyMixingAction extends AbstractAction implements NotifiableAction {

	//See report.pdf (6.2.2.b)
	private static final String ACTION_NAME = SingleKeyMixingAction.class.getSimpleName();

	private static final Logger logger = Logger.getLogger(SingleKeyMixingAction.class.getName());

	@EJB
	ActionManager actionManager;
	@EJB
	TenantManager tenantManager;
	@EJB
	InformationService informationService;
	@EJB
	private UniboardService uniboardService;
	@EJB
	private SecurePersistenceService securePersistenceService;

	@Override
	protected ActionContext createContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		return new SingleKeyMixingActionContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		if (!(actionContext instanceof SingleKeyMixingActionContext)) {
			logger.log(Level.SEVERE, "The actionContext was not the expected one.");
			return false;
		}
		SingleKeyMixingActionContext vmac = (SingleKeyMixingActionContext) actionContext;
// TODO:
// This is not a good query, as it might return the result of an old request...
// We have to think on that again, so it will be left out. This way, a Request could be executed multiple times
// Problem: There might be no AccessRight to write down the result. Check with Sevi.
//	try {
//	    PublicKey publicKey = tenantManager.getPublicKey(actionContext.getTenant());
//	    ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
//								 QueryFactory.getQueryForSingleKeyMixingResult(actionContext.getSection(), publicKey));
//	    if (!result.getResult().getPost().isEmpty()) {
//		return true;
//	    }
//	} catch (UnivoteException ex) {
//	    logger.log(Level.WARNING, "Could not request key mixing result.", ex);
//	    this.informationService.informTenant(actionContext.getActionContextKey(),
//						 "Could not check post condition.");
//	    return false;
//	}
		return false;
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		if (!(actionContext instanceof SingleKeyMixingActionContext)) {
			logger.log(Level.SEVERE, "The actionContext was not the expected one.");
			return;
		}
		SingleKeyMixingActionContext skmac = (SingleKeyMixingActionContext) actionContext;
		TrusteeActionHelper.checkAndSetCryptoSetting(skmac, uniboardService, tenantManager, informationService, logger);
		TrusteeActionHelper.checkAndSetAccsessRight(skmac, GroupEnum.SINGLE_KEY_MIXING_RESULT, uniboardService,
				tenantManager, informationService, logger);
		this.checkAndSetSingleKeyMixingRequest(skmac);
	}

	protected void checkAndSetSingleKeyMixingRequest(SingleKeyMixingActionContext actionContext) {
		ActionContextKey actionContextKey = actionContext.getActionContextKey();
		String section = actionContext.getSection();
		try {
			SingleKeyMixingRequest singleKeyMixingRequest = actionContext.getSingleKeyMixingRequest();

			//Add Notification
			if (singleKeyMixingRequest == null) {
				singleKeyMixingRequest = retrieveSingleKeyMixingRequest(actionContext);
				actionContext.setSingleKeyMixingRequest(singleKeyMixingRequest);
			}

		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not get single key mixing request.", ex);
			informationService.informTenant(actionContextKey,
					"Error retrieving single key mixing request: " + ex.getMessage());
		} catch (JsonException ex) {
			logger.log(Level.WARNING, "Could not parse single key mixing request.", ex);
			informationService.informTenant(actionContextKey,
					"Error reading single key mixing request.");
		} catch (Exception ex) {
			logger.log(Level.WARNING, "Could not parse single key mixing request.", ex);
			informationService.informTenant(actionContextKey,
					"Error reading single key mixing request.");
		}
		try {
			if (actionContext.getSingleKeyMixingRequest() == null) {
				//Add Notification
				BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
						QueryFactory.getQueryForSingleKeyMixingRequestsForMixer(section, actionContext.getTenant()),
						BoardsEnum.UNIVOTE.getValue());
				actionContext.getPreconditionQueries().add(bQuery);
			}
		} catch (UnivoteException exception) {
			logger.log(Level.WARNING, "Could not get tenant for single key mixing request.", exception);
			informationService.informTenant(actionContextKey,
					"Error retrieving tenant for single key mixing request: " + exception.getMessage());
		}

	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		if (!(actionContext instanceof SingleKeyMixingActionContext)) {
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		SingleKeyMixingActionContext skmac = (SingleKeyMixingActionContext) actionContext;
		//The following if is strange, as the run should not happen in this case?!
//		if (skmac.isPreconditionReached() == null) {
//			logger.log(Level.WARNING, "Run was called but preCondition is unknown in Context.");
//			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
//
//			return;
//		}
//		if (Objects.equals(skmac.isPreconditionReached(), Boolean.FALSE)) {
//			logger.log(Level.WARNING, "Run was called but preCondition is not yet reached.");
//			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
//			return;
//		}
		String tenant = actionContext.getTenant();
		String section = actionContext.getSection();

		SingleKeyMixingRequest singleKeyMixingRequest = skmac.getSingleKeyMixingRequest();
		CryptoSetting cryptoSetting = skmac.getCryptoSetting();

		try {

			BigInteger alpha = null;
			BigInteger gMinus = null;

			alpha = securePersistenceService.retrieve(tenant, section, KeyMixingAction.PERSISTENCE_NAME_FOR_ALPHA);
			gMinus = securePersistenceService.retrieve(tenant, section, KeyMixingAction.PERSISTENCE_NAME_FOR_G_MINUS);
			if (alpha == null) {
				logger.log(Level.WARNING, "Run was called but alpha is not set for this trustee.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
			SingleKeyMixingResult singleKeyMixingResult = createSingleKeyMixingResult(tenant, singleKeyMixingRequest,
					cryptoSetting, alpha, gMinus);
			String singleKeyMixingResultString = JSONConverter.marshal(singleKeyMixingResult);
			byte[] singleKeyMixingResultByteArray = singleKeyMixingResultString.getBytes(Charset.forName("UTF-8"));

			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), section,
					GroupEnum.SINGLE_KEY_MIXING_RESULT.getValue(), singleKeyMixingResultByteArray, tenant);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Posted single key mixing result. Action finished.");
			this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not post single key mixing result. Action failed."
					+ " Is there an alpha value set for this trustee?");
			Logger.getLogger(SingleKeyMixingAction.class.getName()).log(Level.SEVERE, null, ex);
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		} catch (Exception ex) {
			Logger.getLogger(SingleKeyMixingAction.class.getName()).log(Level.SEVERE, null, ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not marshal key mixing result. Action failed.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	@Override
	@Asynchronous
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (!(actionContext instanceof SingleKeyMixingActionContext)) {
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		SingleKeyMixingActionContext skmac = (SingleKeyMixingActionContext) actionContext;

		this.informationService.informTenant(actionContext.getActionContextKey(), "Notified.");

		if (!(notification instanceof PostDTO)) {
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		PostDTO post = (PostDTO) notification;
		try {
			Attributes attr = Transformer.convertAttributesDTOtoAttributes(post.getAlpha());
			attr.containsKey(AlphaEnum.GROUP.getValue());

			if (attr.containsKey(AlphaEnum.GROUP.getValue())
					&& attr.getValue(AlphaEnum.GROUP.getValue()) instanceof StringValue
					&& GroupEnum.ACCESS_RIGHT.getValue()
					.equals(((StringValue) attr.getValue(AlphaEnum.GROUP.getValue())).getValue())) {
				skmac.setAccessRightGranted(Boolean.TRUE);
			}
			if (skmac.getCryptoSetting() == null && (attr.containsKey(AlphaEnum.GROUP.getValue())
					&& attr.getValue(AlphaEnum.GROUP.getValue()) instanceof StringValue
					&& GroupEnum.CRYPTO_SETTING.getValue()
					.equals(((StringValue) attr.getValue(AlphaEnum.GROUP.getValue())).getValue()))) {
				CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, post.getMessage());
				skmac.setCryptoSetting(cryptoSetting);
			}
			if (skmac.getSingleKeyMixingRequest() == null && (attr.containsKey(AlphaEnum.GROUP.getValue())
					&& attr.getValue(AlphaEnum.GROUP.getValue()) instanceof StringValue
					&& GroupEnum.KEY_MIXING_REQUEST.getValue()
					.equals(((StringValue) attr.getValue(AlphaEnum.GROUP.getValue())).getValue()))) {
				SingleKeyMixingRequest singlekeyMixingRequest
						= JSONConverter.unmarshal(SingleKeyMixingRequest.class, post.getMessage());
				skmac.setSingleKeyMixingRequest(singlekeyMixingRequest);
			}

			run(actionContext);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		} catch (Exception ex) {
			Logger.getLogger(SingleKeyMixingAction.class.getName()).log(Level.SEVERE, null, ex);
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	protected SingleKeyMixingRequest retrieveSingleKeyMixingRequest(ActionContext actionContext)
			throws UnivoteException {
		ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForSingleKeyMixingRequestsForMixer(actionContext.getSection(),
						actionContext.getTenant())).getResult();
		if (result.getPost().isEmpty()) {
			throw new UnivoteException("Key mixing request not published yet.");
		}
		SingleKeyMixingRequest singleKeyMixingRequest
				= JSONConverter.unmarshal(SingleKeyMixingRequest.class, result.getPost().get(0).getMessage());
		return singleKeyMixingRequest;

	}

	private SingleKeyMixingResult createSingleKeyMixingResult(String tenant,
			SingleKeyMixingRequest singleKeyMixingRequest, CryptoSetting cryptoSetting,
			BigInteger alphaAsBigInt, BigInteger gMinusAsBigInt) {

		CryptoSetup cSetup = CryptoProvider.getSignatureSetup(cryptoSetting.getSignatureSetting());
		CyclicGroup cyclicGroup = cSetup.cryptoGroup;

		HashAlgorithm hashAlgorithm = HashAlgorithm.SHA256;

		Element alpha = cyclicGroup.getElementFrom(alphaAsBigInt);

		Element gMinus = cyclicGroup.getElementFrom(gMinusAsBigInt);

		String kString = singleKeyMixingRequest.getKey();
		Element kMinus = cyclicGroup.getElementFrom(kString);

		// b)
		Element k = kMinus.selfApply(alpha);

		Element g = gMinus.selfApply(alpha);

		// P R O O F
		//-----------
		// 0. Setup
		// Create sigma challenge generator
		// Create proof functions
		Function f1 = GeneratorFunction.getInstance(gMinus);
		Function f2 = GeneratorFunction.getInstance(kMinus);
		// Private and public input and prover id
		Element privateInput = alpha;
		Pair publicInput = Pair.getInstance(g, k);
		StringElement otherInput = StringMonoid.getInstance(Alphabet.UNICODE_BMP).getElement(tenant);
		HashMethod hashMethod = HashMethod.getInstance(hashAlgorithm);
		ConvertMethod convertMethod = ConvertMethod.getInstance(
				BigIntegerToByteArray.getInstance(ByteOrder.BIG_ENDIAN),
				StringToByteArray.getInstance(Charset.forName("UTF-8")));

		Converter converter = ByteArrayToBigInteger.getInstance(hashAlgorithm.getByteLength(), 1);

		SigmaChallengeGenerator challengeGenerator = FiatShamirSigmaChallengeGenerator.getInstance(
				cyclicGroup.getZModOrder(), otherInput, convertMethod, hashMethod, converter);
		EqualityPreimageProofSystem proofSystem = EqualityPreimageProofSystem.getInstance(challengeGenerator, f1, f2);
		// Generate and verify proof
		Triple proof = proofSystem.generate(privateInput, publicInput);

		SigmaProof proofDTO = new SigmaProof(proofSystem.getCommitment(proof).convertToString(),
				proofSystem.getChallenge(proof).convertToString(), proofSystem.getResponse(proof).convertToString());
		SingleKeyMixingResult result = new SingleKeyMixingResult(tenant, singleKeyMixingRequest.getPublicKey(),
				k.convertToString(), proofDTO);
		return result;
	}
}
