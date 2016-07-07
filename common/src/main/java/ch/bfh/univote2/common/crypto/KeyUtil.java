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
package ch.bfh.univote2.common.crypto;

import ch.bfh.uniboard.clientlib.signaturehelper.SchnorrSignatureHelper;
import ch.bfh.uniboard.data.AttributeDTO;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.DSAParams;
import java.security.interfaces.DSAPrivateKey;
import java.security.interfaces.DSAPublicKey;
import java.security.spec.DSAPrivateKeySpec;
import java.util.ArrayList;
import java.util.List;

public class KeyUtil {

	public static DSAPublicKey getDSAPublicKey(String certificatePath) throws Exception {
		CertificateFactory factory = CertificateFactory.getInstance("X.509");
		X509Certificate certificate = (X509Certificate) factory.generateCertificate(
				new FileInputStream(certificatePath));
		return (DSAPublicKey) certificate.getPublicKey();
	}

	public static DSAPrivateKey getDSAPrivateKey(String keyPath, String password, DSAParams params) throws Exception {
		String encryptedKey = new String(Files.readAllBytes(Paths.get(keyPath)));
		byte[] decryptedKey = KeyEncryption.decryptPrivateKey(encryptedKey, password);
		DSAPrivateKeySpec keySpec = new DSAPrivateKeySpec(
				new BigInteger(decryptedKey), params.getP(), params.getQ(), params.getG());
		return (DSAPrivateKey) KeyFactory.getInstance("DSA").generatePrivate(keySpec);
	}

	public static boolean checkDSAKeys(DSAPublicKey publicKey, DSAPrivateKey privateKey) throws Exception {
		byte[] message = "Hello World!".getBytes();
		List<AttributeDTO> alpha = new ArrayList();
		BigInteger signature = new SchnorrSignatureHelper(privateKey).sign(message, alpha);
		return new SchnorrSignatureHelper(publicKey).verify(message, alpha, signature);
	}

	public static void printDSAPublicKey(DSAPublicKey publicKey) {
		DSAParams params = publicKey.getParams();
		System.out.println("Modulo:      " + params.getP());
		System.out.println("Order:       " + params.getQ());
		System.out.println("Generator:   " + params.getG());
		System.out.println("Public Key:  " + publicKey.getY());
	}
}
