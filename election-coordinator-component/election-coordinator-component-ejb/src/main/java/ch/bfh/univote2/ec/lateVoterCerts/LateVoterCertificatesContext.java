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

import ch.bfh.univote2.common.message.Certificate;
import ch.bfh.univote2.common.message.CryptoSetting;
import ch.bfh.univote2.common.message.ElectionDefinition;
import ch.bfh.univote2.common.message.ElectoralRoll;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class LateVoterCertificatesContext extends ActionContext {

	private ElectoralRoll electoralRoll;
	private CryptoSetting cryptoSetting;
	private ElectionDefinition electionDefinition;
	private String signatureGenerator;
	private final List<PublicKey> mixerKeys = new ArrayList<>();
	private final Set<CertificateProcessingRecord> certificateProcessingRecords = new ConcurrentSkipListSet<>();
	private final Map<String, String> mixerGenerators = new HashMap<>();

	public LateVoterCertificatesContext(ActionContextKey actionContextKey) {
		super(actionContextKey, new ArrayList<>(), true);
	}

	@Override
	protected void purgeData() {
		this.electoralRoll = null;
		this.cryptoSetting = null;
		this.mixerKeys.clear();
		this.certificateProcessingRecords.clear();
	}

	public ElectoralRoll getElectoralRoll() {
		return electoralRoll;
	}

	public void setElectoralRoll(ElectoralRoll electoralRoll) {
		this.electoralRoll = electoralRoll;
	}

	public CryptoSetting getCryptoSetting() {
		return cryptoSetting;
	}

	public void setCryptoSetting(CryptoSetting cryptoSetting) {
		this.cryptoSetting = cryptoSetting;
	}

	public boolean gotAllNotifications() {
		return this.electoralRoll != null && this.cryptoSetting != null;
	}

	public List<PublicKey> getMixerKeys() {
		return this.mixerKeys;
	}

	/**
	 * Returns the certificate processing record, if any, for a voter identified by his/her common name.
	 *
	 * @param commonName
	 * @return
	 */
	public CertificateProcessingRecord findRecordByCommonName(String commonName) {
		for (CertificateProcessingRecord cpr : this.certificateProcessingRecords) {
			if (cpr.getCertificate().getCommonName().equals(commonName)) {
				return cpr;
			}
		}
		return null;
	}

	/**
	 * Returns the certificate processing record, if any, for the given verification key.
	 *
	 * @param currentVK
	 * @return
	 */
	public CertificateProcessingRecord findRecordByCurrentVK(String currentVK) {
		for (CertificateProcessingRecord cpr : this.certificateProcessingRecords) {
			if (cpr.getCurrentVK().equals(currentVK)) {
				return cpr;
			}
		}
		return null;
	}

	public synchronized boolean testAndSetCertificateProcessingRecord(Certificate voterCertificate) {
		for (CertificateProcessingRecord cpr : this.certificateProcessingRecords) {
			if (cpr.getCertificate().getCommonName().equals(voterCertificate.getCommonName())) {
				return true;
			}
		}
		CertificateProcessingRecord cpr = new CertificateProcessingRecord();
		cpr.setCertificate(voterCertificate);
		this.certificateProcessingRecords.add(cpr);
		return false;
	}

	public synchronized void removeCertificateProcessingRecord(CertificateProcessingRecord cpr) {
		this.certificateProcessingRecords.remove(cpr);
	}

	public ElectionDefinition getElectionDefinition() {
		return electionDefinition;
	}

	public void setElectionDefinition(ElectionDefinition electionDefinition) {
		this.electionDefinition = electionDefinition;
	}

	public String getSignatureGenerator() {
		return signatureGenerator;
	}

	public void setSignatureGenerator(String signatureGenerator) {
		this.signatureGenerator = signatureGenerator;
	}

	public Map<String, String> getMixerGenerators() {
		return mixerGenerators;
	}

}
