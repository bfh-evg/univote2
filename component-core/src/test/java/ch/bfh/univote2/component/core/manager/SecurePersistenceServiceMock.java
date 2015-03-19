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

import ch.bfh.univote2.component.core.services.SecurePersistenceService;
import java.math.BigInteger;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class SecurePersistenceServiceMock implements SecurePersistenceService {

	@Override
	public void securePersist(String tenant, String section, Class type, BigInteger value) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public BigInteger secureRetrieve(String tenant, String section, Class type) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
