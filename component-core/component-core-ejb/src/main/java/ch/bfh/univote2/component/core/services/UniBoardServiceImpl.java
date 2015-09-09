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

import ch.bfh.uniboard.clientlib.BoardErrorException;
import ch.bfh.uniboard.clientlib.GetException;
import ch.bfh.uniboard.clientlib.GetHelper;
import ch.bfh.uniboard.clientlib.PostException;
import ch.bfh.uniboard.clientlib.PostHelper;
import ch.bfh.uniboard.clientlib.signaturehelper.SignatureException;
import ch.bfh.uniboard.data.AttributesDTO;
import ch.bfh.uniboard.data.QueryDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.univote2.common.UnivoteException;
import ch.bfh.univote2.component.core.data.UniBoard;
import ch.bfh.univote2.component.core.manager.TenantManager;
import ch.bfh.univote2.component.core.manager.UniBoardManager;
import javax.ejb.EJB;
import javax.ejb.Stateless;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@Stateless
public class UniBoardServiceImpl implements UniboardService {

	@EJB
	TenantManager tenantManager;

	@EJB
	UniBoardManager uniBoardManager;

	@Override
	public ResultContainerDTO get(String board, QueryDTO query) throws UnivoteException {
		try {
			UniBoard boardConfig = this.uniBoardManager.getUniBoard(board);
			GetHelper getHelper = new GetHelper(boardConfig.getPublicKey(), boardConfig.getWsdlURL(),
					boardConfig.getEndPointURL());
			return getHelper.get(query);
		} catch (GetException ex) {
			throw new UnivoteException("Could not create wsclient.", ex);
		} catch (SignatureException ex) {
			throw new UnivoteException("Could not verify answer.", ex);
		}

	}

	@Override
	public AttributesDTO post(String board, String section, String group, byte[] message, String tenant)
			throws UnivoteException {
		try {
			UniBoard boardConfig = this.uniBoardManager.getUniBoard(board);
			PostHelper postHelper = new PostHelper(this.tenantManager.getPublicKey(tenant),
					this.tenantManager.getPrivateKey(tenant), boardConfig.getPublicKey(), boardConfig.getWsdlURL(),
					boardConfig.getEndPointURL());
			return postHelper.post(message, section, group);
		} catch (PostException ex) {
			throw new UnivoteException("Could not create wsclient.", ex);
		} catch (SignatureException ex) {
			throw new UnivoteException("Could not sign message/Verify response.", ex);
		} catch (BoardErrorException ex) {
			throw new UnivoteException("Uniboard rejected the message", ex);
		}
	}

}
