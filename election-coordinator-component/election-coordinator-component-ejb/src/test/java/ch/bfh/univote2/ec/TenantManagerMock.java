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
package ch.bfh.univote2.ec;

import ch.bfh.unicrypt.helper.array.classes.ByteArray;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.manager.TenantManager;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Set;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Singleton
@LocalBean
public class TenantManagerMock implements TenantManager {

	private PublicKey publicKey;

	@Override
	public boolean checkLogin(String tenant, String password) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean unlock(String tenant, String password) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean lock(String tenant, String password) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public boolean isLocked(String tenant) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public PublicKey getPublicKey(String tenant) throws UnivoteException {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	@Override
	public PrivateKey getPrivateKey(String tenant) throws UnivoteException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public ByteArray getAESKey(String tenant) throws UnivoteException {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Set<String> getUnlockedTenants() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Set<String> getAllTentants() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
