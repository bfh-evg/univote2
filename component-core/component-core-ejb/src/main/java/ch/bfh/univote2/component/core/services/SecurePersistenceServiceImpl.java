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
package ch.bfh.univote2.component.core.services;

import ch.bfh.unicrypt.crypto.schemes.encryption.classes.AESEncryptionScheme;
import ch.bfh.unicrypt.math.algebra.general.interfaces.Element;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.persistence.EncryptedBigIntEntity;
import ch.bfh.univote2.component.core.persistence.EncryptedBigIntEntity_;
import java.math.BigInteger;
import javax.ejb.EJB;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class SecurePersistenceServiceImpl implements SecurePersistenceService {

	@PersistenceContext(unitName = "ComponentPU")
	private EntityManager entityManager;

	@EJB
	private TenantManager tenantManager;

	@Override
	public void persist(String tenant, String section, String type, BigInteger value) throws UnivoteException {
		EncryptedBigIntEntity encBigInt;
		try {
			encBigInt = this.getEncryptedBigInteger(tenant, section, type);

		} catch (NoResultException ex) {
			encBigInt = new EncryptedBigIntEntity();
			encBigInt.setTenant(tenant);
			encBigInt.setSection(section);
			encBigInt.setType(type);
		} catch (NonUniqueResultException ex) {
			throw new UnivoteException("Multiple values stored for this tenant " + tenant + ", section " + section
					+ ", type " + type + ".", ex);
		}
		AESEncryptionScheme aes = AESEncryptionScheme.getInstance();
		Element aesKey = aes.getEncryptionKeySpace().getElement(this.tenantManager.getAESKey(tenant));
		Element bigInt = aes.getMessageSpace().getElementFrom(value);
		Element encBigIntElement = aes.encrypt(aesKey, bigInt);
		encBigInt.setBigInteger(encBigIntElement.getBigInteger());
		this.persist(encBigInt);
	}

	@Override
	public BigInteger retrieve(String tenant, String section, String type) throws UnivoteException {
		EncryptedBigIntEntity encBigIntEntity;
		try {
			encBigIntEntity = this.getEncryptedBigInteger(tenant, section, type);
		} catch (NoResultException | NonUniqueResultException ex) {
			throw new UnivoteException("Unknown entry.");
		}
		AESEncryptionScheme aes = AESEncryptionScheme.getInstance();
		Element aesKey = aes.getEncryptionKeySpace().getElement(this.tenantManager.getAESKey(tenant));
		Element encBigInt = aes.getMessageSpace().getElementFrom(encBigIntEntity.getBigInteger());
		Element bigInt = aes.decrypt(aesKey, encBigInt);
		return bigInt.getBigInteger();
	}

	protected EncryptedBigIntEntity getEncryptedBigInteger(String tenant, String section, String type)
			throws NonUniqueResultException, NoResultException {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<EncryptedBigIntEntity> query = builder.createQuery(EncryptedBigIntEntity.class);
		Root<EncryptedBigIntEntity> result = query.from(EncryptedBigIntEntity.class);
		query.select(result);
		query.where(builder.equal(result.get(EncryptedBigIntEntity_.tenant), tenant),
				builder.equal(result.get(EncryptedBigIntEntity_.sectionName), section),
				builder.equal(result.get(EncryptedBigIntEntity_.type), type));
		return entityManager.createQuery(query).getSingleResult();
	}

	protected void persist(EncryptedBigIntEntity encBigIntEntity) {
		entityManager.persist(encBigIntEntity);
	}

	void setTenantManager(TenantManager tenantManager) {
		this.tenantManager = tenantManager;
	}

}
