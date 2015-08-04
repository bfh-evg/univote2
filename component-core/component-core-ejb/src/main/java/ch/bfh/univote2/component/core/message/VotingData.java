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
package ch.bfh.univote2.component.core.message;

import javax.xml.bind.annotation.XmlType;

/**
 * <pre>
 * {
 *	"$schema": "http://json-schema.org/draft-04/schema",
 *	"title": "UniVote2: Schema of voting data",
 *	"type": "object",
 *	"properties": {
 *		"definition": {
 *			"description": "Election definition",
 *			"$ref": "electionDefinitionSchema.json"
 *		},
 *		"details": {
 *			"description": "Election details",
 *			"$ref": "electionDetailsSchema.json"
 *		},
 *		"cryptoSetting": {
 *			"description": "Crypto setting (encryption, signature, hashing)",
 *			"$ref": "cryptoSettingSchema.json"
 *		},
 *		"encryptionKey": {
 *			"description": "Encryption key (decimal notation)",
 *			"type": "string"
 *		},
 *		"signatureGenerator": {
 *			"description": "Signature generator (decimal notation)",
 *			"type": "string"
 *		}
 *	},
 *	"required": ["definition", "details", "cryptoSetting", "encryptionKey", "signatureGenerator"],
 *	"additionalProperties": false
 * }
 * </pre>
 *
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
@XmlType(propOrder={"definition", "details" , "cryptoSetting", "encryptionKey", "signatureGenerator"})
public class VotingData {

	private ElectionDefinition definition;
	private ElectionDetails details;
	private CryptoSetting cryptoSetting;
	private String encryptionKey;
	private String signatureGenerator;

	public VotingData() {
	}

	public VotingData(ElectionDefinition electionDefinition, ElectionDetails electionDetails,
			CryptoSetting cryptoSetting, String encryptionKey, String signatureGenerator) {
		this.definition = electionDefinition;
		this.details = electionDetails;
		this.cryptoSetting = cryptoSetting;
		this.encryptionKey = encryptionKey;
		this.signatureGenerator = signatureGenerator;
	}

	public ElectionDefinition getDefinition() {
		return definition;
	}

	public void setDefinition(ElectionDefinition definition) {
		this.definition = definition;
	}

	public ElectionDetails getDetails() {
		return details;
	}

	public void setDetails(ElectionDetails details) {
		this.details = details;
	}

	public CryptoSetting getCryptoSetting() {
		return cryptoSetting;
	}

	public void setCryptoSetting(CryptoSetting cryptoSetting) {
		this.cryptoSetting = cryptoSetting;
	}

	public String getEncryptionKey() {
		return encryptionKey;
	}

	public void setEncryptionKey(String encryptionKey) {
		this.encryptionKey = encryptionKey;
	}

	public String getSignatureGenerator() {
		return signatureGenerator;
	}

	public void setSignatureGenerator(String signatureGenerator) {
		this.signatureGenerator = signatureGenerator;
	}
}
