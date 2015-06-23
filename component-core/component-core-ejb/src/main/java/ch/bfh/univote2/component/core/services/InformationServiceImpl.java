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

import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.persistence.TenantInformationEntity;
import ch.bfh.univote2.component.core.persistence.TenantInformationEntity_;
import java.util.List;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

@Singleton
public class InformationServiceImpl implements InformationService {

	@PersistenceContext(unitName = "ComponentPU")
	private EntityManager entityManager;

	@Override
	public void informTenant(String actionName, String tenant, String section, String information) {

		TenantInformationEntity informationEntity
				= new TenantInformationEntity(actionName, tenant, section, information);
		this.entityManager.persist(informationEntity);
	}

	@Override
	public void informTenant(ActionContextKey actionContextKey, String information) {
		this.informTenant(actionContextKey.getAction(), actionContextKey.getTenant(), actionContextKey.getSection(),
				information);
	}

	@Override
	public List<TenantInformationEntity> getTenantInforationEntities(String tenant, int limit) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TenantInformationEntity> query = builder.createQuery(TenantInformationEntity.class);
		Root<TenantInformationEntity> result = query.from(TenantInformationEntity.class);
		query.select(result);
		query.where(builder.equal(result.get(TenantInformationEntity_.tenant), tenant));
		query.orderBy(builder.desc(result.get(TenantInformationEntity_.timestamp)));
		List<TenantInformationEntity> informationEntities = entityManager.createQuery(query)
				.setMaxResults(limit).getResultList();
		return informationEntities;

	}

	@Override
	public List<TenantInformationEntity> getTenantInforationEntities(String tenant, int limit, int start) {
		CriteriaBuilder builder = entityManager.getCriteriaBuilder();
		CriteriaQuery<TenantInformationEntity> query = builder.createQuery(TenantInformationEntity.class);
		Root<TenantInformationEntity> result = query.from(TenantInformationEntity.class);
		query.select(result);
		query.where(builder.equal(result.get(TenantInformationEntity_.tenant), tenant));
		query.orderBy(builder.desc(result.get(TenantInformationEntity_.timestamp)));
		List<TenantInformationEntity> informationEntities = entityManager.createQuery(query)
				.setMaxResults(limit).setFirstResult(start).getResultList();
		return informationEntities;
	}
}
