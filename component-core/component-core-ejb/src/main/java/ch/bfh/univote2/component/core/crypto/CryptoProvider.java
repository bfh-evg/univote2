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
package ch.bfh.univote2.component.core.crypto;

import ch.bfh.unicrypt.helper.hash.HashAlgorithm;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModPrime;
import ch.bfh.unicrypt.math.algebra.multiplicative.classes.GStarModSafePrime;
import ch.bfh.univote2.component.core.message.CryptoSetting;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class CryptoProvider {

	public static CryptoSetting getCryptoSetting(int securtityLevel) {
		String encSetting;
		String sigSetting;
		String hashSetting;

		switch (securtityLevel) {
			case 0:
				encSetting = "RC0e";
				sigSetting = "RC0s";
				hashSetting = "";
				break;
			case 1:
				encSetting = "RC1e";
				sigSetting = "RC1s";
				hashSetting = "H1";
				break;
			case 2:
				encSetting = "RC2e";
				sigSetting = "RC2s";
				hashSetting = "H2";
				break;
			case 3:
				encSetting = "RC3e";
				sigSetting = "RC3s";
				hashSetting = "H3";
				break;
			case 4:
				encSetting = "RC4e";
				sigSetting = "RC4s";
				hashSetting = "H4";
				break;
			case 5:
				encSetting = "RC5e";
				sigSetting = "RC5s";
				hashSetting = "H5";
				break;
			default:
				encSetting = "RC5e";
				sigSetting = "RC5s";
				hashSetting = "H5";
		}
		return new CryptoSetting(encSetting, sigSetting, hashSetting);
	}

	public static GStarModSafePrime getEncryptionSetup(String encryptionClassName) {
		switch (encryptionClassName) {
			case "RC0e":
				return GStarModSafePrime.getFirstInstance(8);
			case "RC1e":
				return GStarModSafePrime.getFirstInstance(1024);
			case "RC2e":
				return GStarModSafePrime.getFirstInstance(2048);
			case "RC3e":
				return GStarModSafePrime.getFirstInstance(3072);
			case "RC4e":
				return GStarModSafePrime.getFirstInstance(7680);
			case "RC5e":
				return GStarModSafePrime.getFirstInstance(15360);
			default:
				return GStarModSafePrime.getFirstInstance(3072);
		}
	}

	public static GStarModPrime getSignatureSetup(String signatureClassName) {
		switch (signatureClassName) {
			case "RC0s":
				return GStarModPrime.getFirstInstance(8, 6);
			case "RC1s":
				return GStarModPrime.getFirstInstance(1024, 160);
			case "RC2s":
				return GStarModPrime.getFirstInstance(2048, 224);
			case "RC3s":
				return GStarModPrime.getFirstInstance(3072, 256);
			case "RC4s":
				return GStarModPrime.getFirstInstance(7680, 384);
			case "RC5s":
				return GStarModPrime.getFirstInstance(15360, 512);
			default:
				return GStarModPrime.getFirstInstance(3072, 256);
		}
	}

	public static HashAlgorithm getHashAlgorithm(String hashFunctionName) {

		switch (hashFunctionName) {
			case "H1":
				return HashAlgorithm.SHA1;
			case "H2":
				return HashAlgorithm.SHA224;
			case "H3":
				return HashAlgorithm.SHA256;
			case "H4":
				return HashAlgorithm.SHA384;
			case "H5":
				return HashAlgorithm.SHA512;
			default:
				return HashAlgorithm.SHA256;
		}
	}
}
