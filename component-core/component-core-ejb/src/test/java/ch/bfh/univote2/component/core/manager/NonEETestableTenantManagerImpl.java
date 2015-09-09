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

import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.component.core.manager.TenantManagerImpl;
import ch.bfh.univote2.component.core.manager.UnlockedTenant;
import ch.bfh.univote2.component.core.persistence.TenantEntity;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class NonEETestableTenantManagerImpl extends TenantManagerImpl {

	TenantEntity entity;

	@Override
	protected TenantEntity getTenant(String tenant) throws UnivoteException {

		return entity;
	}

	public void addToUnlocked(String tenant, UnlockedTenant unlockedTenant) {
		super.unlockedTentants.put(tenant, unlockedTenant);
	}

	public void setTenantEntity(TenantEntity entity) {
		this.entity = entity;
	}

}
