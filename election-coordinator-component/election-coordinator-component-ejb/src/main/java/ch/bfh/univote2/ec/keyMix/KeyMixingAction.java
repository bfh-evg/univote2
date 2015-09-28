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

import ch.bfh.uniboard.clientlib.AttributeHelper;
import ch.bfh.uniboard.data.AttributesDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.unicrypt.helper.math.MathUtil;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.component.core.action.AbstractAction;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.common.message.Certificate;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.ElectoralRoll;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.KeyMixingRequest;
import ch.bfh.univote2.common.message.KeyMixingResult;
import ch.bfh.univote2.common.message.MixedKeys;
import ch.bfh.univote2.common.message.TrusteeCertificates;
import ch.bfh.univote2.common.query.AlphaEnum;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
import ch.bfh.univote2.common.query.MessageFactory;
import ch.bfh.univote2.common.query.QueryFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
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

	private static final String ACTION_NAME = KeyMixingAction.class.getSimpleName();
	private static final Logger logger = Logger.getLogger(KeyMixingAction.class.getName());

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
					"Could not check post condition. UniBoard is not reachable. " + ex.getMessage());
			return false;
		}
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		KeyMixingActionContext ceksac = (KeyMixingActionContext) actionContext;
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
		BoardPreconditionQuery bQuery = new BoardPreconditionQuery(QueryFactory.getQueryForKeyMixingResults(
				actionContext.getSection()), BoardsEnum.UNIVOTE.getValue());
		actionContext.getPreconditionQueries().add(bQuery);
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		this.informationService.informTenant(actionContext.getActionContextKey(), "Running.");
		if (actionContext instanceof KeyMixingActionContext) {
			KeyMixingActionContext kmac = (KeyMixingActionContext) actionContext;
			//Check ER
			if (kmac.getCurrentKeys().isEmpty()) {
				try {
					this.retrieveElectoralRoll(kmac);
				} catch (UnivoteException ex) {
					this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					return;
				}
			}
			//Mixer List
			if (kmac.getMixerKeys().isEmpty()) {
				try {
					this.retrieveMixers(kmac);
				} catch (UnivoteException ex) {
					this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					return;
				}
			}
			//Cryptosetting
			if (kmac.getCryptoSetting() == null) {
				try {
					this.retrieveCryptoSetting(kmac);
				} catch (UnivoteException ex) {
					this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					return;
				}
			}
			try {
				this.determineCurrentMixer(kmac);
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
		if (actionContext instanceof KeyMixingActionContext) {
			KeyMixingActionContext kmac = (KeyMixingActionContext) actionContext;
			if (notification instanceof PostDTO) {
				PostDTO post = (PostDTO) notification;
				AttributesDTO.AttributeDTO group
						= AttributeHelper.searchAttribute(post.getAlpha(), AlphaEnum.GROUP.getValue());
				String groupStr = ((StringValueDTO) group.getValue()).getValue();
				//Check Type (TC, ER, MR)
				if (groupStr.equals(GroupEnum.TRUSTEE_CERTIFICATES.getValue())) {
					try {
						//TC: save mixers in context
						this.parseMixers(kmac, post);
						//Set intial mixer
						kmac.setCurrentMixer(kmac.getMixerOrder().get(0));
						//Check if we can start
						if (!kmac.getCurrentKeys().isEmpty() && kmac.getCryptoSetting() != null) {
							this.createMixingRequest(kmac);
						}
					} catch (UnivoteException ex) {
						this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
						this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					}
				} else if (groupStr.equals(GroupEnum.ELECTORAL_ROLL.getValue())) {
					try {
						//ER: save ER
						ElectoralRoll electoralRoll = JSONConverter.unmarshal(ElectoralRoll.class, post.getMessage());
						this.retrieveInitialKeys(kmac, electoralRoll);
						//Check if we can start
						if (!kmac.getMixerKeys().isEmpty() && kmac.getCryptoSetting() != null) {
							this.createMixingRequest(kmac);
						}
					} catch (UnivoteException ex) {
						this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
						this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					}
				} else if (groupStr.equals(GroupEnum.CRYPTO_SETTING.getValue())) {
					try {
						//CS: save CS
						CryptoSetting cryptoSetting = JSONConverter.unmarshal(CryptoSetting.class, post.getMessage());
						kmac.setCryptoSetting(cryptoSetting);
						//Set generator
						Element generator = CryptoProvider.getSignatureSetup(kmac.getCryptoSetting()
								.getSignatureSetting()).cryptoGenerator;
						kmac.setGenerator(generator.convertToString());
						//Check if we can start
						if (!kmac.getMixerKeys().isEmpty() && !kmac.getMixerKeys().isEmpty()) {
							this.createMixingRequest(kmac);
						}
					} catch (UnivoteException ex) {
						this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
						this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
					}
				} else if (groupStr.equals(GroupEnum.KEY_MIXING_RESULT.getValue())) {
					try {
						//MR: create request for next or post mixed keys
						KeyMixingResult keyMixingResult
								= JSONConverter.unmarshal(KeyMixingResult.class, post.getMessage());

						this.validateMixingResult(kmac, keyMixingResult);
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

	protected void retrieveElectoralRoll(KeyMixingActionContext actionContext) throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForElectoralRoll(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Electoral roll not published yet.");
		}
		byte[] message = result.getResult().getPost().get(0).getMessage();
		ElectoralRoll electoralRoll = JSONConverter.unmarshal(ElectoralRoll.class, message);
		this.retrieveInitialKeys(actionContext, electoralRoll);
	}

	protected void retrieveInitialKeys(KeyMixingActionContext actionContext, ElectoralRoll electoralRoll) {
		for (String voterId : electoralRoll.getVoterIds()) {
			try {
				ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNICERT.getValue(),
						QueryFactory.getQueryFormUniCertForVoterCert(voterId));
				if (!result.getResult().getPost().isEmpty()) {
					PostDTO post = result.getResult().getPost().get(0);
					Certificate certi = JSONConverter.unmarshal(Certificate.class, post.getMessage());
					String pem = certi.getPem();

					CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
					InputStream in = new ByteArrayInputStream(pem.getBytes());
					X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
					PublicKey pk = cert.getPublicKey();
					actionContext.getCurrentKeys().add(this.computePublicKeyString(pk));
				} else {
					logger.log(Level.FINE, "No certificate available for: {0}", voterId);
				}
			} catch (UnivoteException | CertificateException ex) {
				this.informationService.informTenant(actionContext.getActionContextKey(),
						"Could not request voter certificate: " + voterId);
				logger.log(Level.INFO, ex.getMessage());
			}

		}
	}

	protected void retrieveCryptoSetting(KeyMixingActionContext actionContext)
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

	protected void retrieveMixers(KeyMixingActionContext actionContext) throws UnivoteException {
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForTrusteeCerts(actionContext.getSection()));
		if (result.getResult().getPost().isEmpty()) {
			throw new UnivoteException("Trustees certificates not published yet.");
		}
		this.parseMixers(actionContext, result.getResult().getPost().get(0));
	}

	protected void parseMixers(KeyMixingActionContext actionContext, PostDTO post) throws UnivoteException {
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

	protected void determineCurrentMixer(KeyMixingActionContext actionContext) throws UnivoteException {
		//Retrieve last KeyMixingRequest
		ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForLastKeyMixingRequest(actionContext.getSection()));
		//Not yet started
		if (result.getResult().getPost().isEmpty()) {
			actionContext.setCurrentMixer(actionContext.getMixerOrder().get(0));
			//Set generator
			Element generator = CryptoProvider.getSignatureSetup(actionContext.getCryptoSetting()
					.getSignatureSetting()).cryptoGenerator;
			actionContext.setGenerator(generator.convertToString());
			this.createMixingRequest(actionContext);
		}
		//Try to retrieve a corresponding mixing result
		KeyMixingRequest keyMixingRequest = JSONConverter.unmarshal(KeyMixingRequest.class,
				result.getResult().getPost().get(0).getMessage());
		PublicKey pk = actionContext.getMixerKeys().get(keyMixingRequest.getMixerId());
		ResultContainerDTO result2 = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForKeyMixingResultForMixer(actionContext.getSection(), pk));
		if (result2.getResult().getPost().isEmpty()) {
			//Current Mixer has not published his/her result yet
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Waiting for mixing result of " + actionContext.getCurrentMixer());
			this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
			return;
		}
		//Validate mixing result and continue
		KeyMixingResult keyMixingResult = JSONConverter.unmarshal(KeyMixingResult.class,
				result2.getResult().getPost().get(0).getMessage());
		this.validateMixingResult(actionContext, keyMixingResult);
	}

	protected void createMixingRequest(KeyMixingActionContext actionContext) {
		KeyMixingRequest keyMixingRequest = new KeyMixingRequest();
		keyMixingRequest.setMixerId(actionContext.getCurrentMixer());
		keyMixingRequest.setGenerator(actionContext.getGenerator());
		keyMixingRequest.setKeys(actionContext.getCurrentKeys());

		try {
			if (!actionContext.isGrantedARRequest()) {
				this.grantARforKeyMixingRequest(actionContext);
			}
			//post ac for keyMixingResult
			PublicKey pk = actionContext.getMixerKeys().get(actionContext.getCurrentMixer());
			byte[] arMessage = MessageFactory.createAccessRight(GroupEnum.KEY_MIXING_RESULT, pk, 1);
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), arMessage, actionContext.getTenant());
			//post keyMixingRequest
			logger.log(Level.INFO, JSONConverter.marshal(keyMixingRequest));
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.KEY_MIXING_REQUEST.getValue(),
					JSONConverter.marshal(keyMixingRequest).getBytes(Charset.forName("UTF-8")),
					actionContext.getTenant());
			//inform am
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Requested key mixing from: " + actionContext.getCurrentMixer());
			this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}

	}

	protected void validateMixingResult(KeyMixingActionContext actionContext, KeyMixingResult mixingResult)
			throws UnivoteException {
		//TODO Verify proof
//		CyclicGroup cyclicGroup
//				= CryptoProvider.getSignatureSetup(actionContext.getCryptoSetting().getSignatureSetting());
//
//		Element currentG = cyclicGroup.getElementFrom(actionContext.getGenerator());
//		Element newG = cyclicGroup.getElementFrom(mixingResult.getGenerator());
//
//		Tuple vks = Tuple.getInstance();
//		for (String string : actionContext.getCurrentKeys()) {
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

			actionContext.setCurrentKeys(mixingResult.getMixedKeys());
			actionContext.setGenerator(mixingResult.getGenerator());

			int i = actionContext.getMixerOrder().indexOf(actionContext.getCurrentMixer());
			//Check if last mixer
			if (i + 1 == actionContext.getMixerOrder().size()) {
				//post mixedKeys
				this.postMixedKeys(actionContext);
			} else {
				actionContext.setCurrentMixer(actionContext.getMixerOrder().get(i + 1));

				this.createMixingRequest(actionContext);
			}
		}
	}

	protected void postMixedKeys(KeyMixingActionContext actionContext) {
		try {
			MixedKeys mixedKeys = new MixedKeys();
			mixedKeys.setMixedKeys(actionContext.getCurrentKeys());
			mixedKeys.setGenerator(actionContext.getGenerator());
			//post ac for mixedKeys
			PublicKey pk = this.tenantManager.getPublicKey(actionContext.getTenant());
			byte[] arMessage = MessageFactory.createAccessRight(GroupEnum.MIXED_KEYS, pk);
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), arMessage, actionContext.getTenant());

			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
					GroupEnum.MIXED_KEYS.getValue(),
					JSONConverter.marshal(mixedKeys).getBytes(Charset.forName("UTF-8")),
					actionContext.getTenant());
			//inform tenant
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Posted mixed keys." + actionContext.getCurrentMixer());
			this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
		} catch (UnivoteException ex) {
			this.informationService.informTenant(actionContext.getActionContextKey(), ex.getMessage());
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}

	}

	protected void grantARforKeyMixingRequest(KeyMixingActionContext actionContext) throws UnivoteException {
		//post ac for keyMixingResult
		PublicKey pk = this.tenantManager.getPublicKey(actionContext.getTenant());
		byte[] arMessage = MessageFactory.createAccessRight(GroupEnum.KEY_MIXING_REQUEST, pk);
		this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
				GroupEnum.ACCESS_RIGHT.getValue(), arMessage, actionContext.getTenant());
		actionContext.setGrantedARRequest(true);
	}

	protected String computePublicKeyString(PublicKey publicKey) throws UnivoteException {
		if (publicKey instanceof DSAPublicKey) {
			DSAPublicKey dsaPubKey = (DSAPublicKey) publicKey;
			return dsaPubKey.getY().toString(10);
		} else if (publicKey instanceof RSAPublicKey) {
			RSAPublicKey rsaPubKey = (RSAPublicKey) publicKey;
			BigInteger unicertRsaPubKey = MathUtil.pair(rsaPubKey.getPublicExponent(), rsaPubKey.getModulus());

			return unicertRsaPubKey.toString(10);
		}
		throw new UnivoteException("Unssuport public key type");
	}
}
