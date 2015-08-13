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

import ch.bfh.unicrypt.helper.math.MathUtil;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.message.AccessRight;
import ch.bfh.univote2.component.core.message.DL;
import ch.bfh.univote2.component.core.message.JSONConverter;
import ch.bfh.univote2.component.core.message.RSA;
import ch.bfh.univote2.component.core.query.GroupEnum;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.List;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class MessageFactory {

	public static byte[] createAccessRight(GroupEnum group, PublicKey publicKey, Integer amount, Date startTime,
			Date endTime) throws UnivoteException {

		AccessRight accessRight = new AccessRight();
		accessRight.setGroup(group.getValue());
		if (amount != null) {
			accessRight.setAmount(amount);
		}
		if (startTime != null) {
			accessRight.setStartTime(startTime);
		}
		if (endTime != null) {
			accessRight.setEndTime(endTime);
		}
		if (publicKey instanceof DSAPublicKey) {
			DSAPublicKey dsaPubKey = (DSAPublicKey) publicKey;
			DL dl = new DL();
			dl.setP(dsaPubKey.getParams().getP().toString(10));
			dl.setQ(dsaPubKey.getParams().getQ().toString(10));
			dl.setG(dsaPubKey.getParams().getG().toString(10));
			dl.setPublickey(dsaPubKey.getY().toString(10));
			accessRight.setCrypto(dl);
		} else if (publicKey instanceof RSAPublicKey) {
			RSAPublicKey rsaPubKey = (RSAPublicKey) publicKey;
			BigInteger unicertRsaPubKey = MathUtil.pair(rsaPubKey.getPublicExponent(), rsaPubKey.getModulus());

			RSA rsa = new RSA(unicertRsaPubKey.toString(10));
			accessRight.setCrypto(rsa);
		} else {
			throw new UnivoteException("Unsupported public key: " + publicKey.getClass());
		}
		try {
			return JSONConverter.marshal(accessRight).getBytes(Charset.forName("UTF-8"));
		} catch (Exception ex) {
			return null;
		}
	}

	public static byte[] createAccessRight(GroupEnum group, PublicKey publicKey, Integer amount)
			throws UnivoteException {
		return MessageFactory.createAccessRight(group, publicKey, amount, null, null);
	}

	public static byte[] createAccessRight(GroupEnum group, PublicKey publicKey)
			throws UnivoteException {
		return MessageFactory.createAccessRight(group, publicKey, null, null, null);
	}

	public static byte[] createAccessRight(GroupEnum group, PublicKey publicKey, Date startTime, Date endTime)
			throws UnivoteException {
		return MessageFactory.createAccessRight(group, publicKey, null, startTime, endTime);
	}

	public static byte[] createTrusteeCerts(List<String> mixerCerts, List<String> tallierCerts) {
		String message = "{";

		message += "\"mixerCertificates\" : [";
		for (String mixerCert : mixerCerts) {
			message += mixerCert + ", ";
		}
		message = message.substring(0, message.length() - 2);
		message += "], ";
		message += "\"tallierCertificates\" : [";
		for (String tallierCert : tallierCerts) {
			message += tallierCert + ", ";
		}
		message = message.substring(0, message.length() - 2);
		message += "]}";
		return message.getBytes(Charset.forName("UTF-8"));
	}
}
