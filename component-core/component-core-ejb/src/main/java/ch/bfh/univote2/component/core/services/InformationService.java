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
import java.util.List;
import javax.ejb.Local;

/**
 * Allows actions to display information about their run to the corresponding tenant
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Local
public interface InformationService {

	/**
	 * Shows the provided information to the corresponding tenant
	 *
	 * @param actionName - name of the action providing the information(caller of the method)
	 * @param tenant - tenant the information is for
	 * @param section - section the information belongs to
	 * @param information - information to be shown to the tenant
	 */
	public void informTenant(String actionName, String tenant, String section, String information);

	/**
	 * Shows the provided information to the corresponding tenant
	 *
	 * @param actionContextKey context key containing actionName, tenant, and section
	 * @param information - information to be shown to the tenant
	 */
	public void informTenant(ActionContextKey actionContextKey, String information);

	/**
	 * Returns all the last received TenantInformationEntities limited by limit
	 *
	 * @param tenant the information only for this tenant is returned
	 * @param limit limits the amount of information entities.
	 * @return
	 */
	public List<TenantInformationEntity> getTenantInforationEntities(String tenant, int limit);

	/**
	 * Returns all the last received TenantInformationEntities limited by limit starting at start
	 *
	 * @param tenant the information only for this tenant is returned
	 * @param limit limits the amount of information entities.
	 * @param start defines the first entry returned
	 * @return
	 */
	public List<TenantInformationEntity> getTenantInforationEntities(String tenant, int limit, int start);
}
