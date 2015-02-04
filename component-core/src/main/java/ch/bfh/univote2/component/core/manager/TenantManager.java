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
import ch.bfh.univote2.component.core.UnivoteException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;
import javax.ejb.Local;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Local
public interface TenantManager {

	public boolean unlock(String tenant, String password) throws UnivoteException;

	public boolean lock(String tenant, String password) throws UnivoteException;

	public PublicKey getPublicKey(String tenant) throws UnivoteException;

	public PrivateKey getPrivateKey(String tenant) throws UnivoteException;

	public EncryptionHelper getEncrytpionHelper(String tenant) throws UnivoteException;

	public Set<String> getUnlockedTenants();

	public Set<String> getAllTentants();
}
