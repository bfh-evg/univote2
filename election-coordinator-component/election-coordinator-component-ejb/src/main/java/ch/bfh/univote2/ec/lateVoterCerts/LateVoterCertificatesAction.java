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
package ch.bfh.univote2.ec.lateVoterCerts;

import ch.bfh.uniboard.clientlib.AttributeHelper;
import ch.bfh.uniboard.data.AttributesDTO;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.ResultDTO;
import ch.bfh.uniboard.data.StringValueDTO;
import ch.bfh.unicrypt.helper.math.MathUtil;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModPrime;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.common.crypto.CryptoProvider;
import ch.bfh.univote2.common.crypto.CryptoSetup;
import ch.bfh.univote2.common.message.AccessRight;
import ch.bfh.univote2.common.message.Certificate;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.DL;
import ch.bfh.univote2.common.message.ElectionDefinition;
import ch.bfh.univote2.common.message.ElectoralRoll;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.MixedKeys;
import ch.bfh.univote2.common.message.SingleKeyMixingRequest;
import ch.bfh.univote2.common.message.SingleKeyMixingResult;
import ch.bfh.univote2.common.message.TrusteeCertificates;
import ch.bfh.univote2.common.message.VoterCertificates;
import ch.bfh.univote2.common.query.AlphaEnum;
import ch.bfh.univote2.common.query.GroupEnum;
import ch.bfh.univote2.common.query.MessageFactory;
import ch.bfh.univote2.common.query.QueryFactory;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.actionmanager.ActionManager;
import ch.bfh.univote2.component.core.data.BoardPreconditionQuery;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.data.TimerPreconditionQuery;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.services.InformationService;
import ch.bfh.univote2.component.core.services.UniboardService;
import ch.bfh.univote2.ec.BoardsEnum;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.management.timer.TimerNotification;

/**
 * Action handling the Late Voter Certificates (see section 6.3).
 *
 * @author Reto E. Koenig &lt;reto.koenig@bfh.ch&gt;
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
@Stateless
public class LateVoterCertificatesAction implements NotifiableAction {

	private static final String ACTION_NAME = LateVoterCertificatesAction.class.getSimpleName();
	private static final Logger logger = Logger.getLogger(LateVoterCertificatesAction.class.getName());

	@EJB
	private ActionManager actionManager;
	@EJB
	private InformationService informationService;
	@EJB
	private UniboardService uniboardService;
	@EJB
	private TenantManager tenantManager;

	@Override
	public ActionContext prepareContext(String tenant, String section) {
		ActionContextKey ack = new ActionContextKey(ACTION_NAME, tenant, section);
		LateVoterCertificatesContext actionContext = new LateVoterCertificatesContext(ack);
		try {
			{
				ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						QueryFactory.getQueryForElectoralRoll(actionContext.getSection())).getResult();
				if (result.getPost().isEmpty()) {
					throw new UnivoteException("Electoral Roll not yet published.");
				}
				ElectoralRoll electoralRoll
						= JSONConverter.unmarshal(ElectoralRoll.class, result.getPost().get(0).getMessage());
				actionContext.setElectoralRoll(electoralRoll);
			}
			{
				ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						QueryFactory.getQueryForCryptoSetting(actionContext.getSection())).getResult();
				if (result.getPost().isEmpty()) {
					throw new UnivoteException("Crypto setting not yet published.");
				}
				CryptoSetting cryptoSetting
						= JSONConverter.unmarshal(CryptoSetting.class, result.getPost().get(0).getMessage());
				actionContext.setCryptoSetting(cryptoSetting);
			}
			{
				ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						QueryFactory.getQueryForMixedKeys(actionContext.getSection())).getResult();
				if (result.getPost().isEmpty()) {
					throw new UnivoteException("Mixed keys not yet published.");
				}
				MixedKeys mixedKeys
						= JSONConverter.unmarshal(MixedKeys.class, result.getPost().get(0).getMessage());
				actionContext.setSignatureGenerator(mixedKeys.getGenerator());
			}
			{
				ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						QueryFactory.getQueryForTrusteeCerts(actionContext.getSection()));
				if (result.getResult().getPost().isEmpty()) {
					throw new UnivoteException("Trustees certificates not published yet.");
				}
				this.parseMixers(actionContext, result.getResult().getPost().get(0));
			}
			{
				ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
						QueryFactory.getQueryForElectionDefinition(actionContext.getSection())).getResult();
				if (result.getPost().isEmpty()) {
					throw new UnivoteException("Election definition not yet published.");
				}
				ElectionDefinition electionDefinition
						= JSONConverter.unmarshal(ElectionDefinition.class, result.getPost().get(0).getMessage());
				actionContext.setElectionDefinition(electionDefinition);
				Date votingPeriodEnd = electionDefinition.getVotingPeriodEnd();
				// end of voting period notification
				TimerPreconditionQuery bQuery = new TimerPreconditionQuery(votingPeriodEnd);
				actionContext.getPreconditionQueries().add(bQuery);
				// Check if end date reached. If true => post condition is also true
				Date now = new Date();
				if (now.after(votingPeriodEnd)) {
					actionContext.setPostCondition(true);
				}
			}
			// new voter certificate notification
			BoardPreconditionQuery bQuery
					= new BoardPreconditionQuery(QueryFactory.getQueryFormUniCertForVoterCert(),
							BoardsEnum.UNICERT.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
			// single key mixing result notification
			BoardPreconditionQuery bQuery2
					= new BoardPreconditionQuery(QueryFactory.getQueryForSingleKeyMixingResults(section),
							BoardsEnum.UNICERT.getValue());
			actionContext.getPreconditionQueries().add(bQuery2);
		} catch (UnivoteException ex) {
			Logger.getLogger(LateVoterCertificatesAction.class.getName()).log(Level.SEVERE, null, ex);
			// TODO: Failure reporting to, and appropriate handling by the action manager to be provided
		}
		return actionContext;
	}

	private void parseMixers(LateVoterCertificatesContext actionContext, PostDTO post) throws UnivoteException {
		byte[] message = post.getMessage();
		TrusteeCertificates trusteeCertificates;
		trusteeCertificates = JSONConverter.unmarshal(TrusteeCertificates.class, message);
		List<Certificate> mixers = trusteeCertificates.getMixerCertificates();
		if (mixers == null || mixers.isEmpty()) {
			throw new UnivoteException("Invalid trustees certificates message. mixerCertificates is missing.");
		}
		for (Certificate c
				: trusteeCertificates.getMixerCertificates()) {
			try {
				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
				InputStream in = new ByteArrayInputStream(c.getPem().getBytes());
				X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
				PublicKey pk = cert.getPublicKey();
				actionContext.getMixerKeys().add(pk);
			} catch (CertificateException ex) {
				throw new UnivoteException("Invalid trustees certificates message. Could not load pem.", ex);
			}
		}
	}

	@Override
	@Asynchronous
	public void run(ActionContext actionContext) {
		// Called as a side effect from the action manager. Nothing to do here except:

		// Ensure that the following access rights are set correctly:
		// - ours
		// - those of the mixers
		createAccessRights((LateVoterCertificatesContext) actionContext);
	}

	@Override
	@Asynchronous
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (!(actionContext instanceof LateVoterCertificatesContext)) {
			return;
		}
		LateVoterCertificatesContext context = (LateVoterCertificatesContext) actionContext;

		// The following notification indicates the end of the voting period.
		if (notification instanceof TimerNotification) {
			this.actionManager.runFinished(actionContext, ResultStatus.FINISHED);
			return;
		}

		if ((notification instanceof PostDTO)) {
			PostDTO post = (PostDTO) notification;
			AttributesDTO.AttributeDTO group
					= AttributeHelper.searchAttribute(post.getAlpha(), AlphaEnum.GROUP.getValue());
			if (((StringValueDTO) group.getValue()).getValue().equals(GroupEnum.SINGLE_KEY_MIXING_RESULT.getValue())) {
				try {
					SingleKeyMixingResult skmr
							= JSONConverter.unmarshal(SingleKeyMixingResult.class, post.getMessage());
					if (!verifySingleKeyMixingResult(skmr)) {
						logger.log(Level.SEVERE, "Invalid proof for single key mixing result: {0}", skmr.getMixerId());
						this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
						return;
					}
					CertificateProcessingRecord cpr = context.findRecordByCurrentVK(skmr.getPublicKey());
					if (moreMoreMixersToEngage(cpr)) {
						cpr.incrementK();
						this.createMixingRequest(context, cpr.getK(), skmr.getPublicKey(), skmr.getMixedKey());
					} else {
						switch (cpr.getProcessingState()) {
							case MIXING_OLD_VK:
								this.revokeOldCertificate(context, cpr, skmr.getMixedKey());
								break;
							case MIXING_NEW_VK:
								this.activateNewCertificate(context, cpr, skmr.getMixedKey());
								break;
							default:
							// ERROR !! Should not happen!
							// TODO: Add logger statement
							// TODO: Write more tests!
						}
					}
				} catch (UnivoteException ex) {
					logger.log(Level.SEVERE, "Cannot unmarshal message.", ex);
					this.informationService.informTenant(actionContext.getActionContextKey(),
							ex.getMessage());
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				}
			} else if (((StringValueDTO) group.getValue()).getValue().equals(GroupEnum.CERTIFICATE.getValue())) {
				try {
					Certificate voterCertificate = JSONConverter.unmarshal(Certificate.class, post.getMessage());
					if (!verifyCertificate(voterCertificate)) {
						// Is certificate valid? If not, then terminate this action.
						this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
						return;
					}
					if (!checkIfEligible(context, voterCertificate)) {
						// Is voter eligible? If not, then terminate this action.
						this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
						return;
					}
					// Given Zi'. Then:
					// if record exists
					// - queue certificate Zi' and return
					if (testAndSetCertificateProcessingRecord(context, voterCertificate)) {
						// There is another certificate for this voter being processed; queue this one for
						// later processing.
						queueCertificate(context, voterCertificate);
						this.actionManager.runFinished(actionContext, ResultStatus.RUN_FINISHED);
						return;
					}
					CertificateProcessingRecord cpr = context.findRecordByCommonName(voterCertificate.getCommonName());
					// else
					// - create certificate processing record ==> see above
					//   Label 1:
					postNewCertificateAndCheckOldOne(context, cpr);
				} catch (UnivoteException ex) {
					logger.log(Level.SEVERE, "Cannot unmarshal message.", ex);
					this.informationService.informTenant(actionContext.getActionContextKey(),
							ex.getMessage());
					this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
				}
			} else {
				// Unexpected post...
				logger.log(Level.SEVERE, "Unexpected post!", notification);
				this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
			}
		}
		// Unexpected notification...
		logger.log(Level.SEVERE, "Unexpected notification!", notification);
		this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
	}

	//The following is copied from KeyMixingAction... Might be 'out-sourced'
	private PublicKey getPublicKeyFromCertificate(Certificate certificate) throws UnivoteException {
		PublicKey pk;
		String pem = certificate.getPem();
		if (pem == null) {
			throw new UnivoteException("Invalid certificate message. pem is missing.");
		}
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(pem.getBytes());
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);
			pk = cert.getPublicKey();
			return pk;
		} catch (CertificateException ex) {
			throw new UnivoteException("Invalid voter certificates message. Could not load pem.", ex);
		}
	}

	private boolean checkIfEligible(LateVoterCertificatesContext context, Certificate voterCertificate) {
		// TODO ...
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private boolean verifyCertificate(Certificate voterCertificate) {
		// TODO ...
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private void queueCertificate(LateVoterCertificatesContext context, Certificate voterCertificate) {
		// TODO ...
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private boolean testAndSetCertificateProcessingRecord(LateVoterCertificatesContext context,
			Certificate voterCertificate) {
		// TODO ...
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private void postNewCertificateAndCheckOldOne(LateVoterCertificatesContext context,
			CertificateProcessingRecord cpr) {
		try {
			// post new voter certificate Zi'
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.NEW_VOTER_CERTIFICATE.getValue(), JSONConverter.marshal(cpr.getCertificate()).getBytes(),
					context.getTenant());
			this.informationService.informTenant(context.getActionContextKey(), "New Certificate for: "
					+ cpr.getCertificate().getCommonName());
			Certificate cert = checkIfOldCertifcateExists(context, cpr.getCertificate().getCommonName());
			if (cert != null) {
				// old certificate Zi exists:
				//   - set current VKi = Zi.pem.pk
				//   - set processingState = MIXING_OLD_VK
				cpr.setOldCertificate(cert);
				cpr.setProcessingState(CertificateProcessingRecord.ProcessingState.MIXING_OLD_VK);
				cpr.setCurrentVK(this.getVKFromCertificate(cert));
			} else {
				// - set current VKi = Zi'.pem.pk
				// - set processingState = MIXING_NEW_VK (?)
				cpr.setProcessingState(CertificateProcessingRecord.ProcessingState.MIXING_NEW_VK);
				cpr.setCurrentVK(this.getVKFromCertificate(cpr.getCertificate()));
			}
			//   - start mixing
			cpr.resetK();
			this.createMixingRequest(context, cpr.getK(), cpr.getCurrentVK(), cpr.getCurrentVK());
		} catch (UnivoteException ex) {
			Logger.getLogger(LateVoterCertificatesAction.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void createMixingRequest(LateVoterCertificatesContext context, int k, String currentVK,
			String vkToBeMixed) {
		try {
			String mixerId = getMixerId(context, k);
			// - post request
			SingleKeyMixingRequest smr = new SingleKeyMixingRequest(mixerId, currentVK, vkToBeMixed);
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.MIXED_KEYS.getValue(),
					JSONConverter.marshal(smr).getBytes(Charset.forName("UTF-8")),
					context.getTenant());
			//inform tenant
			this.informationService.informTenant(context.getActionContextKey(),
					"Posted single key mixing request to " + mixerId + " for VK: " + currentVK);
			logger.log(Level.INFO, "Posted single key mixing request to {0} for VK: {1}",
					new Object[]{mixerId, currentVK});
		} catch (UnivoteException ex) {
			logger.log(Level.SEVERE, ex.getMessage(), ex);
		} finally {
			this.actionManager.runFinished(context, ResultStatus.RUN_FINISHED);
		}
	}

	private void createAccessRights(LateVoterCertificatesContext context) {
		try {
			PublicKey ownPK = this.tenantManager.getPublicKey(context.getTenant());
			grantAccessRight(context, ownPK, GroupEnum.NEW_VOTER_CERTIFICATE);
			grantAccessRight(context, ownPK, GroupEnum.ADDED_VOTER_CERTIFICATE);
			grantAccessRight(context, ownPK, GroupEnum.CANCELLED_VOTER_CERTIFICATE);
			grantAccessRight(context, ownPK, GroupEnum.SINGLE_KEY_MIXING_REQUEST);
		} catch (UnivoteException ex) {
			logger.log(Level.SEVERE, ex.getMessage());
			this.informationService.informTenant(context.getActionContextKey(),
					"Could not get public key");
		}
		for (PublicKey pk : context.getMixerKeys()) {
			grantAccessRight(context, pk, GroupEnum.SINGLE_KEY_MIXING_RESULT);
		}
	}

	private void grantAccessRight(ActionContext actionContext, PublicKey publickey, GroupEnum group) {
		try {
			ResultContainerDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForAccessRight(actionContext.getSection(),
							publickey, group));
			if (result.getResult().getPost().isEmpty()) {
				byte[] message = MessageFactory.createAccessRight(group, publickey);
				this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), actionContext.getSection(),
						GroupEnum.ACCESS_RIGHT.getValue(), message, actionContext.getTenant());
			}
		} catch (UnivoteException ex) {
			logger.log(Level.SEVERE, ex.getMessage());
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Could not post access right for " + group.getValue());
		}
	}

	private String getMixerId(LateVoterCertificatesContext context, int k) throws UnivoteException {
		PublicKey publicKey = context.getMixerKeys().get(k);
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

	private String getVKFromCertificate(Certificate certificate) throws UnivoteException {
		PublicKey pk = this.getPublicKeyFromCertificate(certificate);
		if (pk instanceof DSAPublicKey) {
			DSAPublicKey dsaPubKey = (DSAPublicKey) pk;
			return dsaPubKey.getY().toString(10);
		} else {
			throw new UnivoteException("Unsupported key type.");
		}
	}

	private void revokeOldCertificate(LateVoterCertificatesContext context, CertificateProcessingRecord cpr,
			String oldMixedVK) {
		try {
			//   - post old certificate Zi to the set of cancelled certificates
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.CANCELLED_VOTER_CERTIFICATE.getValue(),
					JSONConverter.marshal(cpr.getOldCertificate()).getBytes(), context.getTenant());
			//   - remove access right for old mixed VKi

			CryptoSetup sSetup = CryptoProvider.getSignatureSetup(context.getCryptoSetting().getSignatureSetting());
			GStarModPrime signatureGroup = (GStarModPrime) sSetup.cryptoGroup;

			AccessRight ar = new AccessRight();
			ar.setCrypto(new DL(signatureGroup.getModulus().toString(), signatureGroup.getOrder().
					toString(), context.getSignatureGenerator(), oldMixedVK));
			ar.setGroup(GroupEnum.BALLOT.getValue());
			ar.setAmount(0);
			ar.setStartTime(context.getElectionDefinition().getVotingPeriodBegin()); // could be omitted
			ar.setEndTime(context.getElectionDefinition().getVotingPeriodEnd()); // could be omitted
			byte[] message = JSONConverter.marshal(ar).getBytes();
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), message, context.getTenant());
			this.informationService.informTenant(context.getActionContextKey(),
					"AccessRight for voter revoked.");
			logger.log(Level.INFO, "pk revoked {0}.", oldMixedVK);
			//   - if exists ballot (old mixed VKi)
			// Check if a ballot exists for revoked mixed vk_i if so... abort.
			ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForBallot(context.getSection(), oldMixedVK)).getResult();
			if (!result.getPost().isEmpty()) {
				//  - remove certificate processing record
				context.removeCertificateProcessingRecord(cpr);
				logger.log(Level.INFO, "{0} has already voted.", cpr.getCertificate().getCommonName());
				//  - abort
				this.actionManager.runFinished(context, ResultStatus.RUN_FINISHED);
				return;
			}
			logger.log(Level.INFO, "{0} has not voted yet.", cpr.getCertificate().getCommonName());
			// - set current VKi = Zi'.pem.pk
			cpr.setCurrentVK(this.getVKFromCertificate(cpr.getCertificate()));
			// - new mixed VKi = mix(current VKi)
			cpr.setProcessingState(CertificateProcessingRecord.ProcessingState.MIXING_NEW_VK);
			cpr.resetK();
			this.createMixingRequest(context, cpr.getK(), cpr.getCurrentVK(), cpr.getCurrentVK());
		} catch (UnivoteException ex) {
			logger.log(Level.SEVERE, ex.getMessage());
			this.informationService.informTenant(context.getActionContextKey(), ex.getMessage());
		}
	}

	private void activateNewCertificate(LateVoterCertificatesContext context, CertificateProcessingRecord cpr,
			String newMixedVK) {
		try {
			// - post Zi' to set of added voter certificates
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.ADDED_VOTER_CERTIFICATE.getValue(),
					JSONConverter.marshal(cpr.getCertificate()).getBytes(), context.getTenant());

			// - set access right for new mixed VKi
			CryptoSetup sSetup = CryptoProvider.getSignatureSetup(context.getCryptoSetting().getSignatureSetting());
			GStarModPrime signatureGroup = (GStarModPrime) sSetup.cryptoGroup;

			AccessRight ar = new AccessRight();
			ar.setCrypto(new DL(signatureGroup.getModulus().toString(), signatureGroup.getOrder().
					toString(), context.getSignatureGenerator(), newMixedVK));
			ar.setGroup(GroupEnum.BALLOT.getValue());
			ar.setAmount(1);
			ar.setStartTime(context.getElectionDefinition().getVotingPeriodBegin());
			ar.setEndTime(context.getElectionDefinition().getVotingPeriodEnd());
			byte[] message = JSONConverter.marshal(ar).getBytes();
			this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
					GroupEnum.ACCESS_RIGHT.getValue(), message, context.getTenant());
			this.informationService.informTenant(context.getActionContextKey(),
					"AccessRight for voter granted.");
			logger.log(Level.INFO, "pk granted {0}.", newMixedVK);
			// - if queue of certificate to be processed is not empty:
			if (!cpr.getQueuedNotifications().isEmpty()) {
				// - get next Zi'
				cpr.setCertificate(cpr.getQueuedNotifications().remove());
				// - goto
				postNewCertificateAndCheckOldOne(context, cpr);
			} else {
				// - remove certificate processing record
				context.removeCertificateProcessingRecord(cpr);
				this.actionManager.runFinished(context, ResultStatus.RUN_FINISHED);
				logger.log(Level.INFO, "Finished late voter certificate processing for {0}",
						cpr.getCertificate().getCommonName());
			}
		} catch (UnivoteException ex) {
			logger.log(Level.SEVERE, ex.getMessage());
			this.informationService.informTenant(context.getActionContextKey(), ex.getMessage());
		}
	}

	private boolean verifySingleKeyMixingResult(SingleKeyMixingResult skmr) {
		// TODO: Implement...
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private boolean moreMoreMixersToEngage(CertificateProcessingRecord cpr) {
		// TODO: Implement...
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private Certificate checkIfOldCertifcateExists(ActionContext context, String commonName) throws UnivoteException {
		//Get List Z_V and put all items (z_v) into Z_AV with the following constraint:
		//If Z_C contains the item (z_v) then skip it and remove it from Z_C
		//Get VoterCertificates (list of all)
		//Go through the voter certificates and check for each:
		// If commonName of item is equal to commonName of the new certificate then
		// Check if this item has already been cancelled (is also part of Z_C).
		// If no, add the item to Z_VA
		List<Certificate> zvaList = new ArrayList<>();
		// TODO: Minimal optimization possible if no certificate available for the common name in the
		// votercertificates post.
		ResultDTO voterCertificatesResult = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForVoterCertificates(context.getSection())).getResult();

		VoterCertificates voterCertificates = JSONConverter.unmarshal(VoterCertificates.class,
				voterCertificatesResult.getPost().get(0).getMessage());
		for (Certificate certificate : voterCertificates.getVoterCertificates()) {
			if (certificate.getCommonName().equals(commonName)) {
				zvaList.add(certificate);
				break;
			}
		}

		//Get List Z_A and put all items (z_v) into with the following constraint:
		//if Z_C contains the item (z_v) then skip it and remove it from Z_C
		//Get addedVoterCertificate Z_A from UVB
		ResultDTO addedVoterCertificateResult = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForAddedVoterCertificate(context.getSection(), commonName)).getResult();
		for (PostDTO post : addedVoterCertificateResult.getPost()) {
			Certificate certificate = JSONConverter.unmarshal(Certificate.class, post.getMessage());
			zvaList.add(certificate);
		}

		//Get cancelledVoterCertifiecate Z_C from UBV
		ResultDTO cancelledVoterCertificateResult = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForCancelledVoterCertificate(context.getSection(), commonName)).getResult();
		for (PostDTO post : cancelledVoterCertificateResult.getPost()) {
			Certificate cancelledCertificate = JSONConverter.unmarshal(Certificate.class, post.getMessage());
			for (Iterator<Certificate> iterator = zvaList.iterator(); iterator.hasNext();) {
				Certificate avCertificate = iterator.next();
				if (cancelledCertificate.getPem().equals(avCertificate.getPem())) {
					iterator.remove();
					break;
				}
			}
		}

		//Check if Z_VA is empty if not... it should contain exactliy one Element -> Z_i
		if (!zvaList.isEmpty()) {
			return zvaList.get(0);
		} else {
			return null;
		}
	}
}
