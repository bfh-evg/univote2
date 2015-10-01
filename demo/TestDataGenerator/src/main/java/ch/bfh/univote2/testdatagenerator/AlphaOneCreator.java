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
package ch.bfh.univote2.testdatagenerator;

import ch.bfh.unicrypt.crypto.schemes.encryption.classes.AESEncryptionScheme;
import ch.bfh.unicrypt.crypto.schemes.padding.classes.PKCSPaddingScheme;
import ch.bfh.unicrypt.crypto.schemes.padding.interfaces.ReversiblePaddingScheme;
import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.unicrypt.math.algebra.concatenative.classes.ByteArrayMonoid;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.univote2.common.crypto.KeyUtil;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.interfaces.DSAPublicKey;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class AlphaOneCreator {

	private static final String TENANT_CERTIFICATE = "../trustee-certificate.pem";
	private static final String TENANT_ENCRYPTED_PRIVATE_KEY = "../trustee-encrypted-private-key.pem";
	private static final String TENANT_PRIVATE_KEY_PASSWORD = "12345678";
	private static final String TENANT_NAME = "severin.hauser@bfh.ch";

	private static final String ENC_PRIVATE_KEY_PREFIX = "-----BEGIN ENCRYPTED UNICERT KEY-----";
	private static final String ENC_PRIVATE_KEY_POSTFIX = "-----END ENCRYPTED UNICERT KEY-----";

	private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
	private static final int KEY_SIZE = 128;
	private static final int ITERATIONS = 1000;

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) throws Exception {
		//Load public and private key
		DSAPublicKey posterPublicKey = KeyUtil.getDSAPublicKey(TENANT_CERTIFICATE);
		KeyUtil.getDSAPrivateKey(
				TENANT_ENCRYPTED_PRIVATE_KEY, TENANT_PRIVATE_KEY_PASSWORD, posterPublicKey.getParams());
		String encryptedKey = new String(Files.readAllBytes(Paths.get(TENANT_ENCRYPTED_PRIVATE_KEY)));
		byte[] aesKeyByte = AlphaOneCreator.getAESKey(encryptedKey, TENANT_PRIVATE_KEY_PASSWORD);

		AESEncryptionScheme aes = AESEncryptionScheme.getInstance();
		Element aesKey = aes.getEncryptionKeySpace().getElementFrom(ByteArray.getInstance(aesKeyByte));
		Element message = ByteArrayMonoid.getInstance().getElementFrom(BigInteger.ONE);
		ReversiblePaddingScheme pkcs = PKCSPaddingScheme.getInstance(16);
		Element paddedMessage = pkcs.pad(message);
		Element encBigIntElement = aes.encrypt(aesKey, paddedMessage);
		System.out.println(encBigIntElement.convertToBigInteger());

	}

	public static byte[] getAESKey(String privateKey, String password) throws Exception {
		String toDecrypt = privateKey.replace(ENC_PRIVATE_KEY_PREFIX, "");
		toDecrypt = toDecrypt.replace(ENC_PRIVATE_KEY_POSTFIX, "");
		toDecrypt = toDecrypt.replaceAll("\n", "");
		toDecrypt = toDecrypt.trim();

		byte[] salt = DatatypeConverter.parseBase64Binary(toDecrypt.substring(0, 24));
		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
		SecretKey secretKey = keyFactory.generateSecret(keySpec);

		return secretKey.getEncoded();
	}

}
