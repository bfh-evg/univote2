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
package ch.bfh.univote2.ec.grantEKS;

import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import javax.ejb.LocalBean;
import javax.ejb.Singleton;
import javax.json.JsonObject;
import sun.security.provider.DSAPublicKey;

/**
 * To test the protected methods
 *
 */
@Singleton
@LocalBean
public class TestableGrantEncryptionKeyShareAction2 extends GrantEncryptionKeyShareAction {

	private boolean grantAC = false;
	private boolean checkAC = false;
	private boolean checkACThrow = false;
	private boolean parseTCThrow = false;
	private boolean retrieveTallierThrow = false;

	@Override
	protected boolean grantAccessRight(ActionContext actionContext, PublicKey publickey) {
		return grantAC;
	}

	@Override
	protected void runInternal(GrantEncryptionKeyShareActionContext actionContext) {
	}

	public void runInternalPub(GrantEncryptionKeyShareActionContext actionContext) {
		super.runInternal(actionContext);
	}

	@Override
	protected boolean checkAccessRight(ActionContext actionContext, PublicKey publicKey) throws UnivoteException {
		if (checkACThrow) {
			throw new UnivoteException("Test");
		}
		return checkAC;
	}

	@Override
	protected void parseTrusteeCerts(JsonObject message, GrantEncryptionKeyShareActionContext actionContext) throws UnivoteException {
		if (parseTCThrow) {
			throw new UnivoteException("Test");
		}
	}

	@Override
	protected void retrieveTalliers(GrantEncryptionKeyShareActionContext actionContext) throws UnivoteException {
		try {
			actionContext.getTalliers().add(new AccessRightCandidate(new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)));
		} catch (InvalidKeyException ex) {

		}
		if (retrieveTallierThrow) {
			throw new UnivoteException("Test");
		}
	}

	public void setGrantAC(boolean grantAC) {
		this.grantAC = grantAC;
	}

	public void setCheckAC(boolean checkAC) {
		this.checkAC = checkAC;
	}

	public void setCheckACThrow(boolean checkACThrow) {
		this.checkACThrow = checkACThrow;
	}

	public void setParseTCThorw(boolean parseTCThorw) {
		this.parseTCThrow = parseTCThorw;
	}

	public void setRetrieveTallierThrow(boolean retrieveTallierThrow) {
		this.retrieveTallierThrow = retrieveTallierThrow;
	}

	@Override
	public void notifyAction(ActionContext actionContext, Object notification) {
		super.notifyAction(actionContext, notification);
	}

	@Override
	public void run(ActionContext actionContext) {
		super.run(actionContext);
	}

	@Override
	public ActionContext prepareContext(String tenant, String section) {
		return super.prepareContext(tenant, section);
	}

}
