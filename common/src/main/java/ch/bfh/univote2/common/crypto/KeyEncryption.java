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

import java.nio.charset.Charset;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

public class KeyEncryption {

	private static final String PRIVATE_KEY_PREFIX = "=====BEGIN_UNICERT_PRIVATE_KEY=====";
	private static final String PRIVATE_KEY_POSTFIX = "=====END_UNICERT_PRIVATE_KEY=====";
	private static final String ENC_PRIVATE_KEY_PREFIX = "-----BEGIN ENCRYPTED UNICERT KEY-----";
	private static final String ENC_PRIVATE_KEY_POSTFIX = "-----END ENCRYPTED UNICERT KEY-----";

	private static final String RANDOM_ALGORITHM = "SHA1PRNG";
	private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA1";
	private static final String TRANSFORM_ALGORITHM = "AES/CBC/PKCS5Padding";
	private static final int KEY_SIZE = 128;
	private static final int ITERATIONS = 1000;

	/**
	 * Encrypts a private key using a key derived from a password.
	 *
	 * @param privateKey the private key to encrypt
	 * @param password the password used to derive the encryption key
	 * @return the encrypted private key including pre- and postfix
	 */
	public static String encryptPrivateKey(byte[] privateKey, String password) throws Exception {
		String toEncrypt = PRIVATE_KEY_PREFIX + DatatypeConverter.printHexBinary(privateKey) + PRIVATE_KEY_POSTFIX;
		SecureRandom random = SecureRandom.getInstance(RANDOM_ALGORITHM);

		byte[] salt = random.generateSeed(16);
		PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
		SecretKey secretKey = keyFactory.generateSecret(spec);
		SecretKey keySpec = new SecretKeySpec(secretKey.getEncoded(), "AES");

		byte[] iv = random.generateSeed(16);
		Cipher cipher = Cipher.getInstance(TRANSFORM_ALGORITHM);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

		byte[] encrypted = cipher.doFinal(toEncrypt.getBytes("UTF-8"));
		return ENC_PRIVATE_KEY_PREFIX
				+ DatatypeConverter.printBase64Binary(salt)
				+ DatatypeConverter.printBase64Binary(iv)
				+ DatatypeConverter.printBase64Binary(encrypted)
				+ ENC_PRIVATE_KEY_POSTFIX;
	}

	/**
	 * Decrypt an encrypted private key with a key derived from the password.
	 *
	 * @param privateKey the encrypted private key including pre- and postfix
	 * @param password the password used to derive the encryption key
	 * @return the decrypted private key
	 */
	public static byte[] decryptPrivateKey(String privateKey, String password) throws Exception {
		String toDecrypt = privateKey.replace(ENC_PRIVATE_KEY_PREFIX, "");
		toDecrypt = toDecrypt.replace(ENC_PRIVATE_KEY_POSTFIX, "");
		toDecrypt = toDecrypt.replaceAll("\n", "");
		toDecrypt = toDecrypt.trim();

		byte[] salt = DatatypeConverter.parseBase64Binary(toDecrypt.substring(0, 24));
		byte[] iv = DatatypeConverter.parseBase64Binary(toDecrypt.substring(24, 48));
		byte[] encrypted = DatatypeConverter.parseBase64Binary(toDecrypt.substring(48));

		PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_SIZE);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
		SecretKey secretKey = keyFactory.generateSecret(keySpec);
		secretKey = new SecretKeySpec(secretKey.getEncoded(), "AES");

		Cipher cipher = Cipher.getInstance(TRANSFORM_ALGORITHM);
		IvParameterSpec ivSpec = new IvParameterSpec(iv);
		cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);

		String decrypted = new String(cipher.doFinal(encrypted), Charset.forName("UTF-8"));
		if (!decrypted.contains(PRIVATE_KEY_PREFIX) || !decrypted.contains(PRIVATE_KEY_POSTFIX)) {
			throw new RuntimeException("Wrong password");
		}
		decrypted = decrypted.replace(PRIVATE_KEY_PREFIX, "");
		decrypted = decrypted.replace(PRIVATE_KEY_POSTFIX, "");
		decrypted = decrypted.replace("\n", "");
		if (decrypted.length() % 2 == 1) {
			decrypted = "0" + decrypted;
		}
		return DatatypeConverter.parseHexBinary(decrypted);
	}
}
