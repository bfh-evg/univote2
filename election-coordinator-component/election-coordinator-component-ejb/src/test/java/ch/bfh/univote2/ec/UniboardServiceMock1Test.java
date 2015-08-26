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

import ch.bfh.uniboard.data.PostDTO;
import ch.bfh.uniboard.data.ResultContainerDTO;
import ch.bfh.uniboard.data.ResultDTO;
import ch.bfh.univote2.component.core.query.GroupEnum;
import java.nio.charset.Charset;
import java.util.List;
import javax.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;

/**
 *
 * @author Eric Dubuis &lt;eric.dubuis@bfh.ch&gt;
 */
@RunWith(Arquillian.class)
public class UniboardServiceMock1Test {
	/**
	 * Helper method for building the in-memory variant of a deployable unit. See Arquillian for more information.
	 *
	 * @return a Java archive
	 */
	@Deployment
	public static WebArchive createDeployment() {
		WebArchive ja = ShrinkWrap.create(WebArchive.class)
				.addClass(UniboardServiceMock1.class)
				.addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
		//System.out.println(ja.toString(true));
		return ja;
	}

	@EJB
	private UniboardServiceMock1 uniboardServiceMock;

	@Test
	public void testInjection() throws Exception {
		assertNotNull(this.uniboardServiceMock);
	}

	@Test
	public void testAddElectionDefinition1() throws Exception {
		String jsonElectionDefinition
				= "{\n"
				+ "	\"title\": {\n"
				+ "		\"default\": \"Universität Bern: Wahlen des SUB-StudentInnenrates\",\n"
				+ "		\"fr\": \"Université de Berne: Élection du conseil des étudiant-e-s SUB\",\n"
				+ "		\"en\": \"University of Bern: SUB Elections\"\n"
				+ "	},\n"
				+ "	\"administration\": {\n"
				+ "		\"default\": \"StudentInnenschaft der Universität Bern (SUB)\",\n"
				+ "		\"fr\": \"Ensemble des étudiants de l'Université de Berne (SUB)\",\n"
				+ "		\"en\": \"Student Body of the University of Bern (SUB)\"\n"
				+ "	},\n"
				+ "	\"votingPeriodBegin\": \"2015-03-08T23:00:00Z\",\n"
				+ "	\"votingPeriodEnd\": \"2015-03-26T11:00:00Z\"\n"
				+ "}";
		byte[] message = jsonElectionDefinition.getBytes(Charset.forName("UTF-8"));
		ResultDTO response = new ResultDTO();
		response.getPost().add(new PostDTO(message, null, null));
		// Post message to the mock.
		this.uniboardServiceMock.addResponse(response, GroupEnum.ELECTION_DEFINITION.getValue());

		ResultContainerDTO container = this.uniboardServiceMock.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForElectionDefinition("section"));
		assertNotNull(container);
		ResultDTO result = container.getResult();
		assertNotNull(result);
		List<PostDTO> posts = result.getPost();
		assertNotNull(posts);
		assertEquals(1, posts.size());
		PostDTO p = posts.get(0);
		assertNotNull(p);
		JSONAssert.assertEquals(jsonElectionDefinition, new String(p.getMessage(), Charset.forName("UTF-8")), true);
	}

	@Test
	public void testAddElectionDefinition2() throws Exception {
		String jsonElectionDefinition
				= "{\n"
				+ "	\"title\": {\n"
				+ "		\"default\": \"Universität Bern: Wahlen des SUB-StudentInnenrates\",\n"
				+ "		\"fr\": \"Université de Berne: Élection du conseil des étudiant-e-s SUB\",\n"
				+ "		\"en\": \"University of Bern: SUB Elections\"\n"
				+ "	},\n"
				+ "	\"administration\": {\n"
				+ "		\"default\": \"StudentInnenschaft der Universität Bern (SUB)\",\n"
				+ "		\"fr\": \"Ensemble des étudiants de l'Université de Berne (SUB)\",\n"
				+ "		\"en\": \"Student Body of the University of Bern (SUB)\"\n"
				+ "	},\n"
				+ "	\"votingPeriodBegin\": \"2015-03-08T23:00:00Z\",\n"
				+ "	\"votingPeriodEnd\": \"2015-03-26T11:00:00Z\"\n"
				+ "}";
		byte[] message = jsonElectionDefinition.getBytes(Charset.forName("UTF-8"));
		ResultDTO response = new ResultDTO();
		response.getPost().add(new PostDTO(message, null, null));
		// Post message to the mock.
		this.uniboardServiceMock.addResponse(response, GroupEnum.ELECTION_DEFINITION.getValue());

		// Not voting data should be present:
		ResultContainerDTO container = this.uniboardServiceMock.get(BoardsEnum.UNIVOTE.getValue(),
				QueryFactory.getQueryForVotingData("section"));
		assertNotNull(container);
		ResultDTO result = container.getResult();
		assertNotNull(result);
		List<PostDTO> posts = result.getPost();
		assertTrue(posts == null || posts.isEmpty());
	}
}
