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

import ch.bfh.uniboard.data.AttributesDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.TransformException;
import ch.bfh.uniboard.data.Transformer;
import ch.bfh.uniboard.service.data.Attributes;
import ch.bfh.unicrypt.crypto.mixer.classes.IdentityMixer;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.FiatShamirSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.ChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.classes.IdentityShuffleProofSystem;
import ch.bfh.unicrypt.crypto.proofsystem.classes.PermutationCommitmentProofSystem;
import ch.bfh.unicrypt.crypto.schemes.commitment.classes.PermutationCommitmentScheme;
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
import ch.bfh.unicrypt.math.algebra.general.classes.PermutationElement;
import ch.bfh.unicrypt.math.algebra.general.classes.Tuple;
import ch.bfh.unicrypt.math.algebra.general.interfaces.CyclicGroup;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
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
import ch.bfh.univote2.common.message.KeyMixingRequest;
import ch.bfh.univote2.common.message.KeyMixingResult;
import ch.bfh.univote2.common.message.MixProof;
import ch.bfh.univote2.common.message.PermutationProof;
import ch.bfh.univote2.common.message.ShuffleProof;
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
import java.security.PublicKey;
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
public class KeyMixingAction extends AbstractAction implements NotifiableAction {

	//See report.pdf (6.2.2.b)
	public static final String PERSISTENCE_NAME_FOR_ALPHA = "alpha";

	//This generator should not be stored here but taken from the board... However, this is a QUERY
	//unknown for the programmer so for now the simple (but wrong) way is choosen to store gMinus localy. (Sorry)
	public static final String PERSISTENCE_NAME_FOR_G_MINUS = "gMinus";

	private static final String ACTION_NAME = KeyMixingAction.class.getSimpleName();

	private static final Logger logger = Logger.getLogger(KeyMixingAction.class.getName());

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
		return new KeyMixingActionContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		try {
			PublicKey publicKey = tenantManager.getPublicKey(actionContext.getTenant());
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForKeyMixingResultForMixer(actionContext.getSection(), publicKey));
			if (!result.getResult().isEmpty()) {
				return true;
			}
		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not request key mixing result.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not check post condition.");
			return false;
		}
		return false;
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		KeyMixingActionContext kmac = (KeyMixingActionContext) actionContext;
		TrusteeActionHelper.checkAndSetCryptoSetting(kmac, uniboardService, tenantManager, informationService, logger);
		TrusteeActionHelper.checkAndSetAccsessRight(kmac, GroupEnum.KEY_MIXING_RESULT, uniboardService,
				tenantManager, informationService, logger);
		this.checkAndSetKeyMixingRequest(kmac);
	}

	protected void checkAndSetKeyMixingRequest(KeyMixingActionContext actionContext) {
		ActionContextKey actionContextKey = actionContext.getActionContextKey();
		String section = actionContext.getSection();
		try {
			KeyMixingRequest keyMixingRequest = actionContext.getKeyMixingRequest();

			//Add Notification
			if (keyMixingRequest == null) {
				keyMixingRequest = retrieveKeyMixingRequest(actionContext);
				actionContext.setKeyMixingRequest(keyMixingRequest);
			}

		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not get key mixing request.", ex);
			informationService.informTenant(actionContextKey, ex.getMessage());
		}
		try {
			if (actionContext.getKeyMixingRequest() == null) {
				//Add Notification
				BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
						QueryFactory.getQueryForKeyMixingRequestForMixer(section, actionContext.getTenant()),
						BoardsEnum.UNIVOTE.getValue());
				actionContext.getPreconditionQueries().add(bQuery);
			}
		} catch (UnivoteException exception) {
			logger.log(Level.WARNING, "Could not get tenant for key mixing request.", exception);
			informationService.informTenant(actionContextKey, exception.getMessage());
		}

	}

	protected KeyMixingRequest retrieveKeyMixingRequest(ActionContext actionContext) throws UnivoteException {
		List<PostDTO> result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForKeyMixingRequestForMixer(actionContext.getSection(),
						actionContext.getTenant())).getResult();
		if (result.isEmpty()) {
			throw new UnivoteException("Key mixing request not published yet.");

		}
		KeyMixingRequest keyMixingRequest = JSONConverter.unmarshal(KeyMixingRequest.class,
				result.get(0).getMessage());
		return keyMixingRequest;

	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		if (!(actionContext instanceof KeyMixingActionContext)) {
			this.informationService.informTenant(actionContext.getActionContextKey(), "Invalid context.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		KeyMixingActionContext kmac = (KeyMixingActionContext) actionContext;
		if (!kmac.getAccessRightGranted()) {
			TrusteeActionHelper.checkAndSetAccsessRight(kmac, GroupEnum.KEY_MIXING_RESULT, uniboardService,
					tenantManager, informationService, logger);
			if (!kmac.getAccessRightGranted()) {
				logger.log(Level.INFO, "Access right has not been granted yet.");
				this.informationService.informTenant(kmac.getActionContextKey(), "Access right not yet granted.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
		}
		if (kmac.getCryptoSetting() == null) {
			try {
				kmac.setCryptoSetting(TrusteeActionHelper.retrieveCryptoSetting(actionContext, uniboardService));
			} catch (UnivoteException ex) {
				logger.log(Level.WARNING, "Crypto setting is not published yet.", ex);
				this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
		}
		if (kmac.getKeyMixingRequest() == null) {
			try {
				kmac.setKeyMixingRequest(this.retrieveKeyMixingRequest(actionContext));
			} catch (UnivoteException ex) {
				logger.log(Level.WARNING, "Key mixing request is not published yet.", ex);
				this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
		}
		this.informationService.informTenant(actionContext.getActionContextKey(), "Preconditions fullfilled.");
		String tenant = actionContext.getTenant();
		String section = actionContext.getSection();

		KeyMixingRequest keyMixingRequest = kmac.getKeyMixingRequest();
		CryptoSetting cryptoSetting = kmac.getCryptoSetting();

		try {
			BigInteger alpha = null;
			try {
				alpha = securePersistenceService.retrieve(tenant, section, PERSISTENCE_NAME_FOR_ALPHA);
			} catch (UnivoteException ex) {
				//No exponent available so a new one will be built
			}

			EnhancedKeyMixingResult enhancedKeyMixingResult = createKeyMixingResult(tenant, keyMixingRequest,
					cryptoSetting, alpha);
			alpha = enhancedKeyMixingResult.alpha;
			BigInteger gMinus = enhancedKeyMixingResult.gMinus;
			KeyMixingResult keyMixingResult = enhancedKeyMixingResult.keyMixingResult;
			String keyMixingResultString = JSONConverter.marshal(keyMixingResult);
			byte[] keyMixingResultByteArray = keyMixingResultString.getBytes(Charset.forName("UTF-8"));

			securePersistenceService.persist(tenant, section, PERSISTENCE_NAME_FOR_ALPHA, alpha);
			securePersistenceService.persist(tenant, section, PERSISTENCE_NAME_FOR_G_MINUS, gMinus);

			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), section, GroupEnum.KEY_MIXING_RESULT.getValue(),
					keyMixingResultByteArray, tenant);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Posted key mixing result. Action finished.");
			this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);

		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not post key mixing result. " + ex.getMessage());
			Logger.getLogger(KeyMixingAction.class.getName()).log(Level.SEVERE, null, ex);
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	@Override
	@Asynchronous
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (!(actionContext instanceof KeyMixingActionContext)) {
			this.informationService.informTenant(actionContext.getActionContextKey(), "Invalid context.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		KeyMixingActionContext kmac = (KeyMixingActionContext) actionContext;

		this.informationService.informTenant(actionContext.getActionContextKey(), "Notified.");

		if (!(notification instanceof PostDTO)) {
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		PostDTO post = (PostDTO) notification;
		try {
			Attributes attr = Transformer.convertAttributesDTOtoAttributes(new AttributesDTO(post.getAlpha()));
			attr.containsKey(AlphaEnum.GROUP.getValue());

			if (attr.containsKey(AlphaEnum.GROUP.getValue())
					&& attr.getAttribute(AlphaEnum.GROUP.getValue()).getValue().equals(
					GroupEnum.ACCESS_RIGHT.getValue())) {
				kmac.setAccessRightGranted(true);
			} else if (kmac.getCryptoSetting() == null && attr.getAttribute(AlphaEnum.GROUP.getValue()).getValue()
					.equals(GroupEnum.CRYPTO_SETTING.getValue())) {
				CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, post.getMessage());
				kmac.setCryptoSetting(cryptoSetting);
			} else if (kmac.getKeyMixingRequest() == null && attr.getAttribute(AlphaEnum.GROUP.getValue()).getValue()
					.equals(GroupEnum.KEY_MIXING_REQUEST.getValue())) {
				KeyMixingRequest keyMixingRequest = JSONConverter.unmarshal(KeyMixingRequest.class, post.getMessage());
				kmac.setKeyMixingRequest(keyMixingRequest);
			}

			run(actionContext);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		} catch (TransformException ex) {
			Logger.getLogger(KeyMixingAction.class.getName()).log(Level.SEVERE, null, ex);
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	private EnhancedKeyMixingResult createKeyMixingResult(String tenant, KeyMixingRequest keyMixingRequest,
			CryptoSetting cryptoSetting, BigInteger alphaAsBigInt) {
		CryptoSetup cSetup = CryptoProvider.getSignatureSetup(cryptoSetting.getSignatureSetting());
		CyclicGroup cyclicGroup = cSetup.cryptoGroup;

		// d)
		Element alpha;
		if (alphaAsBigInt != null) {
			alpha = cyclicGroup.getZModOrder().getElementFrom(alphaAsBigInt);
		} else {
			alpha = cyclicGroup.getZModOrder().getRandomElement();
		}

		// e)
		Element gMinus = cyclicGroup.getElementFrom(keyMixingRequest.getGenerator());
		Element g = gMinus.selfApply(alpha);

		List<String> vkString = keyMixingRequest.getKeys();
		Tuple vks = Tuple.getInstance();
		for (String string : vkString) {
			logger.log(Level.INFO, string);
			vks = vks.add(cyclicGroup.getElementFrom(string));
		}

		// Create mixer and shuffle
		IdentityMixer mixer = IdentityMixer.getInstance(cyclicGroup, vks.getArity());

		// f) Create psi
		PermutationElement psi = mixer.getPermutationGroup().getRandomElement();

		// Perfom shuffle
		Tuple shuffledVks = mixer.shuffle(vks, psi, alpha);

		// P R O O F
		//-----------
		// 0. Setup
		// Create sigma challenge generator
		StringElement otherInput = StringMonoid.getInstance(Alphabet.UNICODE_BMP).getElement(tenant);
		HashMethod hashMethod = HashMethod.getInstance(HashAlgorithm.SHA256);
		ConvertMethod convertMethod = ConvertMethod.getInstance(
				BigIntegerToByteArray.getInstance(ByteOrder.BIG_ENDIAN),
				StringToByteArray.getInstance(Charset.forName("UTF-8")));

		Converter converter = ByteArrayToBigInteger.getInstance(HashAlgorithm.SHA256.getByteLength(), 1);

		SigmaChallengeGenerator challengeGenerator = FiatShamirSigmaChallengeGenerator.getInstance(
				cyclicGroup.getZModOrder(), otherInput, convertMethod, hashMethod, converter);

		// Create e-values challenge generator
		ChallengeGenerator ecg = PermutationCommitmentProofSystem.createNonInteractiveEValuesGenerator(
				cyclicGroup.getZModOrder(), vks.getArity());

		// 1. Permutation Proof
		//----------------------
		// Create psi commitment
		PermutationCommitmentScheme pcs = PermutationCommitmentScheme.getInstance(cyclicGroup, vks.getArity());
		Tuple permutationCommitmentRandomizations = pcs.getRandomizationSpace().getRandomElement();
		Tuple permutationCommitment = pcs.commit(psi, permutationCommitmentRandomizations);

		// Create psi commitment proof system
		PermutationCommitmentProofSystem pcps = PermutationCommitmentProofSystem.getInstance(challengeGenerator,
				ecg, cyclicGroup, vks.getArity());

		// Create psi commitment proof
		Pair privateInputPermutation = Pair.getInstance(psi, permutationCommitmentRandomizations);
		Element publicInputPermutation = permutationCommitment;
		Tuple permutationProof = pcps.generate(privateInputPermutation, publicInputPermutation);

		// 2. Shuffle Proof
		//------------------
		// Create shuffle proof system
		IdentityShuffleProofSystem spg = IdentityShuffleProofSystem.getInstance(challengeGenerator, ecg,
				vks.getArity(), cyclicGroup);

		// Proof and verify
		Tuple privateInputShuffle = Tuple.getInstance(psi, permutationCommitmentRandomizations, alpha);
		Tuple publicInputShuffle = Tuple.getInstance(permutationCommitment, vks, shuffledVks, gMinus, g);

		// Create shuffle proof
		Tuple mixProof = spg.generate(privateInputShuffle, publicInputShuffle);

		EnhancedKeyMixingResult result = new EnhancedKeyMixingResult();
		result.alpha = alpha.convertToBigInteger();
		result.gMinus = gMinus.convertToBigInteger();

		List<String> shuffledVKsAsStrings = new ArrayList<>();
		for (Element shuffledVK : shuffledVks) {
			shuffledVKsAsStrings.add(shuffledVK.convertToString());
		}

		PermutationProof permutationProofDTO = new PermutationProof();
		permutationProofDTO.setChallenge(pcps.getChallenge(permutationProof).convertToString());
		permutationProofDTO.setCommitment(pcps.getCommitment(permutationProof).convertToString());
		permutationProofDTO.setResponse(pcps.getResponse(permutationProof).convertToString());
		{
			List<String> bridgingCommitmentsAsStrings = new ArrayList<>();

			for (Element bridgingCommitment : ((Tuple) pcps.getBridingCommitment(permutationProof)).getSequence()) {
				bridgingCommitmentsAsStrings.add(bridgingCommitment.convertToString());
			}
			permutationProofDTO.setBridgingCommitments(bridgingCommitmentsAsStrings);
		}
		{
			List<String> eValuesAsStrings = new ArrayList<>();

			for (Element eValue : ((Tuple) pcps.getEValues(permutationProof)).getSequence()) {
				eValuesAsStrings.add(eValue.convertToString());
			}
			permutationProofDTO.seteValues(eValuesAsStrings);
		}

		MixProof mixProofDTO = new MixProof();
		mixProofDTO.setChallenge(spg.getChallenge(mixProof).convertToString());
		mixProofDTO.setCommitment(spg.getCommitment(mixProof).convertToString());
		mixProofDTO.setResponse(spg.getResponse(mixProof).convertToString());
		{
			List<String> eValuesAsStrings = new ArrayList<>();

			for (Element eValue : ((Tuple) spg.getEValues(mixProof)).getSequence()) {
				eValuesAsStrings.add(eValue.convertToString());
			}
			mixProofDTO.seteValues(eValuesAsStrings);
		}

		ShuffleProof shuffleProofDTO = new ShuffleProof();
		shuffleProofDTO.setMixProof(mixProofDTO);
		shuffleProofDTO.setPermutationProof(permutationProofDTO);

		KeyMixingResult keyMixingResult = new KeyMixingResult(shuffledVKsAsStrings, g.convertToString(),
				shuffleProofDTO);
		result.keyMixingResult = keyMixingResult;
		return result;
	}

	protected class EnhancedKeyMixingResult {

		protected KeyMixingResult keyMixingResult;
		protected BigInteger alpha;
		protected BigInteger gMinus;
	}

}
