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
package ch.bfh.univote2.trustee.mixer.voteMixing;

import ch.bfh.uniboard.data.AttributesDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.Transformer;
import ch.bfh.uniboard.service.data.Attributes;
import ch.bfh.unicrypt.crypto.mixer.classes.ReEncryptionMixer;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.classes.FiatShamirSigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.ChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.challengegenerator.interfaces.SigmaChallengeGenerator;
import ch.bfh.unicrypt.crypto.proofsystem.classes.PermutationCommitmentProofSystem;
import ch.bfh.unicrypt.crypto.proofsystem.classes.ReEncryptionShuffleProofSystem;
import ch.bfh.unicrypt.crypto.schemes.commitment.classes.PermutationCommitmentScheme;
import ch.bfh.unicrypt.crypto.schemes.encryption.classes.ElGamalEncryptionScheme;
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
import ch.bfh.univote2.common.message.EncryptedVote;
import ch.bfh.univote2.common.message.EncryptionKey;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.MixProof;
import ch.bfh.univote2.common.message.PermutationProof;
import ch.bfh.univote2.common.message.ShuffleProof;
import ch.bfh.univote2.common.message.VoteMixingRequest;
import ch.bfh.univote2.common.message.VoteMixingResult;
import ch.bfh.univote2.common.query.AlphaEnum;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.QueryFactory;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.trustee.BoardsEnum;
import ch.bfh.univote2.trustee.TrusteeActionHelper;
import ch.bfh.univote2.trustee.mixer.keyMixing.KeyMixingAction;
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
public class VoteMixingAction extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = VoteMixingAction.class.getSimpleName();

	private static final Logger logger = Logger.getLogger(VoteMixingAction.class.getName());

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
		return new VoteMixingActionContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		if (!(actionContext instanceof VoteMixingActionContext)) {
			logger.log(Level.SEVERE, "The actionContext was not the expected one.");
			return false;
		}
		VoteMixingActionContext vmac = (VoteMixingActionContext) actionContext;
		try {
			PublicKey publicKey = tenantManager.getPublicKey(actionContext.getTenant());
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForVoteMixingResultForMixer(actionContext.getSection(), publicKey));
			if (!result.getResult().isEmpty()) {
				return true;
			}
		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not request vote mixing result.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not check post condition.");
			return false;
		}
		return false;
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		if (!(actionContext instanceof VoteMixingActionContext)) {
			logger.log(Level.SEVERE, "The actionContext was not the expected one.");
			return;
		}
		VoteMixingActionContext vmac = (VoteMixingActionContext) actionContext;
		TrusteeActionHelper.checkAndSetCryptoSetting(vmac, uniboardService, tenantManager, informationService, logger);
		TrusteeActionHelper.checkAndSetAccsessRight(vmac, GroupEnum.VOTE_MIXING_RESULT, uniboardService, tenantManager,
				informationService, logger);
		TrusteeActionHelper.checkAndSetEncryptionKey(vmac, uniboardService, informationService, logger);
		this.checkAndSetVoteMixingRequest(vmac);
	}

	protected VoteMixingRequest retrieveVoteMixingRequest(ActionContext actionContext) throws UnivoteException {
		List<PostDTO> result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForVoteMixingRequestForMixer(actionContext.getSection(),
						actionContext.getTenant())).getResult();
		if (result.isEmpty()) {
			throw new UnivoteException("key mixing request not published yet.");

		}
		VoteMixingRequest voteMixingRequest
				= JSONConverter.unmarshal(VoteMixingRequest.class, result.get(0).getMessage());
		return voteMixingRequest;

	}

	protected void checkAndSetVoteMixingRequest(VoteMixingActionContext actionContext) {
		ActionContextKey actionContextKey = actionContext.getActionContextKey();
		String section = actionContext.getSection();
		String tenant = actionContext.getTenant();
		try {
			VoteMixingRequest voteMixingRequest = actionContext.getVoteMixingRequest();

			//Add Notification
			if (voteMixingRequest == null) {
				voteMixingRequest = retrieveVoteMixingRequest(actionContext);
				actionContext.setVoteMixingRequest(voteMixingRequest);
			}

		} catch (UnivoteException ex) {
			logger.log(Level.WARNING, "Could not get vote mixing request.", ex);
			this.informationService.informTenant(actionContextKey,
					"Error retrieving vote mixing request: " + ex.getMessage());
		}
		try {
			if (actionContext.getVoteMixingRequest() == null) {
				//Add Notification
				BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
						QueryFactory.getQueryForVoteMixingRequestForMixer(section, tenant),
						BoardsEnum.UNIVOTE.getValue());
				actionContext.getPreconditionQueries().add(bQuery);
			}
		} catch (UnivoteException exception) {
			logger.log(Level.WARNING, "Could not get tenant for vote mixing request.", exception);
			this.informationService.informTenant(actionContextKey,
					"Error retrieving tenant for vote mixing request: " + exception.getMessage());
		}

	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		if (!(actionContext instanceof VoteMixingActionContext)) {
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		VoteMixingActionContext skcac = (VoteMixingActionContext) actionContext;
		if (!skcac.getAccessRightGranted()) {
			TrusteeActionHelper.checkAndSetAccsessRight(skcac, GroupEnum.VOTE_MIXING_RESULT, uniboardService,
					tenantManager, informationService, logger);
			if (!skcac.getAccessRightGranted()) {
				logger.log(Level.INFO, "Access right has not been granted yet.");
				this.informationService.informTenant(skcac.getActionContextKey(), "Access right not yet granted.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
		}
		if (skcac.getCryptoSetting() == null) {
			try {
				skcac.setCryptoSetting(TrusteeActionHelper.retrieveCryptoSetting(actionContext, uniboardService));
			} catch (UnivoteException ex) {
				logger.log(Level.WARNING, "Crypto setting is not published yet.", ex);
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Crypto setting not published yet.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
		}
		if (skcac.getVoteMixingRequest() == null) {
			try {
				skcac.setVoteMixingRequest(this.retrieveVoteMixingRequest(actionContext));
			} catch (UnivoteException ex) {
				logger.log(Level.WARNING, "Vote mixing request is not published yet.", ex);
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Vote mixing request not published yet.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
		}
		if (skcac.getEncryptionKey() == null) {
			try {
				skcac.setEncryptionKey(TrusteeActionHelper.retrieveEncryptionKey(actionContext, uniboardService));
			} catch (UnivoteException ex) {
				logger.log(Level.WARNING, "Encryption key is not published yet.", ex);
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Encryption key not published yet.");
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				return;
			}
		}

		String tenant = actionContext.getTenant();
		try {
			String encryptionKeyAsString = skcac.getEncryptionKey().getEncryptionKey();
			VoteMixingResult voteMixingResult = createVoteMixingResult(tenant, skcac.getVoteMixingRequest(),
					skcac.getCryptoSetting(), encryptionKeyAsString);
			String voteMixingResultString = JSONConverter.marshal(voteMixingResult);
			byte[] voteMixingResultByteArray = voteMixingResultString.getBytes(Charset.forName("UTF-8"));

			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.VOTE_MIXING_RESULT.getValue(), voteMixingResultByteArray, tenant);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Posted vote mixing result. Action finished.");
			this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);

		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not post vote mixing result. Action failed.");
			Logger.getLogger(KeyMixingAction.class.getName()).log(Level.SEVERE, null, ex);
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		} catch (Exception ex) {
			Logger.getLogger(KeyMixingAction.class.getName()).log(Level.SEVERE, null, ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not marshal vote mixing result. Action failed.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	@Override
	@Asynchronous
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (!(actionContext instanceof VoteMixingActionContext)) {
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			return;
		}
		VoteMixingActionContext vmac = (VoteMixingActionContext) actionContext;

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
				vmac.setAccessRightGranted(Boolean.TRUE);
			}
			if (vmac.getCryptoSetting() == null && attr.getAttribute(AlphaEnum.GROUP.getValue()).getValue().equals(
					GroupEnum.CRYPTO_SETTING.getValue())) {
				CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, post.getMessage());
				vmac.setCryptoSetting(cryptoSetting);
			}
			if (vmac.getVoteMixingRequest() == null && attr.getAttribute(AlphaEnum.GROUP.getValue()).getValue().equals(
					GroupEnum.VOTE_MIXING_REQUEST.getValue())) {
				VoteMixingRequest voteMixingRequest = JSONConverter.unmarshal(VoteMixingRequest.class,
						post.getMessage());
				vmac.setVoteMixingRequest(voteMixingRequest);
			}
			if (vmac.getVoteMixingRequest() == null && attr.getAttribute(AlphaEnum.GROUP.getValue()).getValue().equals(
					GroupEnum.ENCRYPTION_KEY.getValue())) {
				EncryptionKey encryptionKey = JSONConverter.unmarshal(EncryptionKey.class, post.getMessage());
				vmac.setEncryptionKey(encryptionKey);
			}

			run(actionContext);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		} catch (Exception ex) {
			Logger.getLogger(VoteMixingAction.class.getName()).log(Level.SEVERE, null, ex);
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	private VoteMixingResult createVoteMixingResult(String tenant, VoteMixingRequest voteMixingRequest,
			CryptoSetting cryptoSetting, String encryptionKeyAsString) {
		CryptoSetup cSetup = CryptoProvider.getEncryptionSetup(cryptoSetting.getEncryptionSetting());
		CyclicGroup cyclicGroup = cSetup.cryptoGroup;
		Element encryptionGenerator = cSetup.cryptoGenerator;
		Element encryptionKey = cyclicGroup.getElementFrom(encryptionKeyAsString);
		List<EncryptedVote> vString = voteMixingRequest.getVotesToMix();
		Tuple vs = Tuple.getInstance();
		for (EncryptedVote vote : vString) {
			vs = vs.add(Pair.getInstance(cyclicGroup.getElementFrom(vote.getFirstValue()), cyclicGroup.getElementFrom(vote.getSecondValue())));
		}

		ElGamalEncryptionScheme elGamal = ElGamalEncryptionScheme.getInstance(encryptionGenerator);
		// Create mixer and shuffle
		ReEncryptionMixer mixer = ReEncryptionMixer.getInstance(elGamal, encryptionKey, vs.getArity());

		// f) Create psi
		PermutationElement psi = mixer.getPermutationGroup().getRandomElement();

		Tuple rs = mixer.generateRandomizations();

		// Perfom shuffle
		Tuple shuffledVs = mixer.shuffle(vs, psi, rs);

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
				cyclicGroup.getZModOrder(), vs.getArity());

		// 1. Permutation Proof
		//----------------------
		// Create psi commitment
		PermutationCommitmentScheme pcs = PermutationCommitmentScheme.getInstance(cyclicGroup, vs.getArity());
		Tuple permutationCommitmentRandomizations = pcs.getRandomizationSpace().getRandomElement();
		Tuple permutationCommitment = pcs.commit(psi, permutationCommitmentRandomizations);

		// Create psi commitment proof system
		PermutationCommitmentProofSystem pcps = PermutationCommitmentProofSystem.getInstance(challengeGenerator, ecg,
				cyclicGroup, vs.getArity());

		// Create psi commitment proof
		Pair privateInputPermutation = Pair.getInstance(psi, permutationCommitmentRandomizations);
		Element publicInputPermutation = permutationCommitment;
		Tuple permutationProof = pcps.generate(privateInputPermutation, publicInputPermutation);

		// 2. Shuffle Proof
		//------------------
		// Create shuffle proof system
		ReEncryptionShuffleProofSystem spg = ReEncryptionShuffleProofSystem.getInstance(challengeGenerator, ecg, vs.getArity(), elGamal, encryptionKey);

		// Proof and verify
		Tuple privateInputShuffle = Tuple.getInstance(psi, permutationCommitmentRandomizations, rs);
		Tuple publicInputShuffle = Tuple.getInstance(permutationCommitment, vs, shuffledVs);

		// Create shuffle proof
		Tuple mixProof = spg.generate(privateInputShuffle, publicInputShuffle);

		List<EncryptedVote> shuffledVsAsEncryptedVote = new ArrayList<>();
		for (Element shuffledV : shuffledVs) {
			Pair encVote = (Pair) shuffledV;
			shuffledVsAsEncryptedVote.add(new EncryptedVote(encVote.getFirst().convertToString(), encVote.getSecond().convertToString()));
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

		VoteMixingResult result = new VoteMixingResult(shuffledVsAsEncryptedVote, shuffleProofDTO);
		return result;
	}

}
