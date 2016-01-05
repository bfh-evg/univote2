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
package ch.bfh.univote2.ec.voteMix;

import ch.bfh.uniboard.clientlib.AttributeHelper;
import ch.bfh.uniboard.data.AttributesDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.common.message.Certificate;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.MixedVotes;
import ch.bfh.univote2.common.message.TrusteeCertificates;
import ch.bfh.univote2.common.message.ValidVotes;
import ch.bfh.univote2.common.message.VoteMixingRequest;
import ch.bfh.univote2.common.message.VoteMixingResult;
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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
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
		VoteMixingActionContext actionContext = new VoteMixingActionContext(ack);
		return actionContext;
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		try {
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForMixedVotes(actionContext.getSection()));
			return !result.getResult().getPost().isEmpty();
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not check post condition. UniBoard is not reachable. " + ex.getMessage());
			return false;
		}
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		VoteMixingActionContext ceksac = (VoteMixingActionContext) actionContext;
		try {
			this.retrieveValidVotes(ceksac);
		} catch (UnivoteException ex) {
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
					QueryFactory.getQueryForValidVotes(actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
			logger.log(Level.INFO, "Could not get valid encrypted votes.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Valid encrypted votes not yet published.");
		}
		try {
			this.retrieveMixers(ceksac);
		} catch (UnivoteException ex) {
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
					QueryFactory.getQueryForTrusteeCerts(actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
			logger.log(Level.INFO, "Could not get trustee certs.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Trustee certs not yet published.");
		}
		try {
			this.retrieveCryptoSetting(ceksac);
		} catch (UnivoteException ex) {
			//Add Notification
			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(
					QueryFactory.getQueryForCryptoSetting(actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
			logger.log(Level.INFO, "Could not get crypto setting.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Crypto setting not yet published.");
		}
		BoardPreconditionQuery bQuery = new BoardPreconditionQuery(QueryFactory.getQueryForVoteMixingResults(
				actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
		actionContext.getPreconditionQueries().add(bQuery);
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {

		if (actionContext instanceof VoteMixingActionContext) {
			VoteMixingActionContext vmac = (VoteMixingActionContext) actionContext;
			//Check ER
			if (vmac.getCurrentVotes().isEmpty()) {
				try {
					this.retrieveValidVotes(vmac);
				} catch (UnivoteException ex) {
					this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					return;
				}
			}
			//Mixer List
			if (vmac.getMixerKeys().isEmpty()) {
				try {
					this.retrieveMixers(vmac);
				} catch (UnivoteException ex) {
					this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					return;
				}
			}
			//Cryptosetting
			if (vmac.getCryptoSetting() == null) {
				try {
					this.retrieveCryptoSetting(vmac);
				} catch (UnivoteException ex) {
					this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					return;
				}
			}
			try {
				this.determineCurrentMixer(vmac);
			} catch (UnivoteException ex) {
				this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
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
		if (actionContext instanceof VoteMixingActionContext) {
			VoteMixingActionContext vmac = (VoteMixingActionContext) actionContext;
			if (notification instanceof PostDTO) {
				PostDTO post = (PostDTO) notification;
				AttributesDTO.AttributeDTO group
						= AttributeHelper.searchAttribute(post.getAlpha(), AlphaEnum.GROUP.getValue());
				String groupStr = ((StringValueDTO) group.getValue()).getValue();
				//Check Type (TC, E, MR)
				if (groupStr.equals(GroupEnum.TRUSTEE_CERTIFICATES.getValue())) {
					try {
						//TC: save mixers in context
						this.parseMixers(vmac, post);
						//Set intial mixer
						vmac.setCurrentMixer(vmac.getMixerOrder().get(0));
						//Check if we can start
						if (!vmac.getCurrentVotes().isEmpty() && vmac.getCryptoSetting() != null) {
							this.createMixingRequest(vmac);
						}
					} catch (UnivoteException ex) {
						this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
						this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					}
				} else if (groupStr.equals(GroupEnum.VALID_VOTES.getValue())) {
					try {
						//VC: save VC
						ValidVotes validVotes
								= JSONConverter.unmarshal(ValidVotes.class, post.getMessage());
						vmac.setCurrentVotes(validVotes.getValidVotes());
						//Check if we can start
						if (!vmac.getMixerKeys().isEmpty() && vmac.getCryptoSetting() != null) {
							this.createMixingRequest(vmac);
						}
					} catch (UnivoteException ex) {
						this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
						this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					}
				} else if (groupStr.equals(GroupEnum.CRYPTO_SETTING.getValue())) {
					try {
						//CS: save CS
						CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, post.getMessage());
						vmac.setCryptoSetting(cryptoSetting);
						//Check if we can start
						if (!vmac.getMixerKeys().isEmpty() && !vmac.getCurrentVotes().isEmpty()) {
							this.createMixingRequest(vmac);
						}
					} catch (UnivoteException ex) {
						this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
						this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					}
				} else if (groupStr.equals(GroupEnum.VOTE_MIXING_RESULT.getValue())) {
					try {
						//MR: create request for next or post mixed keys
						VoteMixingResult voteMixingResult
								= JSONConverter.unmarshal(VoteMixingResult.class, post.getMessage());

						this.validateMixingResult(vmac, voteMixingResult);
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

	protected void retrieveValidVotes(VoteMixingActionContext actionContext) throws UnivoteException {
		//Not yet implemented

	}

	protected void retrieveCryptoSetting(VoteMixingActionContext actionContext)
			throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForCryptoSetting(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Crypto setting not published yet.");
		}
		byte[] message = result.getResult().getPost().get(0).getMessage();
		CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, message);
		actionContext.setCryptoSetting(cryptoSetting);
	}

	protected void retrieveMixers(VoteMixingActionContext actionContext) throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForTrusteeCerts(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Trustees certificates not published yet.");
		}
		this.parseMixers(actionContext, result.getResult().getPost().get(0));
	}

	protected void parseMixers(VoteMixingActionContext actionContext, PostDTO post) throws UnivoteException {
		byte[] message = post.getMessage();
		TrusteeCertificates trusteeCertificates;
		trusteeCertificates = JSONConverter.unmarshal(TrusteeCertificates.class, message);
		List<Certificate> mixers = trusteeCertificates.getMixerCertificates();
		if (mixers == null || mixers.isEmpty()) {
			throw new UnivoteException("Invalid trustees certificates message. mixerCertificates is missing.");
		}
		boolean first = true;
		for (Certificate c
				: trusteeCertificates.getMixerCertificates()) {
			try {
				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
				InputStream in = new ByteArrayInputStream(c.getPem().getBytes());
				X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
				PublicKey pk = cert.getPublicKey();
				actionContext.getMixerKeys().put(c.getCommonName(), pk);
				actionContext.getMixerOrder().add(c.getCommonName());
				if (first) {
					actionContext.setCurrentMixer(c.getCommonName());
					first = false;
				}
			} catch (CertificateException ex) {
				throw new UnivoteException("Invalid trustees certificates message. Could not load pem.", ex);
			}
		}
	}

	protected void determineCurrentMixer(VoteMixingActionContext actionContext) throws UnivoteException {
		//Retrieve last KeyMixingRequest
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForLastKeyMixingRequest(actionContext.getSection()));
		//Not yet started
		if (result.getResult().getPost().isEmpty()) {
			actionContext.setCurrentMixer(actionContext.getMixerOrder().get(0));
			//Set generator
			Element generator = CryptoProvider.getSignatureSetup(actionContext.getCryptoSetting()
					.getSignatureSetting()).cryptoGenerator;
			this.createMixingRequest(actionContext);
		} else {
			//Try to retrieve a corresponding mixing result
			VoteMixingRequest voteMixingRequest = JSONConverter.unmarshal(VoteMixingRequest.class,
					result.getResult().getPost().get(0).getMessage());
			PublicKey pk = actionContext.getMixerKeys().get(voteMixingRequest.getMixerId());
			ResultContainerDTO result2 = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForVoteMixingResultForMixer(actionContext.getSection(), pk));
			if (result2.getResult().getPost().isEmpty()) {
				//Current Mixer has not published his/her result yet
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Waiting for mixing result of " + actionContext.getCurrentMixer());
				this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
			} else {
				//Validate mixing result and continue
				VoteMixingResult voteMixingResult = JSONConverter.unmarshal(VoteMixingResult.class,
						result2.getResult().getPost().get(0).getMessage());
				this.validateMixingResult(actionContext, voteMixingResult);
			}
		}
	}

	protected void createMixingRequest(VoteMixingActionContext actionContext) {
		VoteMixingRequest voteMixingRequest = new VoteMixingRequest();
		voteMixingRequest.setMixerId(actionContext.getCurrentMixer());
		voteMixingRequest.setVotesToMix(actionContext.getCurrentVotes());

		try {
			if (!actionContext.isGrantedARRequest()) {
				this.grantARforVoteMixingRequest(actionContext);
			}
			//post ac for voteMixingResult
			PublicKey pk = actionContext.getMixerKeys().get(actionContext.getCurrentMixer());
			byte[] arMessage = MessageFactory.createAccessRight(GroupEnum.VOTE_MIXING_RESULT, pk, 1);
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), arMessage, actionContext.getTenant());
			//post voteMixingRequest
			logger.log(Level.INFO, JSONConverter.marshal(voteMixingRequest));
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.VOTE_MIXING_REQUEST.getValue(),
					JSONConverter.marshal(voteMixingRequest).getBytes(Charset.forName("UTF-8")),
					actionContext.getTenant());
			//inform am
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Requested vote mixing from: " + actionContext.getCurrentMixer());
			this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}

	}

	protected void validateMixingResult(VoteMixingActionContext actionContext, VoteMixingResult mixingResult)
			throws UnivoteException {
		//TODO Verify proof
//		CyclicGroup cyclicGroup
//				= CryptoProvider.getSignatureSetup(actionContext.getCryptoSetting().getSignatureSetting());
//
//		Element currentG = cyclicGroup.getElementFrom(actionContext.getGenerator());
//		Element newG = cyclicGroup.getElementFrom(mixingResult.getGenerator());
//
//		Tuple vks = Tuple.getInstance();
//		for (String string : actionContext.getCurrentVotes()) {
//			vks = vks.add(cyclicGroup.getElementFrom(string));
//		}
//		Tuple shuffledVks = Tuple.getInstance();
//		for (String string : mixingResult.getMixedKeys()) {
//			shuffledVks = shuffledVks.add(cyclicGroup.getElementFrom(string));
//		}
//
//		Tuple permutationCommitment = Tuple.getInstance();
//		for (String string : mixingResult.getShuffleProof().getPermutationProof().getBridgingCommitments()) {
//			permutationCommitment = permutationCommitment.add(cyclicGroup.getElementFrom(string));
//		}
//
//		// 0. Setup
//		// Create sigma challenge generator
//		StringElement otherInput
//				= StringMonoid.getInstance(Alphabet.UNICODE_BMP).getElement(actionContext.getCurrentMixer());
//
//		HashMethod hashMethod = HashMethod.getInstance(
//				CryptoProvider.getHashAlgorithm(actionContext.getCryptoSetting().getHashSetting()));
//		ConvertMethod convertMethod = ConvertMethod.getInstance(
//				BigIntegerToByteArray.getInstance(ByteOrder.BIG_ENDIAN),
//				StringToByteArray.getInstance(Charset.forName("UTF-8")));
//
//		Converter converter = ByteArrayToBigInteger.getInstance(hashMethod.getHashAlgorithm().getByteLength(), 1);
//
//		SigmaChallengeGenerator challengeGenerator = FiatShamirSigmaChallengeGenerator.getInstance(
//				cyclicGroup.getZModOrder(), otherInput, convertMethod, hashMethod, converter);
//
//		// Create e-values challenge generator
//		ChallengeGenerator ecg = PermutationCommitmentProofSystem.createNonInteractiveEValuesGenerator(
//				cyclicGroup.getZModOrder(), vks.getArity());
//
//		Tuple publicInputShuffle = Tuple.getInstance(permutationCommitment, vks, shuffledVks, currentG, newG);
//		IdentityShuffleProofSystem spg
//				= IdentityShuffleProofSystem.getInstance(challengeGenerator, ecg, vks.getArity(), cyclicGroup);
		//if (spg.verify(publicInputShuffle, otherInput)) {
		if (true) {

			actionContext.setCurrentVotes(mixingResult.getMixedVotes());

			int i = actionContext.getMixerOrder().indexOf(actionContext.getCurrentMixer());
			//Check if last mixer
			if (i + 1 == actionContext.getMixerOrder().size()) {
				//post mixedVotes
				this.postMixedVotes(actionContext);
			} else {
				actionContext.setCurrentMixer(actionContext.getMixerOrder().get(i + 1));

				this.createMixingRequest(actionContext);
			}
		}
	}

	protected void postMixedVotes(VoteMixingActionContext actionContext) {
		try {
			MixedVotes mixedVotes = new MixedVotes();
			mixedVotes.setMixedVotes(actionContext.getCurrentVotes());
			//post ac for mixedVotes
			PublicKey pk = this.tenantManager.getPublicKey(actionContext.getTenant());
			byte[] arMessage = MessageFactory.createAccessRight(GroupEnum.MIXED_VOTES, pk);
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), arMessage, actionContext.getTenant());

			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.MIXED_VOTES.getValue(),
					JSONConverter.marshal(mixedVotes).getBytes(Charset.forName("UTF-8")),
					actionContext.getTenant());
			//inform tenant
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Posted mixed votes." + actionContext.getCurrentMixer());
			this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}

	}

	protected void grantARforVoteMixingRequest(VoteMixingActionContext actionContext) throws UnivoteException {
		//post ac for voteMixingRequest
		PublicKey pk = this.tenantManager.getPublicKey(actionContext.getTenant());
		byte[] arMessage = MessageFactory.createAccessRight(GroupEnum.VOTE_MIXING_REQUEST, pk);
		this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
				GroupEnum.ACCESS_RIGHT.getValue(), arMessage, actionContext.getTenant());
		actionContext.setGrantedARRequest(true);
	}

}
