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
package ch.bfh.univote2.trustee.tallier.sharedKeyCreation;

import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.message.CryptoSetting;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Reto E. Koenig <reto.koenig@bfh.ch>
 */
public class SharedKeyCreationActionContextTest {

	public SharedKeyCreationActionContextTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of purgeData method, of class SharedKeyCreationActionContext.
	 */
	@Ignore
	public void testPurgeData() {
		System.out.println("purgeData");
		ActionContextKey ack = new ActionContextKey("X", "Y", "Z");
		SharedKeyCreationActionContext instance = new SharedKeyCreationActionContext(ack);
		instance.setAccessRightGranted(Boolean.TRUE);
		instance.setCryptoSetting(new CryptoSetting("a", "b", "c"));
		Assert.assertEquals(instance.getCryptoSetting(), null);
		Assert.assertEquals(null, instance.getAccessRightGranted());

	}

	/**
	 * Test of getAccessRightGranted method, of class SharedKeyCreationActionContext.
	 */
	@Test
	public void testGetAccessRightGranted() {
		System.out.println("getAccessRightGranted");
		testSetAccessRightGranted();

	}

	/**
	 * Test of setAccessRightGranted method, of class SharedKeyCreationActionContext.
	 */
	@Test
	public void testSetAccessRightGranted() {
		System.out.println("setAccessRightGranted");
		ActionContextKey ack = new ActionContextKey("X", "Y", "Z");
		SharedKeyCreationActionContext instance = new SharedKeyCreationActionContext(ack);
		Assert.assertEquals(null, instance.getAccessRightGranted());
		instance.setAccessRightGranted(Boolean.FALSE);
		Assert.assertEquals(Boolean.FALSE, instance.getAccessRightGranted());
		instance.setAccessRightGranted(Boolean.TRUE);
		Assert.assertEquals(Boolean.TRUE, instance.getAccessRightGranted());
	}

	/**
	 * Test of getCryptoSetting method, of class SharedKeyCreationActionContext.
	 */
	@Test
	public void testGetCryptoSetting() {
		System.out.println("getCryptoSetting");
		testSetCryptoSetting();
	}

	/**
	 * Test of setCryptoSetting method, of class SharedKeyCreationActionContext.
	 */
	@Test
	public void testSetCryptoSetting() {
		System.out.println("setCryptoSetting");
		ActionContextKey ack = new ActionContextKey("X", "Y", "Z");
		SharedKeyCreationActionContext instance = new SharedKeyCreationActionContext(ack);
		Assert.assertEquals(null, instance.getCryptoSetting());
		CryptoSetting cryptoSetting = new CryptoSetting("a", "b", "c");
		instance.setCryptoSetting(cryptoSetting);
		Assert.assertEquals(cryptoSetting, instance.getCryptoSetting());
	}

	/**
	 * Test of isPreconditionReached method, of class SharedKeyCreationActionContext.
	 */
	@Ignore
	public void testIsPreconditionReached() {
		ActionContextKey ack = new ActionContextKey("X", "Y", "Z");
		SharedKeyCreationActionContext instance = new SharedKeyCreationActionContext(ack);
		Assert.assertEquals(null, instance.isPreconditionReached());
		instance.setAccessRightGranted(Boolean.TRUE);
		Assert.assertEquals(null, instance.isPreconditionReached());
		instance.setCryptoSetting(new CryptoSetting("a", "b", "c"));
		Assert.assertEquals(Boolean.TRUE, instance.isPreconditionReached());
		instance.setAccessRightGranted(Boolean.FALSE);
		Assert.assertEquals(Boolean.FALSE, instance.isPreconditionReached());
		Assert.assertEquals(null, instance.isPreconditionReached());
	}

}
