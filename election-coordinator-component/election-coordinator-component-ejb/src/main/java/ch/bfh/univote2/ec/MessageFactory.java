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

import ch.bfh.unicrypt.helper.MathUtil;
import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.query.GroupEnum;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.security.interfaces.DSAPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class MessageFactory {

	public static byte[] createAccessRight(GroupEnum group, PublicKey publicKey, Integer amount, Date startTime,
			Date endTime) throws UnivoteException {

		String message = "{";
		message += "\"group\":\"" + group.getValue() + "\",";
		if (amount != null) {
			message += "\"amount\": " + amount + ",";
		}
		SimpleDateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
		iso8601.setTimeZone(TimeZone.getTimeZone("GMT"));
		if (startTime != null) {
			message += "\"startTime\":\"" + iso8601.format(startTime) + "\",";
		}
		if (endTime != null) {
			message += "\"endTime\":\"" + iso8601.format(endTime) + "\",";
		}
		if (publicKey instanceof DSAPublicKey) {
			DSAPublicKey dsaPubKey = (DSAPublicKey) publicKey;
			message += "\"crypto\":{\"type\":\"DL\", \"p\":\""
					+ dsaPubKey.getParams().getP().toString(10)
					+ "\",\"q\":\"" + dsaPubKey.getParams().getQ().toString(10)
					+ "\",\"g\":\"" + dsaPubKey.getParams().getG().toString(10)
					+ "\",\"publickey\":\""
					+ dsaPubKey.getY().toString(10) + "\"}";
		} else if (publicKey instanceof RSAPublicKey) {
			RSAPublicKey rsaPubKey = (RSAPublicKey) publicKey;
			BigInteger unicertRsaPubKey = MathUtil.pair(rsaPubKey.getPublicExponent(), rsaPubKey.getModulus());
			//Create correct json message
			message += "\"crypto\":{\"type\":\"RSA\",\"publickey\":\""
					+ unicertRsaPubKey.toString(10) + "\"}";
		} else {
			throw new UnivoteException("Unsupported public key: " + publicKey.getClass());
		}
		message += "}";
		return message.getBytes(Charset.forName("UTF-8"));
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
}
