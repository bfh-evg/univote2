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

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultDTO;
import ch.bfh.unicrypt.helper.math.MathUtil;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.common.message.Certificate;
import ch.bfh.univote2.common.message.ElectoralRoll;
import ch.bfh.univote2.common.message.JSONConverter;
import ch.bfh.univote2.common.message.VoterCertificates;
import ch.bfh.univote2.common.query.GroupEnum;
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
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class LateVoterCertificates extends AbstractAction implements NotifiableAction {

	private static final String ACTION_NAME = "LateVoterCertificates";
	private static final Logger logger = Logger.getLogger(LateVoterCertificates.class.getName());

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
		return new LateVoterCertificatesContext(ack);
	}

	@Override
	protected boolean checkPostCondition(ActionContext actionContext) {
		return false; //You can always do that!  (?)
	}

	@Override
	protected void definePreconditions(ActionContext actionContext) {
		if (!(actionContext instanceof LateVoterCertificatesContext)) {
			return;
		}
		LateVoterCertificatesContext context = (LateVoterCertificatesContext) actionContext;
		try {
			ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForElectoralRoll(actionContext.getSection())).getResult();
			if (result.getPost().isEmpty()) {
				throw new UnivoteException("Electoral Roll not yet published.");

			}
			ElectoralRoll electoralRoll = JSONConverter.unmarshal(ElectoralRoll.class, result.getPost().get(0).getMessage());
			context.setElectoralRoll(electoralRoll);

			BoardPreconditionQuery bQuery = new BoardPreconditionQuery(QueryFactory.getQueryFormUniCertForVoterCert(), BoardsEnum.UNICERT.getValue());
			actionContext.getPreconditionQueries().add(bQuery);
		} catch (UnivoteException ex) {
			Logger.getLogger(LateVoterCertificates.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public void run(ActionContext actionContext) {
		logger.log(Level.INFO, "run method not needed in " + actionContext.getActionContextKey());
		this.informationService.informTenant(actionContext.getActionContextKey(),
				"run method not needed in " + actionContext.getActionContextKey());

	}

	@Override
	public void notifyAction(ActionContext actionContext, Object notification) {
		if (!(actionContext instanceof LateVoterCertificatesContext)) {
			return;
		}
		LateVoterCertificatesContext context = (LateVoterCertificatesContext) actionContext;

		if (!(notification instanceof PostDTO)) {
			return;
		}
		PostDTO post = (PostDTO) notification;
		logger.log(Level.INFO, "Message received for " + ACTION_NAME);
		this.informationService.informTenant(actionContext.getActionContextKey(),
				"Message received for " + ACTION_NAME);
		try {
			Certificate voterCertificate = JSONConverter.unmarshal(Certificate.class, post.getMessage());
			internalRun(context, voterCertificate);
		} catch (UnivoteException ex) {
			logger.log(Level.SEVERE, "Do not understand message.", ex);
			this.informationService.informTenant(actionContext.getActionContextKey(),
					"Do not understand message.");
			this.actionManager.runFinished(actionContext, ResultStatus.FAILURE);
		}
	}

	private void internalRun(LateVoterCertificatesContext context, Certificate voterCertificate) throws UnivoteException {
		//TODO: Verify Z'_i.
		ElectoralRoll roll = context.getElectoralRoll();
		String commonName = voterCertificate.getCommonName();

		// Check if V_i \in V
		if (!(roll.getVoterIds().contains(commonName))) {
			return;
		}

		//Post new VoterCertificate to UBV
		this.uniboardService.post(BoardsEnum.UNIVOTE.getValue(), context.getSection(),
				GroupEnum.NEW_VOTER_CERTIFICATE.getValue(), JSONConverter.marshal(voterCertificate).getBytes(), context.getTenant());

		//Get cancelledVoterCertifiecate Z_C from UBV
		List<Certificate> cancelledVoterCertificateList = new ArrayList<>();
		ResultDTO cancelledVoterCertificateResult = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForCancelledVoterCertificate(context.getSection(), commonName)).getResult();
		for (PostDTO post : cancelledVoterCertificateResult.getPost()) {
			Certificate certificate = JSONConverter.unmarshal(Certificate.class, post.getMessage());
			if (certificate.getCommonName().equals(commonName)) {
				cancelledVoterCertificateList.add(certificate);
			}
		}

		//Get List Z_V into Z_AV if entry in Z_C do not put it in, remove it from Z_C
		//Get VoterCertificates (list of all)
		//TODO: Expecting exactly one result which represents a list... the voterCertificates. Is that correct? Or should it be a list of certificates?
		List<Certificate> voterCertificateList = new ArrayList<>();
		ResultDTO voterCertificatesResult = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForVoterCertificates(context.getSection())).getResult();
		if (!(voterCertificatesResult.getPost().isEmpty())) {
			VoterCertificates voterCertificates = JSONConverter.unmarshal(VoterCertificates.class,
					voterCertificatesResult.getPost().get(0).getMessage());
			for (Certificate certificate : voterCertificates.getVoterCertificates()) {
				if (certificate.getCommonName().equals(commonName)) {
					for (Iterator<Certificate> iterator = cancelledVoterCertificateList.iterator(); iterator.hasNext();) {
						Certificate cancelledCertificate = iterator.next();
						if (Objects.deepEquals(certificate, cancelledCertificate)) {
							iterator.remove();
						} else {
							voterCertificateList.add(certificate);
						}
					}

				}
			}
		}

		//Get List Z_A into Z_AV if entry in Z_C do not put it in, remove it from Z_C
		//Get addedVoterCertificate Z_A from UVB
		ResultDTO addedVoterCertificateResult = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForAddedVoterCertificate(context.getSection(), commonName)).getResult();
		for (PostDTO post : addedVoterCertificateResult.getPost()) {
			Certificate certificate = JSONConverter.unmarshal(Certificate.class, post.getMessage());
			if (certificate.getCommonName().equals(commonName)) {

				for (Iterator<Certificate> iterator = cancelledVoterCertificateList.iterator(); iterator.hasNext();) {
					Certificate cancelledCertificate = iterator.next();
					if (Objects.deepEquals(certificate, cancelledCertificate)) {
						iterator.remove();
					} else {
						voterCertificateList.add(certificate);
					}
				}
			}
		}
		//Check if Z_AV is empty if not... it should contain exactliy one Element -> Z_i
		if (!(voterCertificateList.isEmpty())) {
			//if Z_i is present: Check if there is a vote for according v^k_i  yes... abort
			//if Z_i is present: Remove accessRight for v^k_i
			//if Z_i is present: Add Z_i to Z_C on UBV
			// get vk'_i from Z'_i
			//...
			Certificate revokableCertificate = voterCertificateList.get(0);
			PublicKey pk = getPublicKeyFromCertificate(revokableCertificate);
			String pkString = computePublicKeyString(pk);

			// TODO: Call Mixer
			ResultDTO result = this.uniboardService.get(BoardsEnum.UNIVOTE.getValue(),
					QueryFactory.getQueryForVotingData(context.getSection())).getResult();
		}

	}

	//The following is copied from KeyMixingAction... Might be 'out-sourced'
	protected PublicKey getPublicKeyFromCertificate(Certificate certificate) throws UnivoteException {
		PublicKey pk = null;
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
			throw new UnivoteException("Invalid trustees certificates message. Could not load pem.", ex);
		}
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
