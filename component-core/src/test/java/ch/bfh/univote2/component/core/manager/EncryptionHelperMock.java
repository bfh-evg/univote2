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
package ch.bfh.univote2.component.core.manager;

import ch.bfh.univote2.component.core.helper.EncryptionHelper;
import java.math.BigInteger;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class EncryptionHelperMock implements EncryptionHelper {

	@Override
	public BigInteger decryptBigInteger(BigInteger encBigInteger) {
		return encBigInteger;
	}

	@Override
	public BigInteger encryptBigInteger(BigInteger encBigInteger) {
		return encBigInteger;
	}

}
