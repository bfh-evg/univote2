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
package ch.bfh.univote2.ec.pubTC;

import ch.bfh.univote2.ec.UniboardServiceMock;
import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultDTO;
import ch.bfh.univote2.component.core.action.NotifiableAction;
import ch.bfh.univote2.component.core.actionmanager.ActionContext;
import ch.bfh.univote2.component.core.actionmanager.ActionContextKey;
import ch.bfh.univote2.component.core.data.ResultStatus;
import ch.bfh.univote2.ec.ActionManagerMock;
import ch.bfh.univote2.ec.InformationServiceMock;
import java.nio.charset.Charset;
import javax.ejb.EJB;
import static junit.framework.Assert.assertEquals;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Severin Hauser &lt;severin.hauser@bfh.ch&gt;
 */
@RunWith(Arquillian.class)
public class PublishTrusteeCertsActionTest {

	public PublishTrusteeCertsActionTest() {
	}

	/**
	 * Helper method for building the in-memory variant of a deployable unit. See Arquillian for more information.
	 *
	 * @return a Java archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive ja = ShrinkWrap.create(WebArchive.class)
				.addClass(SyncPublishTrusteeCertsAction.class)
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
	NotifiableAction publishTrusteeCertsAction;

	@EJB
	ActionManagerMock actionManagerMock;

	/**
	 * Test of prepareContext with trusteesCerts published
	 *
	 * @throws Exception
	 */
	@Test
	public void testPrepareContext1() throws Exception {
		String tenant = "testPrepareContext1";
		String section = "section";
		ResultDTO response1 = new ResultDTO();
		response1.getPost().add(new PostDTO());
		this.uniboardServiceMock.addResponse(response1);
		ActionContext context = this.publishTrusteeCertsAction.prepareContext(tenant, section);
		assertTrue(context.checkPostCondition());
	}

	/**
	 * Test of prepareContext with trusteesCerts not published but trustees published
	 *
	 * @throws Exception
	 */
	@Test
	public void testPrepareContext2() throws Exception {
		String tenant = "testPrepareContext2";
		String section = "section";
		ResultDTO response1 = new ResultDTO();
		this.uniboardServiceMock.addResponse(response1);
		ResultDTO response2 = new ResultDTO();
		String msg = "{"
				+ "	\"mixerIds\": [\"mixer1\", \"mixer2\", \"mixer3\"],"
				+ "	\"tallierIds\": [\"tallier1\", \"tallier2\", \"tallier3\"]"
				+ "}";
		byte[] message = msg.getBytes(Charset.forName("UTF-8"));
		response2.getPost().add(new PostDTO(message, null, null));
		this.uniboardServiceMock.addResponse(response2);
		ActionContext context = this.publishTrusteeCertsAction.prepareContext(tenant, section);
		assertFalse(context.checkPostCondition());
		assertTrue(context instanceof PublishTrusteeCertsActionContext);
		PublishTrusteeCertsActionContext ptcac = (PublishTrusteeCertsActionContext) context;
		assertEquals(3, ptcac.getMixers().size());
		assertTrue(ptcac.getMixers().contains("mixer1"));
		assertTrue(ptcac.getMixers().contains("mixer2"));
		assertTrue(ptcac.getMixers().contains("mixer3"));
		assertEquals(3, ptcac.getTalliers().size());
		assertTrue(ptcac.getTalliers().contains("tallier1"));
		assertTrue(ptcac.getTalliers().contains("tallier2"));
		assertTrue(ptcac.getTalliers().contains("tallier3"));
	}

	/**
	 * Test of prepareContext with trusteesCerts and trustees not published
	 *
	 * @throws Exception
	 */
	@Test
	public void testPrepareContext3() throws Exception {
		String tenant = "testPrepareContext3";
		String section = "section";
		ResultDTO response1 = new ResultDTO();
		this.uniboardServiceMock.addResponse(response1);
		ResultDTO response2 = new ResultDTO();
		this.uniboardServiceMock.addResponse(response2);
		ActionContext context = this.publishTrusteeCertsAction.prepareContext(tenant, section);
		assertFalse(context.checkPostCondition());
		assertTrue(context instanceof PublishTrusteeCertsActionContext);
		PublishTrusteeCertsActionContext ptcac = (PublishTrusteeCertsActionContext) context;
		assertEquals(0, ptcac.getMixers().size());
		assertEquals(0, ptcac.getTalliers().size());
	}

	/**
	 * Test run with trustees in context not set and not available
	 *
	 * @throws Exception
	 */
	@Test
	public void testRun1() throws Exception {
		String tenant = "testRun1";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		PublishTrusteeCertsActionContext context = new PublishTrusteeCertsActionContext(ack, null);
		ResultDTO response1 = new ResultDTO();
		this.uniboardServiceMock.addResponse(response1);
		this.publishTrusteeCertsAction.run(context);
		assertEquals((ResultStatus.FAILURE), this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test run with trustees in context not set but available
	 *
	 * @throws Exception
	 */
	@Test
	public void testRun2() throws Exception {
		String tenant = "testRun2";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		PublishTrusteeCertsActionContext context = new PublishTrusteeCertsActionContext(ack, null);
		ResultDTO response1 = new ResultDTO();
		String msg = "{"
				+ "	\"mixerIds\": [\"mixer1\"],"
				+ "	\"tallierIds\": [\"tallier1\"]"
				+ "}";
		byte[] message = msg.getBytes(Charset.forName("UTF-8"));
		response1.getPost().add(new PostDTO(message, null, null));
		this.uniboardServiceMock.addResponse(response1);
		ResultDTO response2 = new ResultDTO();
		String msg2 = "{"
				+ "	\"certContent\": [\"mixer1\"]"
				+ "}";
		byte[] message2 = msg2.getBytes(Charset.forName("UTF-8"));
		response2.getPost().add(new PostDTO(message2, null, null));
		this.uniboardServiceMock.addResponse(response2);
		ResultDTO response3 = new ResultDTO();
		String msg3 = "{"
				+ "	\"certContent\": [\"tallier1\"],"
				+ "}";
		byte[] message3 = msg3.getBytes(Charset.forName("UTF-8"));
		response3.getPost().add(new PostDTO(message3, null, null));
		this.uniboardServiceMock.addResponse(response3);
		this.publishTrusteeCertsAction.run(context);
		assertEquals((ResultStatus.FINISHED), this.actionManagerMock.getResultStatus());
		PostDTO post = this.uniboardServiceMock.getPost();
		Assert.assertArrayEquals("{\"mixerCertificates\" : [{	\"certContent\": [\"mixer1\"]}], \"tallierCertificates\" : [{	\"certContent\": [\"tallier1\"],}]}".getBytes(), post.getMessage());
	}

	/**
	 * Test run with trustees in context not set but an corrupt post available
	 *
	 * @throws Exception
	 */
	@Test
	public void testRun3() throws Exception {
		String tenant = "testRun3";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		PublishTrusteeCertsActionContext context = new PublishTrusteeCertsActionContext(ack, null);
		ResultDTO response1 = new ResultDTO();
		String msg = "{"
				+ "	\"mixerIDs\": [\"mixer1\"],"
				+ "	\"tallierIDs\": [\"tallier1\"]"
				+ "}";
		byte[] message = msg.getBytes(Charset.forName("UTF-8"));
		response1.getPost().add(new PostDTO(message, null, null));
		this.uniboardServiceMock.addResponse(response1);
		this.publishTrusteeCertsAction.run(context);
		assertEquals((ResultStatus.FAILURE), this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test notifiyAction with an unsupported Context
	 *
	 * @throws Exception
	 */
	@Test
	public void testNotifyAction1() throws Exception {
		String tenant = "testNotifyAction1";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		ActionContext context = new ActionContext(ack, null, true) {

			@Override
			protected void purgeData() {
				throw new UnsupportedOperationException("Not supported yet.");
			}
		};
		this.publishTrusteeCertsAction.notifyAction(context, section);
		assertEquals((ResultStatus.FAILURE), this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test notifiyAction with invalid Notification Object
	 *
	 * @throws Exception
	 */
	@Test
	public void testNotifyAction2() throws Exception {
		String tenant = "testNotifyAction2";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		PublishTrusteeCertsActionContext context = new PublishTrusteeCertsActionContext(ack, null);
		this.publishTrusteeCertsAction.notifyAction(context, section);
		assertEquals((ResultStatus.FAILURE), this.actionManagerMock.getResultStatus());
	}

	/**
	 * Test notifiyAction with a valid PostDTO
	 *
	 * @throws Exception
	 */
	@Test
	public void testNotifyAction3() throws Exception {
		String tenant = "testNotifyAction3";
		String section = "section";
		ActionContextKey ack = new ActionContextKey("Test", tenant, section);
		PublishTrusteeCertsActionContext context = new PublishTrusteeCertsActionContext(ack, null);
		String msg = "{"
				+ "	\"mixerIds\": [\"mixer1\"],"
				+ "	\"tallierIds\": [\"tallier1\"]"
				+ "}";
		byte[] message = msg.getBytes(Charset.forName("UTF-8"));
		PostDTO notification = new PostDTO(message, null, null);

		ResultDTO response2 = new ResultDTO();
		String msg2 = "{"
				+ "	\"certContent\": [\"mixer1\"]"
				+ "}";
		byte[] message2 = msg2.getBytes(Charset.forName("UTF-8"));
		response2.getPost().add(new PostDTO(message2, null, null));
		this.uniboardServiceMock.addResponse(response2);
		ResultDTO response3 = new ResultDTO();
		String msg3 = "{"
				+ "	\"certContent\": [\"tallier1\"],"
				+ "}";
		byte[] message3 = msg3.getBytes(Charset.forName("UTF-8"));
		response3.getPost().add(new PostDTO(message3, null, null));
		this.uniboardServiceMock.addResponse(response3);

		this.publishTrusteeCertsAction.notifyAction(context, notification);
		assertEquals((ResultStatus.FINISHED), this.actionManagerMock.getResultStatus());
		PostDTO post = this.uniboardServiceMock.getPost();
		Assert.assertArrayEquals("{\"mixerCertificates\" : [{	\"certContent\": [\"mixer1\"]}], \"tallierCertificates\" : [{	\"certContent\": [\"tallier1\"],}]}".getBytes(), post.getMessage());
	}

}
