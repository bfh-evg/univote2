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

import ch.bfh.univote2.component.core.UnivoteException;
import ch.bfh.univote2.component.core.query.GroupEnum;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import static org.junit.Assert.fail;
import org.junit.Test;
import sun.security.provider.DSAPublicKey;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
public class MessageFactoryTest {

	public MessageFactoryTest() {
	}

	@Test
	public void testCreateAccessRight_5args() throws InvalidKeyException {
		PublicKey publicKey = new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
		GroupEnum group = GroupEnum.ACCESS_RIGHT;
		Integer amount = 1;
		Date startTime = new Date(new Long("1435242934856"));
		Date endTime = new Date(new Long("1435242994846"));

		String expectedMessageStr = "{\"group\":\"accessRight\","
				+ "\"crypto\":{\"type\":\"DL\",\"publickey\":\"1\",\"p\":\"1\",\"q\":\"1\",\"g\":\"1\"},"
				+ "\"amount\":1,\"startTime\":\"2015-06-25T14:35:34Z\","
				+ "\"endTime\":\"2015-06-25T14:36:34Z\""
				+ "}";
		try {
			byte[] message = MessageFactory.createAccessRight(group, publicKey, amount, startTime, endTime);
			Assert.assertArrayEquals(message, expectedMessageStr.getBytes(Charset.forName("UTF-8")));
		} catch (UnivoteException ex) {
			fail();
		}

	}

	@Test
	public void testCreateAccessRight_3args() throws Exception {
		PublicKey publicKey = new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
		GroupEnum group = GroupEnum.ACCESS_RIGHT;
		Integer amount = 1;
		Date startTime = new Date(new Long("1435242934856"));
		Date endTime = new Date(new Long("1435242994846"));

		String expectedMessageStr = "{\"group\":\"accessRight\","
				+ "\"crypto\":{\"type\":\"DL\",\"publickey\":\"1\",\"p\":\"1\",\"q\":\"1\",\"g\":\"1\"}"
				+ ",\"amount\":1}";
		try {
			byte[] message = MessageFactory.createAccessRight(group, publicKey, amount);
			Assert.assertArrayEquals(message, expectedMessageStr.getBytes(Charset.forName("UTF-8")));
		} catch (UnivoteException ex) {
			fail();
		}
	}

	@Test
	public void testCreateAccessRight_GroupEnum_PublicKey() throws Exception {
		PublicKey publicKey = new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
		GroupEnum group = GroupEnum.ACCESS_RIGHT;
		Integer amount = 1;
		Date startTime = new Date(new Long("1435242934856"));
		Date endTime = new Date(new Long("1435242994846"));

		String expectedMessageStr = "{\"group\":\"accessRight\","
				+ "\"crypto\":{\"type\":\"DL\",\"publickey\":\"1\",\"p\":\"1\",\"q\":\"1\",\"g\":\"1\"}}";
		try {
			byte[] message = MessageFactory.createAccessRight(group, publicKey);
			Assert.assertArrayEquals(message, expectedMessageStr.getBytes(Charset.forName("UTF-8")));
		} catch (UnivoteException ex) {
			fail();
		}
	}

	@Test
	public void testCreateAccessRight_4args() throws Exception {
		PublicKey publicKey = new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE);
		GroupEnum group = GroupEnum.ACCESS_RIGHT;
		Integer amount = 1;
		Date startTime = new Date(new Long("1435242934856"));
		Date endTime = new Date(new Long("1435242994846"));

		String expectedMessageStr = "{\"group\":\"accessRight\","
				+ "\"crypto\":{\"type\":\"DL\",\"publickey\":\"1\",\"p\":\"1\",\"q\":\"1\",\"g\":\"1\"},"
				+ "\"startTime\":\"2015-06-25T14:35:34Z\","
				+ "\"endTime\":\"2015-06-25T14:36:34Z\"}";
		try {
			byte[] message = MessageFactory.createAccessRight(group, publicKey, startTime, endTime);
			Assert.assertArrayEquals(message, expectedMessageStr.getBytes(Charset.forName("UTF-8")));
		} catch (UnivoteException ex) {
			fail();
		}
	}

	@Test
	public void testcreateTrusteeCerts() {

		List<String> mixerCerts = new ArrayList<>();
		mixerCerts.add("mixer1Cert");
		mixerCerts.add("mixer2Cert");
		mixerCerts.add("mixer3Cert");
		List<String> tallierCerts = new ArrayList<>();
		tallierCerts.add("tallier1Cert");
		tallierCerts.add("tallier2Cert");
		String expectedMessageStr = "{\"mixerCertificates\" : [mixer1Cert, mixer2Cert, mixer3Cert],"
				+ " \"tallierCertificates\" : [tallier1Cert, tallier2Cert]}";
		byte[] message = MessageFactory.createTrusteeCerts(mixerCerts, tallierCerts);
		Assert.assertArrayEquals(message, expectedMessageStr.getBytes(Charset.forName("UTF-8")));
	}

}
