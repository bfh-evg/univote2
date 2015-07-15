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

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.component.core.data.TimerPreconditionQuery;
import ch.bfh.univote2.ec.ActionManagerMock;
import ch.bfh.univote2.ec.InformationServiceMock;
import ch.bfh.univote2.ec.UniboardServiceMock;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.util.Date;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import sun.security.provider.DSAPublicKey;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@RunWith(Arquillian.class)
public class GrantEncryptionKeyShareAction2Test {

	public GrantEncryptionKeyShareAction2Test() {
	}

	/**
	 * Helper method for building the in-memory variant of a deployable unit. See Arquillian for more information.
	 *
	 * @return a Java archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive ja = ShrinkWrap.create(WebArchive.class)
				.addClass(TestableGrantEncryptionKeyShareAction2.class)
				.addClass(UniboardServiceMock.class)
				.addClass(InformationServiceMock.class)
				.addClass(ActionManagerMock.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		//System.out.println(ja.toString(true));
		return ja;
	}

	@EJB
	UniboardServiceMock uniboardServiceMock;

	@EJB
	TestableGrantEncryptionKeyShareAction2 grantEKSAction;

	@EJB
	ActionManagerMock actionManagerMock;

	/**
	 * Test prepareContext with no connection to the board
	 */
	@Test
	public void testPrepareContext1() throws InvalidKeyException {
		String tenant = "prepareContext1";
		String section = "section";
		this.grantEKSAction.setRetrieveTallierThrow(true);
		ActionContext ac = this.grantEKSAction.prepareContext(tenant, section);
		assertEquals(1, ac.getPreconditionQueries().size());
		assertFalse(ac.checkPostCondition());
	}

	/**
	 * Test prepareContext with all accessRights posted
	 */
	@Test
	public void testPrepareContext2() throws InvalidKeyException {
		String tenant = "prepareContext2";
		String section = "section";
		this.grantEKSAction.setRetrieveTallierThrow(false);
		this.grantEKSAction.setCheckAC(true);
		this.grantEKSAction.setCheckACThrow(false);
		ActionContext ac = this.grantEKSAction.prepareContext(tenant, section);
		assertEquals(0, ac.getPreconditionQueries().size());
		assertTrue(ac.checkPostCondition());
		GrantEncryptionKeyShareActionContext geksac = (GrantEncryptionKeyShareActionContext) ac;
		assertEquals(AccessRightStatus.GRANTED, geksac.getTalliers().get(0).getGranted());
	}

	/**
	 * Test prepareContext with no accessRights posted
	 */
	@Test
	public void testPrepareContext3() throws InvalidKeyException {
		String tenant = "prepareContext3";
		String section = "section";
		this.grantEKSAction.setRetrieveTallierThrow(false);
		this.grantEKSAction.setCheckAC(false);
		this.grantEKSAction.setCheckACThrow(false);
		ActionContext ac = this.grantEKSAction.prepareContext(tenant, section);
		assertEquals(0, ac.getPreconditionQueries().size());
		assertFalse(ac.checkPostCondition());
		GrantEncryptionKeyShareActionContext geksac = (GrantEncryptionKeyShareActionContext) ac;
		assertEquals(AccessRightStatus.NOT_GRANTED, geksac.getTalliers().get(0).getGranted());
	}

	/**
	 * Test prepareContext with exception while querying for status
	 */
	@Test
	public void testPrepareContext4() throws InvalidKeyException {
		String tenant = "prepareContext4";
		String section = "section";
		this.grantEKSAction.setRetrieveTallierThrow(false);
		this.grantEKSAction.setCheckAC(false);
		this.grantEKSAction.setCheckACThrow(true);
		ActionContext ac = this.grantEKSAction.prepareContext(tenant, section);
		assertEquals(0, ac.getPreconditionQueries().size());
		assertFalse(ac.checkPostCondition());
		GrantEncryptionKeyShareActionContext geksac = (GrantEncryptionKeyShareActionContext) ac;
		assertEquals(AccessRightStatus.UNKOWN, geksac.getTalliers().get(0).getGranted());
	}

	/**
	 * Test run with valid context and no preconditions
	 */
	@Test
	public void testRun1() throws InvalidKeyException {
		String tenant = "run1";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		ActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		this.grantEKSAction.run(actionContext);
		assertNull(this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test run with invalid context and no preconditions
	 */
	@Test
	public void testRun2() throws InvalidKeyException {
		String tenant = "run2";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		ActionContext actionContext = new ActionContext(ack, null, true) {

			@Override
			protected void purgeData() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
		this.grantEKSAction.run(actionContext);
		assertEquals(ResultStatus.FAILURE, this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test run with valid context and preconditions and no connection to the board
	 */
	@Test
	public void testRun3() throws InvalidKeyException {
		String tenant = "run3";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		//Just any precondition
		actionContext.getPreconditionQueries().add(new TimerPreconditionQuery(new Date()));
		this.grantEKSAction.setRetrieveTallierThrow(true);
		this.grantEKSAction.run(actionContext);
		assertEquals(ResultStatus.FAILURE, this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test run with valid context and preconditions and connection to the board
	 */
	@Test
	public void testRun4() throws InvalidKeyException {
		String tenant = "run4";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		//Just any precondition
		actionContext.getPreconditionQueries().add(new TimerPreconditionQuery(new Date()));
		this.grantEKSAction.setRetrieveTallierThrow(false);
		this.grantEKSAction.run(actionContext);
		assertNull(this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test notifyAction with invalid context
	 */
	@Test
	public void testNotifyAction1() throws InvalidKeyException {
		String tenant = "notifyAction1";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		ActionContext actionContext = new ActionContext(ack, null, true) {

			@Override
			protected void purgeData() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
		this.grantEKSAction.notifyAction(actionContext, null);
		assertEquals(ResultStatus.FAILURE, this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test notifyAction with valid context but no postdto
	 */
	@Test
	public void testNotifyAction2() throws InvalidKeyException {
		String tenant = "notifyAction2";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		this.grantEKSAction.notifyAction(actionContext, section);
		assertEquals(ResultStatus.FAILURE, this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test notifyAction with valid context and postdto but msg is no json
	 */
	@Test
	public void testNotifyAction3() throws InvalidKeyException {
		String tenant = "notifyAction3";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);

		PostDTO post = new PostDTO();
		post.setMessage("test".getBytes());
		this.grantEKSAction.notifyAction(actionContext, post);
		assertEquals(ResultStatus.FAILURE, this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test notifyAction with valid context and with json msg but content no valid
	 */
	@Test
	public void testNotifyAction4() throws InvalidKeyException {
		String tenant = "notifyAction4";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);

		PostDTO post = new PostDTO();
		post.setMessage("{\"test\":2}".getBytes());
		this.grantEKSAction.setParseTCThorw(true);
		this.grantEKSAction.notifyAction(actionContext, post);
		assertEquals(ResultStatus.FAILURE, this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test notifyAction with valid context and with json msg and content set valid
	 */
	@Test
	public void testNotifyAction5() throws InvalidKeyException {
		String tenant = "notifyAction5";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);

		PostDTO post = new PostDTO();
		post.setMessage("{\"test\":2}".getBytes());
		this.grantEKSAction.setParseTCThorw(false);
		this.grantEKSAction.notifyAction(actionContext, post);
		assertNull(this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test runInternal with all accessRight granted
	 */
	@Test
	public void testRunInternal1() throws InvalidKeyException {
		String tenant = "runInternal1";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		actionContext.getTalliers().add(new AccessRightCandidate(
				new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)));
		actionContext.getTalliers().get(0).setGranted(AccessRightStatus.GRANTED);
		this.grantEKSAction.runInternalPub(actionContext);
		assertEquals(ResultStatus.FINISHED, this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test runInternal with all accessRight not granted but granting works
	 */
	@Test
	public void testRunInternal2() throws InvalidKeyException {
		String tenant = "runInternal2";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		actionContext.getTalliers().add(new AccessRightCandidate(
				new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)));
		actionContext.getTalliers().get(0).setGranted(AccessRightStatus.NOT_GRANTED);
		this.grantEKSAction.setGrantAC(true);
		this.grantEKSAction.runInternalPub(actionContext);
		assertEquals(ResultStatus.FINISHED, this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test runInternal with all accessRight not granted and granting doesn't works
	 */
	@Test
	public void testRunInternal3() throws InvalidKeyException {
		String tenant = "runInternal3";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		actionContext.getTalliers().add(new AccessRightCandidate(
				new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)));
		actionContext.getTalliers().get(0).setGranted(AccessRightStatus.NOT_GRANTED);
		this.grantEKSAction.setGrantAC(false);
		this.grantEKSAction.runInternalPub(actionContext);
		assertEquals(ResultStatus.FAILURE, this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test runInternal with all accessRight retrieve works and ac granted
	 */
	@Test
	public void testRunInternal4() throws InvalidKeyException {
		String tenant = "runInternal4";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		actionContext.getTalliers().add(new AccessRightCandidate(
				new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)));
		actionContext.getTalliers().get(0).setGranted(AccessRightStatus.UNKOWN);
		this.grantEKSAction.setCheckAC(true);
		this.grantEKSAction.setCheckACThrow(false);
		this.grantEKSAction.runInternalPub(actionContext);
		assertEquals(ResultStatus.FINISHED, this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test runInternal with all accessRight unknown but retrieve works and ac not granted but granting works
	 */
	@Test
	public void testRunInternal5() throws InvalidKeyException {
		String tenant = "runInternal5";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		actionContext.getTalliers().add(new AccessRightCandidate(
				new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)));
		actionContext.getTalliers().get(0).setGranted(AccessRightStatus.UNKOWN);
		this.grantEKSAction.setCheckAC(false);
		this.grantEKSAction.setCheckACThrow(false);
		this.grantEKSAction.setGrantAC(true);
		this.grantEKSAction.runInternalPub(actionContext);
		assertEquals(ResultStatus.FINISHED, this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test runInternal with all accessRight unknown but retrieve works but ac not granted and granting doesnt work
	 */
	@Test
	public void testRunInternal6() throws InvalidKeyException {
		String tenant = "runInternal6";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		actionContext.getTalliers().add(new AccessRightCandidate(
				new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)));
		actionContext.getTalliers().get(0).setGranted(AccessRightStatus.UNKOWN);
		this.grantEKSAction.setCheckAC(false);
		this.grantEKSAction.setCheckACThrow(false);
		this.grantEKSAction.setGrantAC(false);
		this.grantEKSAction.runInternalPub(actionContext);
		assertEquals(ResultStatus.FAILURE, this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test runInternal with all accessRight unknown and retrieve doesnt work
	 */
	@Test
	public void testRunInternal7() throws InvalidKeyException {
		String tenant = "runInternal7";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		GrantEncryptionKeyShareActionContext actionContext = new GrantEncryptionKeyShareActionContext(ack);
		actionContext.getTalliers().add(new AccessRightCandidate(
				new DSAPublicKey(BigInteger.ONE, BigInteger.ONE, BigInteger.ONE, BigInteger.ONE)));
		actionContext.getTalliers().get(0).setGranted(AccessRightStatus.UNKOWN);
		this.grantEKSAction.setCheckAC(true);
		this.grantEKSAction.setCheckACThrow(true);
		this.grantEKSAction.runInternalPub(actionContext);
		assertEquals(ResultStatus.FAILURE, this.actionManagerMock.getResultStatus());
	}
}
