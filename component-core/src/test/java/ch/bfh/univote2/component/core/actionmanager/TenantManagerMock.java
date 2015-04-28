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
package ch.bfh.univote2.component.core.actionmanager;

import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.services.SecurePersistenceService;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;
import javax.ejb.Singleton;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
public class TenantManagerMock implements TenantManager {

	@Override
	public boolean unlock(String tenant, String password) throws UnivoteException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean lock(String tenant, String password) throws UnivoteException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public PublicKey getPublicKey(String tenant) throws UnivoteException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public PrivateKey getPrivateKey(String tenant) throws UnivoteException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public SecurePersistenceService getEncrytpionHelper(String tenant) throws UnivoteException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Set<String> getUnlockedTenants() {
		return new HashSet<>();
	}

	@Override
	public Set<String> getAllTentants() {
		return new HashSet<>();
	}

}
