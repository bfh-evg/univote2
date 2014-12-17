/*
 * Copyright (c) 2014 Berner Fachhochschule, Switzerland.
 * Bern University of Applied Sciences, Engineering and Information Technology,
 * Research Institute for Security in the Information Society, E-Voting Group,
 * Biel, Switzerland.
 *
 * Project UniVote2.
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package ch.bfh.univote2.component.core.helper;

import ch.bfh.unicrypt.crypto.schemes.encryption.classes.AESEncryptionScheme;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import java.math.BigInteger;

public class AESEncryptionHelper implements EncryptionHelper {

	AESEncryptionScheme aes;
	Element key;

	public AESEncryptionHelper(String password) {
		aes = AESEncryptionScheme.getInstance();
		//key = aes.getEncryptionKeySpace().getElement(password);

	}

	@Override
	public BigInteger decryptBigInteger(BigInteger encBigInteger) {
		return aes.decrypt(key, aes.getMessageSpace().getElementFrom(encBigInteger)).getBigInteger();
	}

	@Override
	public BigInteger encryptBigInteger(BigInteger bigInteger) {
		return aes.encrypt(key, aes.getMessageSpace().getElementFrom(bigInteger)).getBigInteger();
	}

}
